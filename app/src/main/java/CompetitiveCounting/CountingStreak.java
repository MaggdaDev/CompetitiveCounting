/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;

import CompetitiveCounting.dialogue.Dialogue;
import CompetitiveCounting.items.StreakEnders;
import CompetitiveCounting.rules.*;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.Disposable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author DavidPrivat
 */
public class CountingStreak {

    private final String key;
    private final String guildId;
    private int counter, lastCount;
    private String lastCounterId;
    private ArrayList<String> counterIds;

    private HashMap<String, Long> lastCaptureTimes = new HashMap<>();
    private final List<String> captureBlockedUsers = new ArrayList<>();

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

    private boolean destroyedByWrongCapture = false;

    private HashMap<String, Integer> amountOfCountsPerCounter = new HashMap<>();

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

        if (timeLimitRule != null){
            timeLimitRule.initialize(this);
        }
    }

    public boolean count(Message message, Counter user, String content, Runnable asyncStreakDelete) {
        if(destroyedByWrongCapture) {
            return false;
        }
        if (captureBlockedUsers.contains(user.getId())) {
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

        if (isNumCorrect(number, message) && (!user.getId().equals(lastCounterId))) { // Count is accepted
            lastCount = number;
            incrementCounter();
            amountOfCountsPerCounter.replace(user.getId(), amountOfCountsPerCounter.get(user.getId()) + 1);
            user.notifyCount(number, this);

            if(captureHandler.raisedCapture(message, number, user.getId(), lastCaptureTimes, () -> {    // onCaptureFailed
                destroyedByWrongCapture = true;
                fail(message, number, user);
                asyncStreakDelete.run();
            }, () -> {  // onCaptureSucceeded
                captureBlockedUsers.remove(user.getId());
            }, trophyHandler)) {   // Capture was raised
                captureBlockedUsers.add(user.getId());
            }

            trophyHandler.considerSpawningTrophy(number, message, user);

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
                CountingBot.write(message, "Watch out! The next number will activate the timelimit countdown!\n" + user.getPing() + " be ready to keep counting!\n" +
                        "-# Hint: `~addrule notime` can be used to remove the time limit, but it will cost more money.");  // 67 wer das findet ist dumm leel kann das auf sohn ~~500~~ 450 upgraden bin zu faul leel
                timeLimitNewlyAdded = false;
            }
            lastCounterId = user.getId();
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
        message.addReaction(ReactionEmoji.unicode("\u274C")).subscribe();

        int pendingFailScore = user.getPendingStreakScore(this);
        int cuckPayout;

        Rule winnerRule = getWinnerRule(number);
        if ((!(winnerRule instanceof TimeLimitRule)) && timeLimitRule != null) {
            timeLimitRule.cancel();
        }
        String winnerName;
        if (winnerRule != null) {
            int loss;
            String causeForLose = "Wrong number";
            if (winnerRule instanceof SlowModeRule) {
                causeForLose = "Slowmode-rule broken";
            } else if (winnerRule instanceof TimeLimitRule) {
                causeForLose = "Timelimit-rule broken";
            }
            if (winnerRule.getOwnerId().equals(user.getId())) {
                // user fucks up from own rule
                cuckPayout = (int) (pendingFailScore / 2.0d);
                loss = user.failFromOwn(message, this);
                if (currentBase == 10) {
                    CountingBot.write(message, causeForLose + "!\n" + user.getName() + " messed up after " + lastCount + " due to his own rule '" + winnerRule.toString() + "' and lost " + loss + " money.");
                } else {
                    CountingBot.write(message, causeForLose + "!\n" + user.getName() + " messed up after " + BaseSystems.decimalToSystem(lastCount, currentBase) + " (=" + lastCount + ") due to his own rule '" + winnerRule.toString() + "' and lost " + loss + " money.");
                }
            } else {
                cuckPayout = (int) (pendingFailScore / 3.0d);
                loss = user.fail(message, this);
                if (currentBase == 10) {
                    CountingBot.write(message, causeForLose + "!\n" + user.getName() + " messed up after " + lastCount + " and lost " + loss + " money.");
                } else {
                    CountingBot.write(message, causeForLose + "!\n" + user.getName() + " messed up after " + BaseSystems.decimalToSystem(lastCount, currentBase) + " (=" + lastCount + ") and lost " + loss + " money.");
                }

                int win = loss;
                Counter winnerCounter = CountingBot.getCounter(guildId, winnerRule.getOwnerId());
                winnerName = winnerCounter.getName();
                CountingBot.write(message, winnerName + " has pulled a fast one on " + user.getName()
                        + " with their '" + winnerRule + "' rule and got all of the victim's lost money, which is " + win + ".");
                winnerCounter.notifyWin(win, currentBase, message);
            }

        } else {
            cuckPayout = (int) (pendingFailScore / 3.0d);
            int loss = user.fail(message, this);
            if (currentBase == 10) {
                if (!user.getId().equals(lastCounterId)) {
                    CountingBot.write(message, "Wrong number!\n" + user.getName() + " messed up after " + lastCount + " and lost " + loss + " money.");
                } else {
                    CountingBot.write(message, "Oops!\n" + user.getName() + " counted twice in a row at " + lastCount + " and lost " + loss + " money.");
                }
            } else {
                if (!user.getId().equals(lastCounterId)) {
                    CountingBot.write(message, "Wrong number!\n" + user.getName() + " messed up after " + BaseSystems.decimalToSystem(lastCount, currentBase) + " (=" + lastCount + ") and lost " + loss + " money.");
                } else {
                    CountingBot.write(message, "Oops!\n" + user.getName() + " counted twice in a row at " + BaseSystems.decimalToSystem(lastCount, currentBase) + " (=" + lastCount + ") and lost " + loss + " money.");
                }
            }
        }
        streakPayout(message, user.getId(), cuckPayout);
        CountingBot.getInstance().save();
    }

    /**
     *
     * @param message
     * @param idOfCuck - nullable if noone cucked!
     * @param cuckPayout - ignored if idOfCuck is null (or not equal to any counter id)
     */
    public void streakPayout(Message message, String idOfCuck, int cuckPayout) {
        // todo: ggf. schreiben wieviel des payouts an contracts gezahlt wurde
        String payoutMessage = "**Streak payouts:**\n";
        boolean someoneWon = false;

        for (String currCounterId : counterIds) {
            Counter counter = CountingBot.getCounter(guildId, currCounterId);
            if (currCounterId.equals(idOfCuck)) {
                if (cuckPayout > 0) {
                    payoutMessage += CountingBot.getCounter(guildId, idOfCuck).getPing() + " receives " + cuckPayout + " money.\n";
                    someoneWon = true;
                }
            } else {
                int wonAmount = counter.getPendingStreakScore(this);
                if (wonAmount > 0) {
                    payoutMessage += (counter.getPing()) + " receives " + wonAmount + " money.\n";
                    someoneWon = true;
                }
                counter.succeed(this, message);
            }
        }

        if (someoneWon) {
            CountingBot.write(message, payoutMessage);
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

    public double getCurrentBonusFactor() {
        if (timeLimitRule != null) {
            return TimeLimitRule.BONUS_FACTOR;
        } else if (slowModeRule != null && slowModeRule.isNewlyAdded() == false) {
            return slowModeRule.getCurrentBonusFactor();
        }
        return 1.0;
    }

    private boolean canBuyDivRule(Counter author, Message message) {
        if (!author.isUnlocked(Unlockable.DIV_RULE)) {
            CountingBot.write(message, "You have to unlock this rule before you can use it.");
            return false;
        }
        int priceInt = (int) (currDivPrice * author.getAddruleDiscountFactor());
        if (!author.canAfford(priceInt)) {
            CountingBot.write(message, "You only have " + author.getScore() + " out of the needed " + priceInt + " money to add this new rule.");
            return false;
        }

        return true;
    }

    private boolean canBuyDigSumRule(Counter author, Message message) {
        if (!author.isUnlocked(Unlockable.DIGSUM_RULE)) {
            CountingBot.write(message, "You have to unlock this rule before you can use it.");
            return false;
        }
        int priceInt = (int) (currDigPrice * author.getAddruleDiscountFactor());
        if (!author.canAfford(priceInt)) {
            CountingBot.write(message, "You only have " + author.getScore() + " out of the needed " + priceInt + " money to add this new rule.");
            return false;
        }

        return true;
    }

    private boolean canBuyRootRule(Counter author, Message message) {
        if (!author.isUnlocked(Unlockable.ROOT_RULE)) {
            CountingBot.write(message, "You have to unlock this rule before you can use it.");
            return false;
        }
        int priceInt = (int) (currRootPrice * author.getAddruleDiscountFactor());
        if (!author.canAfford(priceInt)) {
            CountingBot.write(message, "You only have " + author.getScore() + " out of the needed " + priceInt  + " money to add this new rule.");
            return false;
        }

        return true;
    }

    private boolean canBuySlowmodeRule(Counter author, Message message) {
        if (!author.isUnlocked(Unlockable.SLOWMODE_RULE)) {
            CountingBot.write(message, "You have to unlock this rule before you can use it.");
            return false;
        }
        int priceInt = (int) (currTimePrice * author.getAddruleDiscountFactor());
        if (!author.canAfford(priceInt)) {
            CountingBot.write(message, "You only have " + author.getScore() + " out of the needed " + priceInt  + " money to add this new rule.");
            return false;
        }

        return true;
    }

    private boolean canBuyTimelimitRule(Counter author, Message message) {

        if (!author.isUnlocked(Unlockable.TIMELIMIT_RULE)) {
            CountingBot.write(message, "You have to unlock this rule before you can use it.");
            return false;
        }
        int priceInt = (int) (currTimePrice * author.getAddruleDiscountFactor());
        if (!author.canAfford(priceInt)) {
            CountingBot.write(message, "You only have " + author.getScore() + " out of the needed " + priceInt  + " money to add this new rule.");
            return false;
        }
        return true;
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

        switch (ruleName) {
            case "div":
                if (!canBuyDivRule(author, message)) {
                    break;
                }
                if (splitted.length < 3) {
                    CountingBot.write(message, "Error: Please enter a number!");
                    return;
                }
                int divInDecimal = 0;
                try {
                    String probablyNumber = splitted[2];
                    if(!BaseSystems.isNumInSystem(probablyNumber, currentBase)) {
                        CountingBot.write(message, "Error: Please enter a number in the current base system!");
                        return;
                    }
                    divInDecimal = BaseSystems.toDecimal(probablyNumber, currentBase);
                } catch (NumberFormatException e) {
                    CountingBot.write(message, "Error: Please enter an integer without special characters!");
                    return;
                }
                if (divInDecimal < 2) {
                    CountingBot.write(message, "Error: Please enter an integer greater than 1!");
                    return;
                }
                DividerRule add = new DividerRule(ownerId, divInDecimal, currentBase);
                addNumberRule(add);
                CountingBot.write(message, createYouPaidToAddRuleString(author, currDivPrice, add.toString()));
                author.subtractScore((int) (author.getAddruleDiscountFactor() * currDivPrice));
                currDivPrice += divPriceAdd;
                break;
            case "digsum":
                if (!canBuyDigSumRule(author, message)) {
                    break;
                }
                if (splitted.length < 3) {
                    CountingBot.write(message, "Error: Please enter a number!");
                    return;
                }
                int digsumInDecimal = 0;
                try {
                    String probablyNumber = splitted[2];
                    if(!BaseSystems.isNumInSystem(probablyNumber, currentBase)) {
                        CountingBot.write(message, "Error: Please enter a number in the current base system!");
                        return;
                    }
                    digsumInDecimal = BaseSystems.toDecimal(probablyNumber, currentBase);
                } catch (NumberFormatException e) {
                    CountingBot.write(message, "Error: Please enter an integer without special characters!");
                    return;
                }
                if (digsumInDecimal < 1) {
                    CountingBot.write(message, "Error: Please enter an integer greater than 0!");
                    return;
                }
                DigSumRule addDigSum = new DigSumRule(ownerId, digsumInDecimal, currentBase);
                addNumberRule(addDigSum);
                CountingBot.write(message, createYouPaidToAddRuleString(author, currDigPrice, addDigSum.toString()));
                author.subtractScore((int) (author.getAddruleDiscountFactor() * currDigPrice));
                if(currentBase == 1) {
                    currDigPrice++;
                } else if(currentBase < 5) {
                    currDigPrice *= digPriceLowBaseFact;
                } else {
                    currDigPrice *= digPriceFact;
                }
                break;
            case "root":
                if (!canBuyRootRule(author, message)) {
                    break;
                }
                if (splitted.length < 3) {
                    CountingBot.write(message, "Error: Please enter a number!");
                    return;
                }
                int rootInDecimal = 0;
                try {
                    String probablyNumber = splitted[2];
                    if(!BaseSystems.isNumInSystem(probablyNumber, currentBase)) {
                        CountingBot.write(message, "Error: Please enter a number in the current base system!");
                        return;
                    }
                    rootInDecimal = BaseSystems.toDecimal(probablyNumber, currentBase);
                } catch (NumberFormatException e) {
                    CountingBot.write(message, "Error: Please enter an integer without special characters!");
                    return;
                }
                if (rootInDecimal < 2) {
                    CountingBot.write(message, "Error: Please enter an integer greater than 1!");
                    return;
                }
                RootRule addRootRule = new RootRule(ownerId, rootInDecimal, currentBase);
                addNumberRule(addRootRule);
                CountingBot.write(message, createYouPaidToAddRuleString(author, currRootPrice, addRootRule.toString()));
                author.subtractScore((int) (author.getAddruleDiscountFactor() * currRootPrice));
                break;
            case "slowmode":
                if (!canBuySlowmodeRule(author, message)) {
                    break;
                }
                if (splitted.length < 3) {
                    CountingBot.write(message, "Error: Please enter a number!");
                    return;
                }
                int slow = 0;
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
                if (!canBuyTimelimitRule(author, message)) {
                    break;
                }
                this.timeLimitNewlyAdded = true;
                timeLimitRule = new TimeLimitRule(ownerId, this);

                CountingBot.write(message, createYouPaidToAddRuleString(author, currTimePrice, timeLimitRule.toString()));
                if (slowModeRule != null) {
                    CountingBot.write(message, "This rule is being replaced: " + slowModeRule.toString());
                    slowModeRule = null;
                }
                author.subtractScore((int) (author.getAddruleDiscountFactor() * currTimePrice));
                currTimePrice *= timePriceFact;
                break;
            case "notime":
                if (!author.canAfford(currTimePrice)) {
                    CountingBot.write(message, "You only have " + author.getScore() + " out of the needed " + currTimePrice + " money to remove the current time rule.");
                    break;
                }
                if (slowModeRule != null) {
                    slowModeRule.stop();
                    CountingBot.write(message, "Removed the rule " + slowModeRule.toString() + " for " + currTimePrice + " money.");
                    slowModeRule = null;
                    author.subtractScore((int) (author.getAddruleDiscountFactor() * currTimePrice));
                    currTimePrice *= timePriceFact;
                } else if (timeLimitRule != null) {
                    timeLimitRule.cancel();
                    CountingBot.write(message, "Removed the rule " + timeLimitRule.toString() + " for " + currTimePrice + " money.");
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
        if(addruleDiscount == 1.0) {
            return "You paid " + (int)cost + " to add: " + ruleName;
        } else {
            return "You paid ~~" + (int)cost + "~~ " + (int) (cost * addruleDiscount) + " to add: " + ruleName;
        }
    }

    public void addNumberRule(NumberRule rule) {
        numberRules.add(rule);
    }

    public String getRulesRespond() {
        if (numberRules.isEmpty() && timeLimitRule == null && slowModeRule == null) {
            return "No rules!";
        } else {
            String ret = "Active rules:";
            for (NumberRule rule : numberRules) {
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

    public String getBaseInfoRespond() {
        String ret = "The current base is **" + currentBase + "** with these characters:\n";
        if (currentBase == 1) {
            ret += "1&1";
        } else {
            for (int i = 0; i < currentBase; i++) {
                ret += BaseSystems.digitToChar(i) + " ";
            }
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
}
