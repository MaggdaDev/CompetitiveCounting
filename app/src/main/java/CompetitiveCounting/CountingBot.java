/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;

import CompetitiveCounting.Parser.TradeOfferParser.TradeOfferChecker;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.Disposable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author DavidPrivat
 */
public class CountingBot {

    private String commandIndicator = "~";
    private Storage storage;
    private HashMap<String, Counter> counters;
    private HashMap<String, CountingStreak> streaks;

    private static CountingBot instance;

    private static int currId = 0;
    private static GatewayDiscordClient client;

    public CountingBot(GatewayDiscordClient client) {
        storage = new Storage();
        this.client = client;
        counters = storage.loadCounters();
        System.out.println(counters);
        streaks = new HashMap<>();
        instance = this;
    }

    public void message(Message message) {
        String content = message.getContent();
        addCounterOptionally(message.getAuthor().get());
        checkCommands(message);

        count(message);

    }

    private void checkCommands(Message message) {
        String content = message.getContent();
        try {
            if (content.startsWith(commandIndicator)) {
                addCounterOptionally(message.getAuthor().get());
                if (content.startsWith(commandIndicator + "help")) {
                    message.getChannel().block().createMessage("tasukete kudasai!\nhttp://hyperlexus.net/competitivecountinghelp.html").subscribe();
                } else if (content.startsWith(commandIndicator + "scoreboard") || content.equals(commandIndicator + "top")) {
                    message.getChannel().block().createMessage(scoreboard()).subscribe();
                } else if (content.startsWith(commandIndicator + "score")) {
                    this.scoreInfo(message);
                } else if (content.startsWith(commandIndicator + "num")) {
                    String channelId = message.getChannelId().asString();
                    if (streaks.containsKey(channelId)) {
                        message.getChannel().block().createMessage("The last number was " + (streaks.get(channelId).getLastNum())).subscribe();
                    } else {
                        message.getChannel().block().createMessage("No current streak! You can start with 1.").subscribe();
                    }
                } else if (content.startsWith(commandIndicator + "addrule")) {
                    addRule(message);
                } else if (content.startsWith(commandIndicator + "rules")) {
                    rules(message);
                } else if (content.startsWith(commandIndicator + "unlock")) {
                    unlock(message);
                } else if (content.equals(commandIndicator + "prestige")) {
                    prestige(message);
                } else if (content.startsWith(commandIndicator + "bonus")) {
                    bonus(message);
                } else if (content.startsWith(commandIndicator + "base")) {
                    baseInfo(message);
                } else if (content.startsWith(commandIndicator + "tradeoffer")) {
                    tradeOffer(message);
                } else if (content.startsWith(commandIndicator + "contracts")) {
                    contractInfo(message);
                } else if (content.startsWith(commandIndicator + "person")) {
                    personInfo(message);
                } else if (content.startsWith(commandIndicator + "fact") || content.startsWith(commandIndicator + "mult")) {
                    factorInfo(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void factorInfo(Message message) {
        String channelId = message.getChannelId().asString();
        Counter author = this.getCounter(this.generateKeyFromUser(message.getAuthor().get()));
        if (streaks.containsKey(channelId)) {
            CountingStreak streak = streaks.get(channelId);
            String msg = "Your collected score in this streak gets a " + Math.round(author.getBonusFact(streak)*100.0)/100.0 + "x bonus multiplier.";
            write(message, msg);
        } else {
            write(message, "There is no active streak in this channel.");
        }
    }

    private void personInfo(Message message) {
        String channelId = message.getChannelId().asString();
        Counter author = this.getCounter(this.generateKeyFromUser(message.getAuthor().get()));
        if (streaks.containsKey(channelId)) {
            Counter lastCounter = streaks.get(channelId).getLastCounter();
            if (lastCounter == null) {
                CountingBot.write(message, "No current streak! You can be the first person to count.");
                return;
            }
            if (lastCounter.getId().equals(author.getId())) {
                CountingBot.write(message, "Attention! You were the last person counting! Don't count now!");
                return;
            } else {
                CountingBot.write(message, lastCounter.getName() + " counted the last number. You can count safely now.");
            }
        } else {
            CountingBot.write(message, "No current streak! You can be the first person to count.");
        }
    }

    private void contractInfo(Message message) {
        Counter author = this.getCounter(this.generateKeyFromUser(message.getAuthor().get()));
        author.contractInfo(message);
    }

    private void tradeOffer(Message message) {
        String content = message.getContent().toUpperCase();
        Counter author = this.getCounter(this.generateKeyFromUser(message.getAuthor().get()));
        if (TradeOfferChecker.isValid(message.getContent(), message)) {
            TradeOffer tradeOffer = new TradeOffer(content, author);
            if (tradeOffer.getRequestedUser() == null) {
                CountingBot.write(message, "This user doesn't seem to have ever counted!");
                return;
            }
            if (tradeOffer.getRequestedUser().getId().equals(author.getId())) {
                CountingBot.write(message, "You can't trade with yourself!");
                return;
            }
            Counter requested = this.getCounter(tradeOffer.getRequestedUserId());
            if (!tradeOffer.isTradeOfferValid(message)) {
                return;
            }
            String tradeId = getNextTradeId();
            Button acceptButton = Button.success(tradeId, "Accept");
            Button declineButton = Button.danger("-" + tradeId, "Decline");
            String cont = tradeOffer.getUserPing() + " do you accept the trade offer?";
            MessageCreateSpec spec = MessageCreateSpec.builder().addComponent(ActionRow.of(List.of(declineButton, acceptButton))).build().withContent(cont);
            message.getChannel().block().createMessage(spec).subscribe();

            requested.addTradeOffer(tradeOffer, tradeId);

        }

    }

    private void baseInfo(Message message) {
        String channelId = message.getChannelId().asString();
        if (streaks.containsKey(channelId)) {
            write(message, streaks.get(channelId).getBaseInfoRespond());
        } else {
            write(message, "There is no selected base yet.");
        }
    }

    private void bonus(Message message) {
        Counter author = this.getCounter(this.generateKeyFromUser(message.getAuthor().get()));
        String[] splitted = message.getContent().split(" ");
        if (splitted.length != 3 || !Util.isNumber(splitted[2])) {
            bonusInfo(message);
            return;
        }
        switch (splitted[1]) {
            case "daily":
                author.bonus(message, BonusStreak.BonusCountType.DAILY, (int) Double.parseDouble(splitted[2]));
                break;
            default:
                bonusInfo(message);
                return;
        }
        CountingBot.getInstance().safeCounters();
    }

    private void bonusInfo(Message message) {
        String answ = "Earn money by getting your daily bonus.\nUsage: '~bonus [type] [count]'\n\nYour streaks so far:";

        Counter author = this.getCounter(this.generateKeyFromUser(message.getAuthor().get()));
        if (author.getBonusStreaks().length == 0) {
            for (BonusStreak.BonusCountType currType : BonusStreak.BonusCountType.values()) {
                author.generateBonusStreak(currType);
            }
        }
        for (BonusStreak curr : author.getBonusStreaks()) {
            int currCount = curr.getCurrCount();
            answ += "\n" + curr.getType().name() + ": ";
            if (currCount == -1) {
                answ += "start your streak with 0!";
            } else {
                switch ((int) curr.getDaysSinceCount()) {
                    case 0:
                        answ += "currently at " + currCount + " (wait " + TimeHandler.timeUntilTomorrowString() + ")";
                        break;
                    case 1:
                        answ += "currently at " + currCount + " and ready for the next number!";
                        break;
                    case 2:
                        answ += "currently at " + currCount + " (expired yesterday; start over again)";
                        break;
                    default:
                        answ += "currently at " + currCount + " (expired " + (curr.getDaysSinceCount() - 1) + " days ago; start over again)";
                }
            }

        }
        CountingBot.write(message, answ);
    }

    private void prestige(Message message) {
        Counter author = this.getCounter(this.generateKeyFromUser(message.getAuthor().get()));
        if (author.prestige(message)) {
            if(author.getPrestiges() < 2) {
            CountingBot.write(message, "GG WP, you just prestiged! You get:\n "
                    + "- 1 prestige point for buying new bases (try out the upgraded ~unlock shop!)\n "
                    + "- a global boost of " + Math.round(Counter.MULT_PLUS_PER_PRESTIGE*100.0d) + "% for counting");
            } else {
                CountingBot.write(message, "GG WP, you prestiged again! You get:\n "
                    + "- 1 additional prestige point for buying new bases\n "
                    + "- an upgrade to your global boost (" + Math.round((author.getPrestiges()-1)*Counter.MULT_PLUS_PER_PRESTIGE*100.0d) + "% => " + Math.round((author.getPrestiges())*Counter.MULT_PLUS_PER_PRESTIGE*100.0d));
            }
            
        } else {
            return;
        }
    }

    private void unlock(Message message) {
        Counter author = this.getCounter(this.generateKeyFromUser(message.getAuthor().get()));
        String[] splitted = message.getContent().split(" ");

        if (author.isUnlocked(Unlockable.UNLOCK_COMMAND)) {
            if (splitted.length != 2 && !(splitted.length == 3 && "base".equals(splitted[1]))) {
                this.unlockInfo(message, author);
                return;
            }
            String toUnlock = splitted[1];
            if (toUnlock.equals("base") && splitted.length == 3) {
                author.unlockBase(message, splitted[2]);
                return;
            }
            for (int i = 0; i < Unlockable.values().length; i++) {
                Unlockable currUnlockable = Unlockable.values()[i];

                if (currUnlockable.getName().equals(toUnlock)) {
                    author.unlock(currUnlockable, message);
                    return;
                }
            }

            CountingBot.write(message, "Error: Invalid unlock!");
        } else {
            if (author.getScore() < Unlockable.UNLOCK_COMMAND.getPrize()) {
                this.unlockInfo(message, author);
                return;
            } else {
                author.unlock(Unlockable.UNLOCK_COMMAND, message);
            }
        }
    }

    private void unlockInfo(Message message, Counter author) {
        if (author.isUnlocked(Unlockable.UNLOCK_COMMAND)) {
            String answ = "Unlock new stuff with the '~unlock' command!\nUsage: ~unlock [unlock name]\n\nYet to unlock:";
            boolean anyUnlockable = false;
            int currCount = 1;
            for (int i = 0; i < Unlockable.values().length; i++) {
                Unlockable currUnlockable = Unlockable.values()[i];
                if (i >= Unlockable.BASE_1.ordinal()) {
                    if (author.getPrestiges() == 0) {
                        continue;
                    }
                }
                if (currUnlockable == Unlockable.BASE_N) {
                    answ += "\n" + String.valueOf(currCount) + ".  '" + currUnlockable.getName() + "': " + currUnlockable.getDescription();
                    answ += " (" + Math.abs(currUnlockable.getPrize()) + " prestige point(s))";
                    anyUnlockable = true;
                } else if (!author.isUnlocked(currUnlockable)) {
                    answ += "\n" + String.valueOf(currCount) + ".  '" + currUnlockable.getName() + "': " + currUnlockable.getDescription();
                    currCount++;
                    if (currUnlockable.getPrize() > 0) {
                        answ += " (" + currUnlockable.getPrize() + " money)";
                    } else {
                        answ += " (" + Math.abs(currUnlockable.getPrize()) + " prestige point(s))";
                    }
                    anyUnlockable = true;
                }

            }
            if (anyUnlockable) {
                CountingBot.write(message, answ);
            } else {
                CountingBot.write(message, "You already own everything!");
            }
        } else {
            CountingBot.write(message, "Unlock the unlock command in order to unlock rules. You have " + (author.getScore()) + " out of the needed " + Unlockable.UNLOCK_COMMAND.getPrize() + ".");
        }
    }

    private void addRule(Message message) {
        String channelID = message.getChannelId().asString();
        if (streaks.containsKey(channelID)) {
            String content = message.getContent();
            streaks.get(channelID).addRule(message, generateKeyFromUser(message.getAuthor().get()));
        } else {
            write(message, "You have to start a streak before you can add rules.");
        }
    }

    private void rules(Message message) {
        String channelId = message.getChannelId().asString();
        if (streaks.containsKey(channelId)) {
            write(message, streaks.get(channelId).getRulesRespond());
        } else {
            write(message, "No rules!");
        }
    }

    private String scoreboard() {
        ArrayList<Counter> countersSorted = new ArrayList<>();
        counters.forEach((String key, Counter counter) -> {
            countersSorted.add(counter);
        });
        countersSorted.sort(new Comparator<Counter>() {
            @Override
            public int compare(Counter arg0, Counter arg1) {    // arg0 > arg1 => 1
                if (arg1.getPrestiges() > arg0.getPrestiges() || (arg1.getPrestiges() == arg0.getPrestiges() && arg1.getPossibleTotal() > arg0.getPossibleTotal())) {
                    return 1;
                } else if (arg1.getPrestiges() == arg0.getPrestiges() && arg1.getPossibleTotal() == arg0.getPossibleTotal()) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        String message = "Scoreboard:";
        for (Counter counter : countersSorted) {
            if (counter.getPrestiges() != 0) {
                message += "\n" + counter.getName() + " : " + counter.getPossibleTotal() + " money (Amount of Prestiges: " + counter.getPrestiges() + ")";
            } else {
                message += "\n" + counter.getName() + " : " + counter.getPossibleTotal() + " money";
            }
        }
        return message;
    }

    private void count(Message message) {
        User user = message.getAuthor().get();
        String channelKey = message.getChannelId().asString();
        String content = message.getContent();
        boolean deleteStreak = false;
        Counter author = null;
        if (counters.containsKey(generateKeyFromUser(user))) {
            author = counters.get(generateKeyFromUser(user));
        }
        if (streaks.containsKey(channelKey)) {
            CountingStreak streak = streaks.get(channelKey);
            if ((!BaseSystems.isNumInSystem(content, streak.getBase()))) {
                return;
            }
            deleteStreak = !streak.count(message, counters.get(generateKeyFromUser(user)), content);
            if(deleteStreak) {
                streak.dispose();
            }
        } else {
            String[] splitted = content.split(" ");
            if (content.equals("1") || (splitted[0].equals("1") && splitted.length == 3 && splitted[1].equals("base") && Util.isNumber(splitted[2]))) {
                if (content.equals("1")) {
                    streaks.put(channelKey, new CountingStreak(channelKey, 10));
                } else {
                    int base = Integer.parseInt(splitted[2]);
                    if(author != null && author.isBaseUnlocked(base)) {
                    streaks.put(channelKey, new CountingStreak(channelKey, base));
                    } else {
                        CountingBot.write(message, "Unlock this base with prestige-points to start a streak.");
                        return;
                    }
                }
                deleteStreak = !streaks.get(channelKey).count(message, counters.get(generateKeyFromUser(user)), splitted[0]);
            }
        }
        if (deleteStreak) {
            streaks.remove(channelKey);
        }

    }

    public void removeStreak(String streakId) {
        streaks.remove(streakId);
    }

    public void safeCounters() {
        storage.safeCounters(counters);
    }

    public static void write(Message message, String s) {
        message.getChannel().block().createMessage(s).subscribe();
    }

    public static void sendDMTo(Counter counter) {
        //client.getUserById(Snowflake.of(counter.getId())).getPrivateChannel().doOnNext((ChannelData channalData)->{
        //    ((ChannelData)channelData).
        //});
    }

    private void scoreInfo(Message message) {
        Counter counter = counters.get(generateKeyFromUser(message.getAuthor().get()));
        String msg;
        if (counter.getPrestiges() == 0) {
            msg = "Your current score is " + counter.getPossibleTotal() + " money (" + counter.getScore() + " in your bank + " + counter.getCurrentScoreAdd() + " possible from current streaks)";
        } else {
            msg = "Your current score is " + counter.getPossibleTotal() + " money (" + counter.getScore() + " in your bank + " + counter.getCurrentScoreAdd() + " possible from current streaks)\nYou have " + counter.getPrestigePoints() + " prestige points";
        }
        CountingBot.write(message, msg);
    }

    private int getScore(User user) {
        int score = counters.get(generateKeyFromUser(user)).getPossibleTotal();
        return score;
    }

    private void addCounterOptionally(User user) {

        String key = generateKeyFromUser(user);
        if (!counters.containsKey(key)) {
            counters.put(key, new Counter(key, user.getUsername(), 0, 0, 0, new int[]{}, new int[]{}, new BonusStreak[]{}));
            storage.safeCounters(counters);
        }

    }

    public Disposable subscribeEmojiReactHandler(EmojiReactHandler handler, String channelId) {
        return client.on(ReactionAddEvent.class).filter(
                (reactionEvent) -> reactionEvent.getChannelId().asString().equals(channelId))
                .doOnNext(handler).subscribe();
    }

    private String generateKeyFromUser(User user) {
        return user.getId().asString();
    }

    public Counter getCounter(String id) {
        return counters.get(id);
    }

    public HashMap<String, Counter> getCounters() {
        return counters;
    }

    public static CountingBot getInstance() {
        return instance;
    }

    public static synchronized String getNextTradeId() {
        currId += 1;
        return String.valueOf(currId);
    }

}
