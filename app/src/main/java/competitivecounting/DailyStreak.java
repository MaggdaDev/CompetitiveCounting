package competitivecounting;

import competitivecounting.items.Consumables;
import competitivecounting.items.Item;
import discord4j.core.object.entity.Message;

import java.time.LocalDate;
import java.time.ZoneId;

public class DailyStreak {
    private int currentCount;
    private long lastDaySinceEpochCounted;
    private boolean messedUpPreviousDay;
    private final static ZoneId TIMEZONE = ZoneId.of("Europe/Berlin");

    private final static int BONUS_PER_DAY = 500;

    public DailyStreak() {
        currentCount = 0;
        lastDaySinceEpochCounted = 0;
        messedUpPreviousDay = false;
    }

    public int increment() {
        currentCount++;
        lastDaySinceEpochCounted = getCurrentDaySinceEpoch();
        return currentCount * BONUS_PER_DAY;
    }

    public void fixedDailyStreakToday() {
        messedUpPreviousDay = false;
    }

    public boolean canIncrement() {
        return lastDaySinceEpochCounted < getCurrentDaySinceEpoch();
    }

    private long getCurrentDaySinceEpoch() {
        return LocalDate.now(TIMEZONE).toEpochDay();
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public void setCountedTodayWithoutIncrementing() {  // cucked streak today
        lastDaySinceEpochCounted = getCurrentDaySinceEpoch();
        messedUpPreviousDay = true;
    }

    public void addDailyScoreAndGift(Message message, Counter counter) {
        int count;
        try {
            count = getCountFromDailyMessage(message);
        } catch (NumberFormatException e) {
            if (getCurrentCount() == 0) {
                CountingBot.write(message, "Start a daily streak using `~daily 1`!");
            } else {
                if (canIncrement()) {
                    CountingBot.write(message, "Your daily streak is at " + getCurrentCount() + " and ready for the next count. Use `~daily <count>`!");
                } else {
                    alreadyCountedTodayMessage(message);
                }
            }
            return;
        }

        if (canIncrement()) {
            if (count != getCurrentCount() + 1) {
                CountingBot.write(message, "Wrong count! The last count was " + getCurrentCount() + ". Try again tomorrow!");
                setCountedTodayWithoutIncrementing();
                return;
            }
            int daysSkipped = (int)(getCurrentDaySinceEpoch() - lastDaySinceEpochCounted - 1);
            int baseScoreAdd = increment();
            int scoreAddWithBonus = (int) (counter.getStreakIndependentBonusFact() * baseScoreAdd);

            Item item = getDailyGiftItem();

            String scoreAddedMsg;
            String loot = "{0} money" + (item != null ? " and a " + item.getName() : "");

            if (getCurrentCount() == 1) {
                scoreAddedMsg = "You started a daily streak and received " + loot + ". Come back tomorrow for the next reward!";
            } else if (daysSkipped == 0) {
                if (messedUpPreviousDay) {
                    scoreAddedMsg = "Nice, you received " + loot + " from getting back on track with your daily streak. Come back tomorrow for the next reward!";
                    fixedDailyStreakToday();
                } else {
                    scoreAddedMsg = "Good job, you received " + loot + " from your daily streak without missing a day. Come back tomorrow for the next reward!";
                }
            } else {
                scoreAddedMsg = "You received " + loot + " from your daily streak after " + daysSkipped + " inactive day" + (daysSkipped>1? "s":"") + ". Come back tomorrow for the next reward!";
            }
            CountingBot.writeBlocking(message, scoreAddedMsg.replace("{0}", Util.valueAndValueWithBoniToString(baseScoreAdd, scoreAddWithBonus)));
            counter.addBonusScore(scoreAddWithBonus, message);
            if (item != null) {
                counter.getInventory().addItem(item);
            }

        } else {
            alreadyCountedTodayMessage(message);
        }
    }

    public Item getDailyGiftItem() {
        return Consumables.COUNTING_BOOSTER;
    }

    private void alreadyCountedTodayMessage(Message message) {
        CountingBot.write(message, "You have already counted in your daily streak today. Come back tomorrow!");
    }

    private int getCountFromDailyMessage(Message message) throws NumberFormatException {
        String content = message.getContent();
        if (!content.contains(" ")) {
            throw new NumberFormatException();
        }
        String[] parts = content.split(" ");
        if (parts.length != 2) {
            throw new NumberFormatException();
        }
        return Integer.parseInt(parts[1]);
    }
}
