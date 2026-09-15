/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competitivecounting;

import competitivecounting.interactionhandlers.CaptureHandler;
import competitivecounting.interactionhandlers.EmojiReactHandler;
import competitivecounting.interactionhandlers.TrophyHandler;
import competitivecounting.items.StreakEnders;
import competitivecounting.rules.*;
import competitivecounting.vaults.Vault;
import competitivecounting.vaults.VaultSpawner;
import discord4j.core.object.entity.Message;
import reactor.core.Disposable;

import java.time.Instant;
import java.util.*;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author DavidPrivat
 */
public class CountingStreak {

    private final String key;
    private final String guildId;
    private int counter, lastCount, lastScoreAdd;
    private String lastCounterId;
    private ArrayList<String> counterIds;

    private HashMap<String, Long> lastCaptureTimes = new HashMap<>();
    private List<String> captureBlockedUsers;

    private ArrayList<NumberRule> numberRules;
    private SlowModeRule slowModeRule;
    private TimeLimitRule timeLimitRule;
    private boolean timeLimitNewlyAdded = false;
    private int currDivPrice = 50, divPriceAdd = 50;
    private int currDigPrice = 100, digPriceFact = 2, digPriceLowBaseFact = 4;
    private int currRootPrice = 150;
    private int currTimePrice = 250, timePriceFact = 2;
    private int currentBase;

    private transient TrophyHandler trophyHandler;
    private transient CaptureHandler captureHandler;
    private transient EmojiReactHandler emojiReactHandler;
    private transient Disposable emojiReactSubscription;
    private transient StreakEnders streakEnders;
    private transient VaultSpawner vaultSpawner;

    private boolean destroyedByWrongCapture = false;

    private HashMap<String, Integer> amountOfCountsPerCounter = new HashMap<>();
    private HashMap<String, Long> lastCountingTimesPerCounter = new HashMap<>();

    private static final Map<String, Unlockable> RULE_MAP = Map.of(
            "div", Unlockable.DIV_RULE,
            "digsum", Unlockable.DIGSUM_RULE,
            "root", Unlockable.ROOT_RULE,
            "slowmode", Unlockable.SLOWMODE_RULE,
            "timelimit", Unlockable.TIMELIMIT_RULE
    );

    private transient CountingContext lastCountingContext = null;

    private List<String> counterIdsOfActiveSponsoredMonocles = new ArrayList<>();

    // Pre-initializing (fully replaced by loading from json!)
    public CountingStreak(String key, int base, String guildId) {
        this.key = key;
        this.guildId = guildId;
        counter = 1;
        lastCount = 0;
        lastCounterId = "";
        counterIds = new ArrayList<>();
        numberRules = new ArrayList<>();
        currentBase = base;

        initialize();
    }

    // Initialize either after constructor or after loaded from json
    public void initialize() {
        emojiReactHandler = new EmojiReactHandler(key);
        emojiReactSubscription = CountingBot.getInstance().subscribeEmojiReactHandler(emojiReactHandler, key);
        trophyHandler = new TrophyHandler(emojiReactHandler);
        captureHandler = new CaptureHandler(emojiReactHandler);
        streakEnders = new StreakEnders(this);
        captureBlockedUsers = new ArrayList<>();
        vaultSpawner = new VaultSpawner();

        if (timeLimitRule != null){
            timeLimitRule.initialize(this);
        }
    }

    public boolean count(Message message, Counter user, String content, Runnable asyncStreakDelete) {
        if(destroyedByWrongCapture) {
            return false;
        }
        if (captureBlockedUsers.contains(user.getId())) {
            CountingBot.write(message, "Please answer the captcha before counting the next number, " + user.getName() + "!");
            return true;
        }
        if (!counterIds.contains(user.getId())) {
            addCounter(user, message);
        }
        int number = BaseSystems.toDecimal(content, currentBase);
        if (number < 0) {
            CountingBot.write(message, "Number too big!");
            return true;
        }
        if (Math.abs(((double) number) / ((double) counter)) > 100.0 || Math.abs(((double) counter) / ((double) number)) > 100.0) {      // nonsense or image or doc
            CountingBot.write(message, "This number will be ignored");
            return true;
        }
        if (lastCount == 1 && number == 1) {
            CountingBot.write(message, "This number will be ignored; A streak has already been started, please continue with the next number!");
            return true;
        }
        if (vaultSpawner.hasRunningVault()) {  // check is only performed down here to see if we should communicate to the user that this number would have been ignored
            CountingBot.write(message, "Please finish the current active " + vaultSpawner.getActiveVault().getVaultName() + " before counting the next number, " + user.getName() + "!");
            return true;
        }


        if (isNumCorrect(number, message) && (!user.getId().equals(lastCounterId))) { // Count is accepted
            lastCountingContext = new CountingContext(user, number, lastCount, this, lastScoreAdd, lastCounterId,
                    CountingBot.getInstance().getGuilds().get(guildId));
            lastCount = number;
            incrementCounter();
            amountOfCountsPerCounter.replace(user.getId(), amountOfCountsPerCounter.get(user.getId()) + 1);
            lastCountingTimesPerCounter.put(user.getId(), Instant.now().getEpochSecond());
            int currScoreAdd = user.notifyCountAndGetScoreAdd(number, lastCountingContext);
            counterIds.stream()
                    .flatMap(id -> CountingBot.getCounter(guildId, id).getCollection().getEquippables().stream())
                    .forEach(eq -> eq.performPassiveAfterCounterReceivesMoney(message, lastCountingContext, currScoreAdd));
            if(captureHandler.raisedCapture(message, number, user.getId(), lastCaptureTimes, () -> {    // onCaptureFailed
                destroyedByWrongCapture = true;
                fail(message, number, user);
                asyncStreakDelete.run();
            }, () -> {  // onCaptureSucceeded
                captureBlockedUsers.remove(user.getId());
            }, trophyHandler)) {   // Capture was raised
                captureBlockedUsers.add(user.getId());
            }

            trophyHandler.considerSpawningTrophy(message, lastCountingContext);

            if (slowModeRule != null) {
                slowModeRule.applyTimerToMessage(message);
            } else if (timeLimitRule != null && (!timeLimitNewlyAdded)) {
                timeLimitRule.applyTimerToMessage(message, CountingBot.getCounter(guildId,lastCounterId));
            } else {
                if (user.hasTrophy(number)) {
                    message.addReaction(CountingEmojis.GOLDEN_KEKMARK).subscribe();
                } else {
                    message.addReaction(CountingEmojis.KEKMARK).subscribe();
                }
            }
            if (timeLimitNewlyAdded) {
                String notimeAddCost = (user.getAddruleDiscountFactor() != 1.0) ?
                        Util.valueAndValueWithBoniToString(currTimePrice, (int) (currTimePrice*user.getAddruleDiscountFactor()))
                        : String.valueOf(currTimePrice);
                CountingBot.write(message, "Watch out! The next number will activate the timelimit countdown!\n" + user.getPing() + " be ready to keep counting!\n" +
                        "-# Hint: `~addrule notime` can be used to remove the time limit, but it will cost " + notimeAddCost + " money.");
                timeLimitNewlyAdded = false;
            }
            lastCounterId = user.getId();

            Optional<Vault> maybeVault = vaultSpawner.maybeSpawnVault(message, lastCountingContext);
            lastScoreAdd = currScoreAdd;
            return true;
        } else {
            fail(message, number, user);
            return false;
        }
    }

    public void addCounter(Counter add, Message message) {
        if (!counterIds.contains(add.getId())) {
            counterIds.add(add.getId());
        }
        if (!amountOfCountsPerCounter.containsKey(add.getId())) {
            amountOfCountsPerCounter.put(add.getId(), 0);
        }
        add.addStreakToCurrAdd(this);
        lastCaptureTimes.put(add.getId(), System.currentTimeMillis());
        if (!add.isBaseUnlocked(currentBase)) {
            CountingBot.write(message, add.getPing() + " you haven't unlocked this base yet, so you will not get the additional " + Counter.SYSTEM_OWNED_FACT + "x bonus.");
        } else if (currentBase != 10) {
            CountingBot.write(message, "Wow " + add.getPing() + ", it seems like you own this streak's base and are getting a " + Counter.SYSTEM_OWNED_FACT + "x bonus!"); // wow!
        }
    }

    private void fail(Message message, int number, Counter user) {
        message.addReaction(CountingEmojis.X).subscribe();

        int pendingFailScore = user.getPendingStreakScore(this);
        int cuckPayout = 0;

        Rule winnerRule = getWinnerRule(number);
        if ((!(winnerRule instanceof TimeLimitRule)) && timeLimitRule != null) {
            timeLimitRule.cancel();
        }

        // efficiency maxxing
        String countDisplay = (currentBase == 10)
                ? String.valueOf(lastCount)
                : BaseSystems.decimalToSystem(lastCount, currentBase) + " (=" + lastCount + ")";

        String nextCountDisplay = (currentBase == 10)
                ? String.valueOf(counter)
                : BaseSystems.decimalToSystem(counter, currentBase) + " (=" + counter + ")";

        String ruleWinnerId = null;
        int winFromRules = 0;
        String moneyInformation; // who lost what to whom, the all known w fragen

        if (winnerRule != null) {
            int loss;
            String causeForLose = "Wrong number";
            if (winnerRule instanceof SlowModeRule) {
                causeForLose = "Slowmode-rule broken";
            } else if (winnerRule instanceof TimeLimitRule) {
                causeForLose = "Timelimit-rule broken";
            } else if (destroyedByWrongCapture) {
                causeForLose = "Incorrectly answered captcha";
            }

            if (winnerRule.getOwnerId().equals(user.getId())) {
                // user fucks up from own rule
                cuckPayout = (int) (pendingFailScore / 2.0d);
                loss = user.failFromOwn(message, this);
                CountingBot.write(message, causeForLose + "!\n" + user.getName() + " messed up after " + countDisplay + ". The next number would have been " + nextCountDisplay + ".");

                moneyInformation = user.getName() + " lost **" + loss + "** money due to their own rule '" + winnerRule.toString() + "', but kept **" + cuckPayout + "** money from the streak.";
            } else {
                // user gets fucked from other rule
                cuckPayout = (int) (pendingFailScore / 3.0d);
                loss = user.fail(message, this);
                CountingBot.write(message, causeForLose + "!\n" + user.getName() + " messed up after " + countDisplay + ". The next number would have been " + nextCountDisplay + ".");

                Counter winnerCounter = CountingBot.getCounter(guildId, winnerRule.getOwnerId());
                ruleWinnerId = winnerCounter.getId();
                winFromRules = loss;

                moneyInformation = user.getName() + " lost **" + loss + "** total money, but kept **" + cuckPayout + "** money from the streak.\n" + winnerCounter.getName() + " has pulled a fast one on " + user.getName()
                        + " with their '" + winnerRule + "' rule and got all of the victim's lost money!";

                winnerCounter.notifyWin(loss, currentBase, message);
            }

        } else {
            cuckPayout = (int) (pendingFailScore / 3.0d);
            int loss = user.fail(message, this);

            if (!user.getId().equals(lastCounterId)) {
                CountingBot.write(message, "Wrong number!\n" + user.getName() + " messed up after " + countDisplay + ". The next number would have been " + nextCountDisplay + ".");
            } else if (destroyedByWrongCapture){
                CountingBot.write(message, "Incorrectly answered captcha!\n" + user.getName() + " scripted all the way to " + countDisplay + "! Please count by hand next time...");
            } else {
                CountingBot.write(message, "Oops!\n" + user.getName() + " counted twice in a row at " + countDisplay + ".");
            }

            moneyInformation = user.getName() + " lost **" + loss + "** money, but kept **" + cuckPayout + "** money from the streak.";
        }

        // now we are no longer cavemen!
        streakPayout(message, user.getId(), cuckPayout, ruleWinnerId, winFromRules, moneyInformation);
        CountingBot.getInstance().save();
    }

    /**
     *
     * @param message
     * @param idOfCuck - nullable if noone cucked!
     * @param cuckPayout - ignored if idOfCuck is null (or not equal to any counter id)
     * @param ruleWinnerId - nullable if noone won from rules
     * @param ruleWinAmount - ignored if ruleWeinerId is null
     * @param moneyInformation - string detailing the loser's money loss and rule thefts
     */
    public void streakPayout(Message message, String idOfCuck, int cuckPayout, String ruleWinnerId, int ruleWinAmount, String moneyInformation) {
        // todo: ggf. schreiben wieviel des payouts an contracts gezahlt wurde
        class Payout { // zeit es komplett zu übertreiben part 2
            final Counter counter;
            int streakAmount = 0;
            int ruleAmount = 0;
            Payout(Counter counter) {this.counter = counter;}
            int getTotal() {return streakAmount + ruleAmount;}
        }

        Map<String, Payout> streakPayoutMap = new HashMap<>();
        // normale payouts und cuck
        for (String currCounterId : counterIds) {
            Counter counter = CountingBot.getCounter(guildId, currCounterId);
            Payout payout = streakPayoutMap.computeIfAbsent(currCounterId, id -> new Payout(counter));

            if (currCounterId.equals(idOfCuck)) {  // cucked
                payout.streakAmount = cuckPayout;
            } else {  // not cucked leel
                payout.streakAmount = counter.getPendingStreakScore(this);
                counter.succeed(this, message);
            }
        }

        // handle rule owner payout (even if rule owner not in streak)
        if (ruleWinnerId != null && ruleWinAmount > 0) {
            Counter winnerCounter = CountingBot.getCounter(guildId, ruleWinnerId);
            Payout payout = streakPayoutMap.computeIfAbsent(ruleWinnerId, id -> new Payout(winnerCounter));
            payout.ruleAmount = ruleWinAmount;
        }

        // finally sortiert der spast
        List<Payout> sortedPayouts = streakPayoutMap.values()
                .stream()
                .filter(p -> p.getTotal() > 0)
                .filter(p -> !p.counter.getId().equals(idOfCuck))  // THE WINNER TAKES IT AAAAALLL AND THE LOSER HAS TO FALL
                .sorted((a, b) -> Integer.compare(b.getTotal(), a.getTotal()))
                .collect(Collectors.toList());

        StringBuilder payoutMessage = new StringBuilder();

        if (moneyInformation != null && !moneyInformation.isEmpty()) {
            payoutMessage.append(moneyInformation).append("\n\n");
        }

        if (!sortedPayouts.isEmpty()) {
            payoutMessage.append("**Streak payouts:**\n");
            int position = 1;
            for (Payout p : sortedPayouts) {
                if (position > 5) {
                    int remainingAmountCounters = sortedPayouts.size() - 5;
                    payoutMessage.append("... and ").append(remainingAmountCounters).append(" more counters\n");
                    break;
                }
                payoutMessage.append("­").append(position).append(". ").append(p.counter.getName())
                        .append(": **").append(p.streakAmount).append("** money");

                if (p.ruleAmount > 0) {
                    payoutMessage.append(" (**+").append(p.ruleAmount).append("** from rules)");
                }
                payoutMessage.append("\n");
                position++;
            }
        }

        if (payoutMessage.length() > 0) {
            CountingBot.write(message, payoutMessage.toString().trim());
        }
    }

    private Rule getWinnerRule(int number) {
        for (NumberRule rule : numberRules) {
            if (!rule.numberAccepted(number)) {
                return rule;
            }
        }
        if (slowModeRule != null && slowModeRule.hasLost()) {
            return slowModeRule;
        }
        if (timeLimitRule != null && timeLimitRule.hasLost()) {
            return timeLimitRule;
        }
        return null;
    }

    private void incrementCounter() {
        do {
            counter++;
        } while (!numberAccepted());
    }

    public double getTimeRulesBonusFact() {
        if (timeLimitRule != null) {
            return TimeLimitRule.BONUS_FACTOR;
        } else if (slowModeRule != null && !slowModeRule.isNewlyAdded()) {
            return slowModeRule.getCurrentBonusFactor();
        }
        return 1.0;
    }

    private boolean hasUnlockedNumberRule(Counter author, String ruleType) {
        Unlockable rule = RULE_MAP.get(ruleType);
        return rule == null || author.isUnlocked(rule);
    }

    public boolean canAffordNumberRulesWithOneType(Message message, Counter author, String ruleType, int amountRules) {
        int totalPrice = 0;
        int dummyCurrentPrice = 0;
        int fahh = 1;
        IntBinaryOperator operation = (price, value) -> price;
        switch (ruleType) {
            case "div":
                dummyCurrentPrice = currDivPrice;
                fahh = divPriceAdd;
                operation = Integer::sum;
                break;
            case "digsum":
                dummyCurrentPrice = currDigPrice;
                if (currentBase == 1) {
                    fahh = 1;
                    operation = Integer::sum;
                } else {
                    fahh = currentBase < 4 ? digPriceLowBaseFact : digPriceFact;
                    operation = (price, value) -> price * value;
                }
                break;
            case "root":
                dummyCurrentPrice = currRootPrice;
                break;
            case "slowmode":
            case "timelimit":
            case "notime":
                dummyCurrentPrice = currTimePrice;
                fahh = timePriceFact;
                operation = (price, value) -> price * value;
                break;
        }

        for (int i = 0; i < amountRules; i++) {
            totalPrice += dummyCurrentPrice;
            dummyCurrentPrice = operation.applyAsInt(dummyCurrentPrice, fahh);
        }
        boolean canAfford = author.canAfford((int) (totalPrice * author.getAddruleDiscountFactor()));
        if (!canAfford) {
            String addOrRemoveOneOrMany = (ruleType.equals("notime")) ? "to remove the timed rules." : "to add" + ((amountRules > 1) ? " these " + amountRules + " new rules." : "this new rule.");
            CountingBot.write(message, "You only have " + author.getScore() + " out of the needed "
                    + Util.valueAndValueWithBoniToString(totalPrice, (int) (totalPrice * author.getAddruleDiscountFactor())) + " money " + addOrRemoveOneOrMany);
        }
        return author.canAfford(totalPrice);
    }

    private void addRuleInfo(Message message, Counter author) {
        String answer = "You can choose to add the following rules:\n";
        boolean anyRule = false;
        if (timeLimitRule != null || slowModeRule != null) {
            answer += "\n'notime': Remove the current slowmode/timelimit. " + createAddruleCostString(author, currTimePrice);
            anyRule = true;
        }
        if (author.isUnlocked(Unlockable.DIV_RULE)) {
            answer += "\n'div': Numbers with the divisor n have to be skipped. " + createAddruleCostString(author, currDivPrice);
            anyRule = true;
        }
        if (author.isUnlocked(Unlockable.ROOT_RULE)) {
            answer += "\n'root': Numbers which have an integer nth root must be skipped. " + createAddruleCostString(author, currRootPrice);
            anyRule = true;
        }
        if (author.isUnlocked(Unlockable.DIGSUM_RULE)) {
            answer += "\n'digsum': Numbers with digsum n must be skipped. " + createAddruleCostString(author, currDigPrice);
            anyRule = true;
        }
        if (author.isUnlocked(Unlockable.SLOWMODE_RULE)) {
            answer += "\n'slowmode': A certain time n has to pass between counts. " + createAddruleCostString(author, currTimePrice);
            anyRule = true;
        }
        if (author.isUnlocked(Unlockable.TIMELIMIT_RULE)) {
            answer += "\n'timelimit': The next number must have been counted before 10s have passed. " + createAddruleCostString(author, currTimePrice);
            anyRule = true;
        }
        answer += "\n\n syntax: '~addrule [name] ([argument])";
        if (anyRule) {
            CountingBot.write(message, answer);
        } else {
            CountingBot.write(message, "Unlock rules with ~unlock!");
        }
    }

    private String createAddruleCostString(Counter adder, double cost) {
        double addruleDiscount = adder.getAddruleDiscountFactor();
        if(addruleDiscount == 1.0) {
            return "(cost: " + (int)cost + ")";
        } else {
            return "(cost: ~~" + (int)cost + "~~ " + (int) (cost * addruleDiscount) + ")";
        }
    }

    public void addRule(Message message, String ownerId) {
        if (!counterIds.contains(ownerId)) {
            String guildId = message.getGuildId().get().asString();
            addCounter(CountingBot.getCounter(guildId, ownerId), message);
        }
        String content = message.getContent();
        String[] splitted = content.split(" ");
        Counter author = CountingBot.getCounter(guildId, ownerId);

        if (splitted.length != 2 && splitted.length != 3) {
            addRuleInfo(message, author);
            return;
        }

        String ruleName;

        try {
            ruleName = splitted[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return;
        }
        if (!RULE_MAP.containsKey(ruleName) && !ruleName.equals("notime")) {
            CountingBot.write(message, "This rule doesn't exist.");
            return;
        }
        if (!hasUnlockedNumberRule(author, ruleName)) {
            CountingBot.write(message, "You have to unlock this rule with ~unlock before you can use it.");
            return;
        }
        String[] ruleNumbers;
        if (!Set.of("notime", "timelimit").contains(ruleName)) {
            if (splitted.length < 3) {
                CountingBot.write(message, "Error: Please enter a number!");
                return;
            }
            ruleNumbers = splitted[2].split(";");
        } else {
            ruleNumbers = new String[0];
        }

        if (!canAffordNumberRulesWithOneType(message, author, ruleName, ruleNumbers.length)) {
            return;
        }

        switch (ruleName) {
            case "div":
                DividerRule dummyDivRule = new DividerRule(ownerId, 0, currentBase);

                String divValidationResult = validateNumberRuleStringArray(ruleNumbers, dummyDivRule, currentBase);
                if (!Objects.equals(divValidationResult, "valid")) {
                    CountingBot.write(message, divValidationResult);
                    return;
                }

                int totalDivRulesAddedCost = 0;
                List<DividerRule> divRulesList = new ArrayList<>();

                for (String ruleNumberString : ruleNumbers) {
                    String probablyNumber = ruleNumberString.trim();
                    if (probablyNumber.isEmpty()) continue;
                    int divInDecimal = BaseSystems.toDecimal(probablyNumber, currentBase);

                    DividerRule ruleToAdd = new DividerRule(ownerId, divInDecimal, currentBase);
                    addNumberRule(ruleToAdd);
                    divRulesList.add(ruleToAdd);

                    totalDivRulesAddedCost += currDivPrice;
                    author.subtractScore((int) (author.getAddruleDiscountFactor() * currDivPrice));
                    currDivPrice += divPriceAdd;
                }
                CountingBot.write(message, createYouPaidToAddMultipleRuleString(author, totalDivRulesAddedCost, divRulesList));
                break;
            case "digsum":
                DigSumRule dummyDigsumRule = new DigSumRule(ownerId, 0, currentBase);
                String digsumValidationResult = validateNumberRuleStringArray(ruleNumbers, dummyDigsumRule, currentBase);
                if (!Objects.equals(digsumValidationResult, "valid")) {
                    CountingBot.write(message, digsumValidationResult);
                    return;
                }

                int totalDigsumRulesAddedCost = 0;
                List<DigSumRule> digsumRuleList = new ArrayList<>();

                for (String ruleNumberString : ruleNumbers) {
                    String probablyNumber = ruleNumberString.trim();
                    if (probablyNumber.isEmpty()) continue;
                    int digsumInDecimal = BaseSystems.toDecimal(probablyNumber, currentBase);

                    DigSumRule ruleToAdd = new DigSumRule(ownerId, digsumInDecimal, currentBase);
                    addNumberRule(ruleToAdd);
                    digsumRuleList.add(ruleToAdd);

                    totalDigsumRulesAddedCost += currDigPrice;
                    author.subtractScore((int) (author.getAddruleDiscountFactor() * currDigPrice));
                    if(currentBase == 1) {
                        currDigPrice++;
                    } else if (currentBase < 5) {
                        currDigPrice *= digPriceLowBaseFact;
                    } else {
                        currDigPrice *= digPriceFact;
                    }
                }
                CountingBot.write(message, createYouPaidToAddMultipleRuleString(author, totalDigsumRulesAddedCost, digsumRuleList));
                break;
            case "root":
                RootRule dummyRootRule = new RootRule(ownerId, 0, currentBase);
                String rootValidationResult = validateNumberRuleStringArray(ruleNumbers, dummyRootRule, currentBase);
                if (!Objects.equals(rootValidationResult, "valid")) {
                    CountingBot.write(message, rootValidationResult);
                    return;
                }

                int totalRootRulesAddedCost = 0;
                List<RootRule> rootRuleList = new ArrayList<>();

                for (String ruleNumberString : ruleNumbers) {
                    String probablyNumber = ruleNumberString.trim();
                    if (probablyNumber.isEmpty()) continue;
                    int rootInDecimal = BaseSystems.toDecimal(probablyNumber, currentBase);

                    RootRule ruleToAdd = new RootRule(ownerId, rootInDecimal, currentBase);
                    addNumberRule(ruleToAdd);
                    rootRuleList.add(ruleToAdd);

                    totalRootRulesAddedCost += currRootPrice;
                    author.subtractScore((int) (author.getAddruleDiscountFactor() * currRootPrice));
                }
                CountingBot.write(message, createYouPaidToAddMultipleRuleString(author, totalRootRulesAddedCost, rootRuleList));
                break;
            case "slowmode":
                int slow;
                try {
                    slow = Integer.parseUnsignedInt(splitted[2]);
                } catch (NumberFormatException e) {
                    CountingBot.write(message, "Error: Please enter an integer without special characters!");
                    return;
                }
                if (slow < 6) {
                    CountingBot.write(message, "Error: Please enter an integer greater than 6!");
                    return;
                }
                slowModeRule = new SlowModeRule(slow, ownerId);
                CountingBot.write(message, createYouPaidToAddRuleString(author, currTimePrice, slowModeRule.toString()));
                if (timeLimitRule != null) {
                    CountingBot.write(message, "This rule is being replaced: " + timeLimitRule);
                    timeLimitRule = null;
                }
                author.subtractScore((int) (author.getAddruleDiscountFactor() * currTimePrice));
                currTimePrice *= timePriceFact;
                break;
            case "timelimit":
                this.timeLimitNewlyAdded = true;
                timeLimitRule = new TimeLimitRule(ownerId, this);

                CountingBot.write(message, createYouPaidToAddRuleString(author, currTimePrice, timeLimitRule.toString()));
                if (slowModeRule != null) {
                    CountingBot.write(message, "This rule is being replaced: " + slowModeRule);
                    slowModeRule = null;
                }
                author.subtractScore((int) (author.getAddruleDiscountFactor() * currTimePrice));
                currTimePrice *= timePriceFact;
                break;
            case "notime":
                if (!author.canAfford(currTimePrice)) {
                    CountingBot.write(message, "You only have " + author.getScore() + " out of the needed " +
                            Util.valueAndValueWithBoniToString(currTimePrice, (int) (currTimePrice * author.getAddruleDiscountFactor())) + " money to remove the current time rule.");
                    break;
                }
                if (slowModeRule != null) {
                    slowModeRule.stop();
                    CountingBot.write(message, "You paid " + Util.valueAndValueWithBoniToString(currTimePrice, (int) (currTimePrice * author.getAddruleDiscountFactor())) +
                            " to remove: " + slowModeRule.toString());
                    slowModeRule = null;
                    author.subtractScore((int) (author.getAddruleDiscountFactor() * currTimePrice));
                    currTimePrice *= timePriceFact;
                } else if (timeLimitRule != null) {
                    timeLimitRule.cancel();
                    CountingBot.write(message, "You paid " + Util.valueAndValueWithBoniToString(currTimePrice, (int) (currTimePrice * author.getAddruleDiscountFactor())) +
                            " to remove: " + timeLimitRule.toString());
                    timeLimitRule = null;
                    author.subtractScore((int) (author.getAddruleDiscountFactor() * currTimePrice));
                    currTimePrice *= timePriceFact;
                } else {
                    CountingBot.write(message, "No active time rule to remove!");
                }
                break;
            default:
                CountingBot.write(message, "This rule does not exist.");
        }
        if (!numberAccepted()) {
            incrementCounter();
        }
    }

    private String createYouPaidToAddRuleString(Counter author, double cost, String ruleName) {
        double addruleDiscount = author.getAddruleDiscountFactor();
        return "You paid " + Util.valueAndValueWithBoniToString((int) cost, (int) (cost * addruleDiscount)) + " to add: " + ruleName;
    }

    private String createYouPaidToAddMultipleRuleString(Counter author, double totalCost, List<? extends NumberRule> ruleNames) {  // what the fuck is this
        double addruleDiscount = author.getAddruleDiscountFactor();
        StringBuilder sb = new StringBuilder("You paid ").append(Util.valueAndValueWithBoniToString((int)totalCost, (int) (totalCost*addruleDiscount))).append(" to add: ").append(ruleNames.get(0).getRuleTypeString());
        for (int i = 0; i < ruleNames.size(); i++) {
            sb.append(ruleNames.get(i).getValueInBase());
            if (i < ruleNames.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String validateNumberRuleStringArray(
            String[] ruleArray,
            NumberRule rule,
            int currentBase) {

        for (String ruleNumber : ruleArray) {
            String probablyNumber = ruleNumber.trim();

            if (probablyNumber.isEmpty()) {
                continue;
            }

            int number;

            try {
                if (!BaseSystems.isNumInSystem(probablyNumber, currentBase)) {
                    return "Error: Please enter a number in the current base system!";
                }

                number = BaseSystems.toDecimal(probablyNumber, currentBase);

            } catch (NumberFormatException e) {
                return "Error: Please enter an integer without special characters!";
            }

            if (number < rule.getMinimumValue()) {
                return "Error: Please enter an integer greater than 1!";
            }
        }

        return "valid";
    }

    public void addNumberRule(NumberRule rule) {
        numberRules.add(rule);
    }

    public String getRulesRespond() {
        if (numberRules.isEmpty() && timeLimitRule == null && slowModeRule == null) {
            return "No rules!";
        } else {
            ArrayList<NumberRule> sanitisedNumberRules = sanitiseNumberRuleList(numberRules);
            String ret = "Active rules:";
            for (NumberRule rule : sanitisedNumberRules) {
                ret += "\n\t\\- " + rule.toString();
            }
            if (slowModeRule != null) {
                ret += "\n\t\\- " + slowModeRule.toString();
            }
            if (timeLimitRule != null) {
                ret += "\n\t\\- " + timeLimitRule.toString();
            }
            return ret;
        }
    }

    public ArrayList<NumberRule> sanitiseNumberRuleList(ArrayList<NumberRule> rules) {
        return new ArrayList<>(new LinkedHashSet<>(rules));
    }

    public String getCompactRulesInfo() {
        if (numberRules.isEmpty() && timeLimitRule == null && slowModeRule == null) {
            return "No rules!";
        }
        StringBuilder builder = new StringBuilder("Active Rules:");
        if (!numberRules.isEmpty()) {
            ArrayList<NumberRule> sanitisedNumberRules = sanitiseNumberRuleList(numberRules);
            Map<String, List<NumberRule>> rulesInAGroup = sanitisedNumberRules.stream().collect(Collectors.groupingBy(NumberRule::getRuleTypeString, TreeMap::new, Collectors.toList()));

            for (Map.Entry<String, List<NumberRule>> entry : rulesInAGroup.entrySet()) {
                builder.append("\n\t\\- ").append(entry.getKey());
                String values = entry.getValue().stream()
                        .sorted((a, b) -> Integer.compare(
                                BaseSystems.toDecimal(a.getValueInBase(), currentBase),
                                BaseSystems.toDecimal(b.getValueInBase(), currentBase)
                        ))
                        .map(NumberRule::getValueInBase)
                        .collect(Collectors.joining(", "));  // scheis java 2.0
                builder.append(values);
            }
        }
        if (slowModeRule != null) {
            builder.append("\n\t\\- ").append(slowModeRule.toString());
        }
        if (timeLimitRule != null) {
            builder.append("\n\t\\- ").append(timeLimitRule.toString());
        }
        return builder.toString();
    }

    public String getBaseInfoRespond(int mode) {
        String ret = "";
        switch (mode) {
            case 0: // ~base
                ret = "The current base is **" + currentBase + "** with these characters:\n";
                if (currentBase == 1) {
                    ret += "1&1";
                } else {
                    for (int i = 0; i < currentBase; i++) {
                        ret += BaseSystems.digitToChar(i) + " ";
                    }
                }
            case 1: // ~streak
                ret = "Base: " + currentBase + (currentBase == 1 ? ", character: " : ", characters: ");
                ret += (currentBase == 1) ? "1&1" : IntStream.range(0, currentBase).mapToObj(i -> BaseSystems.digitToChar(i) + " ").collect(Collectors.joining());
        }
        return ret;
    }

    private boolean numberAccepted() {
        for (NumberRule rule : numberRules) {
            if (!rule.numberAccepted(counter)) {
                return false;
            }
        }
        return true;
    }

    private boolean isNumCorrect(int num, Message message) {
        if (slowModeRule != null) {
            if (!slowModeRule.accepted(message)) {
                return false;
            }
        }
        return num == (counter);
    }

    public Counter getLastCounter() {
        return CountingBot.getCounter(guildId, lastCounterId);
    }

    public int getLastNum() {
        return lastCount;
    }

    public String getKey() {
        return key;
    }

    public void timeLimitLost(String ownerId, Message message, Counter loser) {
        Message lostMessage = message.getChannel().block().createMessage("Whoops! Time ran out!").block();
        fail(lostMessage, lastCount, loser);
    }

    public EmojiReactHandler getEmojiReactHandler() {
        return emojiReactHandler;
    }

    public int getBase() {
        return currentBase;
    }

    public void dispose() {
        if (slowModeRule != null) {
            slowModeRule.stop();
        }
        if (timeLimitRule != null) {
            timeLimitRule.cancel();
        }
        streakEnders.dispose();
        emojiReactSubscription.dispose();
        vaultSpawner.dispose();
        counterIds.stream().map(id -> CountingBot.getCounter(guildId, id)).forEach(c->c.streakDisposed(this));
    }

    public TrophyHandler getTrophyHandler() {
        return trophyHandler;
    }

    public HashMap<String, Integer> getAmountOfCountsPerCounter() {
        return amountOfCountsPerCounter;
    }

    public int getTotalAmountOfCounts() {
        int total = 0;
        for (int amount : amountOfCountsPerCounter.values()) {
            total += amount;
        }
        return total;
    }

    public String getGuildId() {
        return guildId;
    }

    public StreakEnders getStreakEnders() {
        return streakEnders;
    }

    public VaultSpawner getVaultSpawner() {
        return vaultSpawner;
    }

    public CountingContext getLastContext() {
        return lastCountingContext;
    }

    public String getNextCorrectNumberInBase() {
        String num = BaseSystems.decimalToSystem(counter, currentBase);
        return num + (currentBase == 10 ? "" : " (base " + currentBase + ")");
    }

    public HashMap<String, Long> getLastCountingTimesPerCounter() {
        return lastCountingTimesPerCounter;
    }

    public List<String> getCounterIdsOfActiveSponsoredMonocles() {
        return counterIdsOfActiveSponsoredMonocles;
    }

    public CountingContext getLastCountingContext() {
        return lastCountingContext;
    }
}
