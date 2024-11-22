/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;

import CompetitiveCounting.Rules.*;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.Disposable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author DavidPrivat
 */
public class CountingStreak {

    private final String key;
    private final Disposable emojiReactSubscription;
    private int counter, lastCount;
    private Counter lastCounter;
    private HashMap<String, Counter> counters;
    private ArrayList<NumberRule> numberRules;
    private SlowModeRule slowModeRule;
    private TimeLimitRule timeLimitRule;
    private boolean timeLimitNewlyAdded = false;
    private int currDivPrize = 50, divPrizeAdd = 50;
    private int currDigPrize = 100, digPrizeFact = 2;
    private int currRootPrize = 150;
    private int currTimePrize = 250, timePrizeFact = 2;
    private int currentBase;

    private final TrophyHandler trophyHandler;

    private final EmojiReactHandler emojiReactHandler;

    public CountingStreak(String key, int base) {
        this.key = key;
        counter = 1;
        lastCount = 0;
        counters = new HashMap<>();
        lastCounter = null;
        numberRules = new ArrayList<>();
        currentBase = base;
        emojiReactHandler = new EmojiReactHandler(key);
        emojiReactSubscription = CountingBot.getInstance().subscribeEmojiReactHandler(emojiReactHandler, key);
        trophyHandler = new TrophyHandler(emojiReactHandler);
    }

    public boolean count(Message message, Counter user, String content) {
        if (!counters.containsKey(user.getId())) {
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

        if (isNumCorrect(number, message) && (!user.equals(lastCounter))) { // Count is accepted
            lastCount = number;
            incrementCounter();
            user.notifyCount(number, this);

            trophyHandler.considerSpawningTrophy(number, message, user);

            if (slowModeRule != null) {
                slowModeRule.applyTimerToMessage(message);
            } else if (timeLimitRule != null && (!timeLimitNewlyAdded)) {
                timeLimitRule.applyTimerToMessage(message, lastCounter);
            } else {
                if (user.hasTrophy(number)) {
                    message.addReaction(Emojis.GOLDEN_KEKMARK).subscribe();
                } else {
                    message.addReaction(Emojis.KEKMARK).subscribe();
                }
            }
            if (timeLimitNewlyAdded) {
                CountingBot.write(message, "Watch out! The next number will activate the timelimit countdown!\n" + user.getPing() + " be ready to keep counting!");
                timeLimitNewlyAdded = false;
            }
            lastCounter = user;
            return true;
        } else {
            fail(message, number, user);

            return false;
        }
    }

    private void addCounter(Counter add, Message message) {
        counters.put(add.getId(), add);
        add.addStreakToCurrAdd(this);
        if (!add.isBaseUnlocked(currentBase)) {
            CountingBot.write(message, add.getPing() + " you haven't unlocked this base yet, so you will only get " + Counter.SYSTEM_NOT_OWNED_FACT + "x score");
        }
    }

    private void fail(Message message, int number, Counter user) {
        message.addReaction(ReactionEmoji.unicode("\u274C")).subscribe();
        Rule winnerRule = getWinnerRule(number);
        if ((!(winnerRule instanceof TimeLimitRule)) && timeLimitRule != null) {
            timeLimitRule.cancel();
        }
        String winnerName = "";
        if (winnerRule != null) {
            int loss = 0;
            String causeForLose = "Wrong number";
            if (winnerRule instanceof SlowModeRule) {
                causeForLose = "Slowmode-rule broken";
            } else if (winnerRule instanceof TimeLimitRule) {
                causeForLose = "Timelimit-rule broken";
            }
            if (winnerRule.getOwnerId().equals(user.getId())) {
                loss = user.failFromOwn(message, this);
                if (currentBase == 10) {
                    CountingBot.write(message, causeForLose + "!\n" + user.getName() + " messed up after " + lastCount + " due to his own rule '" + winnerRule.toString() + "' and lost " + loss + " money.");
                } else {
                    CountingBot.write(message, causeForLose + "!\n" + user.getName() + " messed up after " + BaseSystems.decimalToSystem(lastCount, currentBase) + " (=" + lastCount + ") due to his own rule '" + winnerRule.toString() + "' and lost " + loss + " money.");
                }
            } else {
                loss = user.fail(message, this);
                if (currentBase == 10) {
                    CountingBot.write(message, causeForLose + "!\n" + user.getName() + " messed up after " + lastCount + " and lost " + loss + " money.");
                } else {
                    CountingBot.write(message, causeForLose + "!\n" + user.getName() + " messed up after " + BaseSystems.decimalToSystem(lastCount, currentBase) + " (=" + lastCount + ") and lost " + loss + " money.");
                }

                int win = (int) (loss);
                winnerName = counters.get(winnerRule.getOwnerId()).getName();
                CountingBot.write(message, winnerName + " has pulled a fast one on " + user.getName() + " with their '" + winnerRule.toString() + "' rule and got all of the victim's lost money, which is " + win + ".");
                counters.get(winnerRule.getOwnerId()).notifyWin(win, currentBase, message);
            }

        } else {
            int loss = user.fail(message, this);
            if (currentBase == 10) {
                if (!user.equals(lastCounter)) {
                    CountingBot.write(message, "Wrong number!\n" + user.getName() + " messed up after " + lastCount + " and lost " + loss + " money.");
                } else {
                    CountingBot.write(message, "Oops!\n" + user.getName() + " counted twice in a row at " + lastCount + " and lost " + loss + " money.");
                }
            } else {
                if (!user.equals(lastCounter)) {
                    CountingBot.write(message, "Wrong number!\n" + user.getName() + " messed up after " + BaseSystems.decimalToSystem(lastCount, currentBase) + " (=" + lastCount + ") and lost " + loss + " money.");
                } else {
                    CountingBot.write(message, "Oops!\n" + user.getName() + " counted twice in a row at " + BaseSystems.decimalToSystem(lastCount, currentBase) + " (=" + lastCount + ") and lost " + loss + " money.");
                }
            }
        }

        counters.forEach((String key, Counter counter) -> {
            if (!key.equals(user.getId())) {
                counter.succeed(this, message);
            }
        });
        CountingBot.getInstance().safeCounters();
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
        if (currDivPrize > author.getScore()) {
            CountingBot.write(message, "You only have " + author.getScore() + " out of the needed " + currDivPrize + " money to add this new rule.");
            return false;
        }

        return true;
    }

    private boolean canBuyDigSumRule(Counter author, Message message) {
        if (!author.isUnlocked(Unlockable.DIGSUM_RULE)) {
            CountingBot.write(message, "You have to unlock this rule before you can use it.");
            return false;
        }
        if (currDigPrize > author.getScore()) {
            CountingBot.write(message, "You only have " + author.getScore() + " out of the needed " + currDigPrize + " money to add this new rule.");
            return false;
        }

        return true;
    }

    private boolean canBuyRootRule(Counter author, Message message) {
        if (!author.isUnlocked(Unlockable.ROOT_RULE)) {
            CountingBot.write(message, "You have to unlock this rule before you can use it.");
            return false;
        }
        if (currRootPrize > author.getScore()) {
            CountingBot.write(message, "You only have " + author.getScore() + " out of the needed " + currRootPrize + " money to add this new rule.");
            return false;
        }

        return true;
    }

    private boolean canBuySlowmodeRule(Counter author, Message message) {
        if (!author.isUnlocked(Unlockable.SLOWMODE_RULE)) {
            CountingBot.write(message, "You have to unlock this rule before you can use it.");
            return false;
        }
        if (currTimePrize > author.getScore()) {
            CountingBot.write(message, "You only have " + author.getScore() + " out of the needed " + currTimePrize + " money to add this new rule.");
            return false;
        }

        return true;
    }

    private boolean canBuyTimelimitRule(Counter author, Message message) {

        if (!author.isUnlocked(Unlockable.TIMELIMIT_RULE)) {
            CountingBot.write(message, "You have to unlock this rule before you can use it.");
            return false;
        }
        if (currTimePrize > author.getScore()) {
            CountingBot.write(message, "You only have " + author.getScore() + " out of the needed " + currTimePrize + " money to add this new rule.");
            return false;
        }
        return true;
    }

    private void addRuleInfo(Message message, Counter author) {
        String answer = "You can choose to add the following rules:\n";
        boolean anyRule = false;
        if (timeLimitRule != null || slowModeRule != null) {
            answer += "\n'notime': Remove the current slowmode/timelimit (cost: " + currTimePrize + ")";
            anyRule = true;
        }
        if (author.isUnlocked(Unlockable.DIV_RULE)) {
            answer += "\n'div': Numbers with the divisor n have to be skipped. (cost: " + currDivPrize + ")";
            anyRule = true;
        }
        if (author.isUnlocked(Unlockable.ROOT_RULE)) {
            answer += "\n'root': Numbers which have an integer nth root must be skipped. (cost: " + currRootPrize + ")";
            anyRule = true;
        }
        if (author.isUnlocked(Unlockable.DIGSUM_RULE)) {
            answer += "\n'digsum': Numbers with digsum n must be skipped. (cost: " + currDigPrize + ")";
            anyRule = true;
        }
        if (author.isUnlocked(Unlockable.SLOWMODE_RULE)) {
            answer += "\n'slowmode': A certain time n has to pass between counts. (cost " + currTimePrize + ")";
            anyRule = true;
        }
        if (author.isUnlocked(Unlockable.TIMELIMIT_RULE)) {
            answer += "\n'timelimit': The next number must have been counted before 10s have passed. (cost: " + currTimePrize + ")";
            anyRule = true;
        }
        answer += "\n\n syntax: '~addrule [name] ([argument])";
        if (anyRule) {
            CountingBot.write(message, answer);
        } else {
            CountingBot.write(message, "Unlock rules with ~unlock!");
        }
    }

    public void addRule(Message message, String ownerId) {
        if (!counters.containsKey(ownerId)) {
            addCounter(CountingBot.getInstance().getCounter(ownerId), message);
        }
        String content = message.getContent();
        String[] splitted = content.split(" ");
        Counter author = counters.get(ownerId);

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
                NumberRule.DividerRule add = new NumberRule.DividerRule(ownerId, divInDecimal, currentBase);
                addNumberRule(add);
                CountingBot.write(message, "You paid " + currDivPrize + " to add: " + add.toString());
                author.subtractScore(currDivPrize);
                currDivPrize += divPrizeAdd;
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
                CountingBot.write(message, "You paid " + currDigPrize + " to add: " + addDigSum.toString());
                author.subtractScore(currDigPrize);
                currDigPrize *= digPrizeFact;
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
                CountingBot.write(message, "You paid " + currRootPrize + " to add: " + addRootRule.toString());
                author.subtractScore(currRootPrize);
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
                if (slow < 1) {
                    CountingBot.write(message, "Error: Please enter an integer greater than 0!");
                    return;
                }
                slowModeRule = new SlowModeRule(slow, ownerId);
                CountingBot.write(message, "You paid " + currTimePrize + " to add: " + slowModeRule.toString());
                if (timeLimitRule != null) {
                    CountingBot.write(message, "This rule is being replaced: " + timeLimitRule.toString());
                    timeLimitRule = null;
                }
                author.subtractScore(currTimePrize);
                currTimePrize *= timePrizeFact;
                break;
            case "timelimit":
                if (!canBuyTimelimitRule(author, message)) {
                    break;
                }
                this.timeLimitNewlyAdded = true;
                timeLimitRule = new TimeLimitRule(ownerId, this);

                CountingBot.write(message, "You paid " + currTimePrize + " to add: " + timeLimitRule.toString());
                if (slowModeRule != null) {
                    CountingBot.write(message, "This rule is being replaced: " + slowModeRule.toString());
                    slowModeRule = null;
                }
                author.subtractScore(currTimePrize);
                currTimePrize *= timePrizeFact;
                break;
            case "notime":
                if (author.getScore() < currTimePrize) {
                    CountingBot.write(message, "You only have " + author.getScore() + " out of the needed " + currTimePrize + " money to remove the current time rule.");
                    break;
                }
                if (slowModeRule != null) {
                    slowModeRule.stop();
                    CountingBot.write(message, "Removed the rule " + slowModeRule.toString() + " for " + currTimePrize + " money.");
                    slowModeRule = null;
                    author.subtractScore(currTimePrize);
                    currTimePrize *= timePrizeFact;
                } else if (timeLimitRule != null) {
                    timeLimitRule.cancel();
                    CountingBot.write(message, "Removed the rule " + timeLimitRule.toString() + " for " + currTimePrize + " money.");
                    timeLimitRule = null;
                    author.subtractScore(currTimePrize);
                    currTimePrize *= timePrizeFact;
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

    public void addNumberRule(NumberRule rule) {
        numberRules.add(rule);
    }

    public String getRulesRespond() {
        if (numberRules.isEmpty() && timeLimitRule == null && slowModeRule == null) {
            return "No rules!";
        } else {
            String ret = "Active rules:";
            for (NumberRule rule : numberRules) {
                ret += "\n\t-" + rule.toString();
            }
            if (slowModeRule != null) {
                ret += "\n\t-" + slowModeRule.toString();
            }
            if (timeLimitRule != null) {
                ret += "\n\t-" + timeLimitRule.toString();
            }
            return ret;
        }
    }

    public String getBaseInfoRespond() {
        String ret = "Current base: " + currentBase;
        ret += "\nWith the characters:\n";
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
        return lastCounter;
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
        emojiReactSubscription.dispose();
    }
}
