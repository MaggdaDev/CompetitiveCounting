package competitivecounting.items.equippables;

import competitivecounting.*;
import discord4j.core.object.entity.Message;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

public class GoodBadUgly extends Equippable {
    public static final String NAME = "The\uD83D\uDE07Good, The\uD83D\uDE08Bad and The\uD83D\uDC79Ugly";
    private static final String description = "When equipped, money gained from counting will be partially redistributed. Can be switched into three different modes.";
    private final static double BASE_GOOD_GIVE_RATE = 0.2;
    private final static double BAD_STEAL_RATE = 0.2;
    private final static double UGLY_MOVE_RATE = 0.4;

    private int state = -1;
    private final static String[] STATE_NAMES = new String[]{
            "The\uD83D\uDE07Good",
            "The\uD83D\uDE08Bad",
            "The\uD83D\uDC79Ugly"
    };


    private int[] statsTracker = {0,0,0};

    private class StreakDependentState {
        private transient int uglyStealStash = 0;
        private transient boolean affectNextCount = false;
        private transient String previouslyAffectedCounterId = "";
    }

    private transient HashMap<String, StreakDependentState> streakDependentStateHashMap = new HashMap<>();

    public final static String[] STATE_DESCRIPTIONS = new String[]{
            "When counting a number, you will receive the usual amount of money. The previous counter (and, if it is a different person, " +
                    "also the counter after you) will be granted an additional sum equal to {0}% of the score that you got.\n{1}\n-# Money generated: {2}",
            "When counting a number, you steal {0}% of the score that the previous counter (and, if it is a different counter, also the counter after you) get for their count.\n{1}\n-# Money stolen: {2}",
            "When counting a number, you steal {0}% of the score that the previous counter got for their count, but all of the stolen money will be given to the counter after you.\n{1}\n-# Money moved: {2}"
    };

    @Override
    public void initialize(Counter owner) {
        super.initialize(owner);
        if (statsTracker == null) {
            statsTracker = new int[]{0,0,0};
        }
        if (streakDependentStateHashMap == null) {
            streakDependentStateHashMap = new HashMap<>();
        }
    }

    public GoodBadUgly(Counter owner) {
        super(null, NAME, description, owner);
    }

    @Override
    public String getCollectionDescription() {
        double baseRate, modifiedRate;
        if (state == 0) {
            baseRate = BASE_GOOD_GIVE_RATE;
            modifiedRate = getGoodGiveRate();
        } else if (state == 1) {
            baseRate = BAD_STEAL_RATE;
            modifiedRate = getBadStealRate();
        } else {
            baseRate = UGLY_MOVE_RATE;
            modifiedRate = getUglyMoveRate();
        }
        int basePerc = (int) Math.round(baseRate * 100.);
        int modifiedPerc = (int) Math.round(modifiedRate * 100.);
        return STATE_DESCRIPTIONS[state]
                .replace("{1}", "_Use_ to cycle through different modes!")
                .replace("{2}", String.valueOf(statsTracker[state]))
                .replace("{0}", Util.valueAndValueWithBoniToString(basePerc, modifiedPerc));
    }

    @Override
    public Equippable createObject(Counter owner) {
        GoodBadUgly ret = new GoodBadUgly(owner);
        ret.state++;
        return ret;
    }

    @Override
    public boolean doCollectionUse(Message message, CountingContext context) {
        if (state < 0) {
            state = 0;
        }
        int oldState = state;
        String oldName = getName();
        state = (state + 1) % 3;
        String s = "You switched your " + oldName + " to a " + getName() + "!";
        CountingBot.write(message, s);
        return true;
    }

    @Override
    public String getName() {
        return state < 0 ? NAME : STATE_NAMES[state] + (level > 1 ? " Mk. " + BaseSystems.intToRoman(level) : "");
    }

    @Override
    public void performPassiveAfterCounterReceivesMoney(Message message, CountingContext context, int scoreAdd) {
        String streakKey = context.getStreak().getKey();
        StreakDependentState ss = streakDependentStateHashMap.computeIfAbsent(streakKey, k -> new StreakDependentState());
        Optional<Counter> previousCounter = Optional.ofNullable(CountingBot.getCounter(context.getStreak().getGuildId(), context.getLastCounterId()));
        if (state == 0) {   // Good
            if (context.getCounter() == owner) {
                previousCounter.ifPresent(preCounter -> {
                    int addMoney = (int) (scoreAdd * getGoodGiveRate());
                    preCounter.addScoreAddForStreak(addMoney, streakKey);
                    statsTracker[state] += addMoney;
                    ss.previouslyAffectedCounterId = preCounter.getId();
                });
                ss.affectNextCount = true;
            } else if (ss.affectNextCount) {
                ss.affectNextCount = false;
                if (!Objects.equals(context.getCounter().getId(), ss.previouslyAffectedCounterId)) {
                    int addMoney = (int) (context.getLastScoreAdd() * getGoodGiveRate());
                    context.getCounter().addScoreAddForStreak(addMoney, streakKey);
                    statsTracker[state] += addMoney;
                }
                ss.previouslyAffectedCounterId = "";
            }
        } else if (state == 1) { // Bad
            if (context.getCounter() == owner) {
                previousCounter.ifPresent(prevCounter -> {
                    int moneyToSteal = (int) (context.getLastScoreAdd() * getBadStealRate());
                    if(stealIfPossible(prevCounter, moneyToSteal, streakKey, ss)) {
                        ss.affectNextCount = true;
                        statsTracker[state] += moneyToSteal;
                    }
                });
            } else if (ss.affectNextCount) {
                ss.affectNextCount = false;
                if (!Objects.equals(context.getCounter().getId(), ss.previouslyAffectedCounterId)) {
                    int moneyToSteal = (int) (scoreAdd * getBadStealRate());
                    if(stealIfPossible(context.getCounter(), moneyToSteal, streakKey, ss)) {
                        statsTracker[state] += moneyToSteal;
                    }
                }
            }
        } else if (state == 2) { // Ugly
            if (context.getCounter() == owner) {
                previousCounter.ifPresent(prevCounter -> {
                    int moneyToSteal = (int) (context.getLastScoreAdd() * getUglyMoveRate());
                    if (stealIfPossible(prevCounter, moneyToSteal, streakKey, ss)) {
                        ss.uglyStealStash = moneyToSteal;
                        ss.affectNextCount = true;
                        ss.previouslyAffectedCounterId = prevCounter.getId();
                    } else {
                        ss.uglyStealStash = 0;
                        ss.affectNextCount = false;
                    }
                });
            } else if (ss.affectNextCount) {
                ss.affectNextCount = false;
                if (ss.uglyStealStash != 0) {
                    context.getCounter().addScoreAddForStreak(ss.uglyStealStash, streakKey);
                    if (!context.getCounter().getId().equals(ss.previouslyAffectedCounterId)) {
                        statsTracker[state] += ss.uglyStealStash;  // Only add to moved money stat if not moved back to the same counter
                    }
                    ss.uglyStealStash = 0;
                }
            }
        }
    }

    private boolean stealIfPossible(Counter toBeStolenFrom, int stealAmount, String streakKey, StreakDependentState ss) {
        if (toBeStolenFrom.removeScoreAddForStreak(stealAmount, streakKey)) {
            ss.previouslyAffectedCounterId = toBeStolenFrom.getId();
            owner.addScoreAddForStreak(stealAmount, streakKey);
            return true;
        }
        return false;
    }

    @Override
    public void streakDisposed(CountingStreak streak) {
        streakDependentStateHashMap.remove(streak.getKey());
    }

    private double getGoodGiveRate() {
        return flatIncreaseStatFromLevel(BASE_GOOD_GIVE_RATE);   // may exceed 100%
    }

    private double getBadStealRate() {
        return modifyProbabilityFromLevel(BAD_STEAL_RATE);   // must not exceed 100%
    }

    private double getUglyMoveRate() {
        return modifyProbabilityFromLevel(UGLY_MOVE_RATE);   // must not exceed 100%
    }
}

