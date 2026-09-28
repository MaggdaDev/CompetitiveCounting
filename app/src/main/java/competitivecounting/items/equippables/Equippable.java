package competitivecounting.items.equippables;

import competitivecounting.*;
import competitivecounting.items.Item;
import discord4j.core.object.entity.Message;

public abstract class Equippable extends Item {
    protected transient Counter owner;
    protected int level;

    public final static double ADDITIONAL_FACTOR_PER_LEVEL = 0.1;


    public Equippable(Price price, String name, String description, Counter owner) {
        super(price, name, description);
        initialize(owner);
        level = 1;
    }

    public void initialize(Counter owner) {
        this.owner = owner;
        if (level <= 0) {
            level = 1;
        }
    }

    public abstract String getCollectionDescription();

    public abstract Equippable createObject(Counter owner);



    // Override and return true if collection-usable
    public boolean doCollectionUse(Message message, CountingContext context) {
        return false;
    }


    public void performPassiveAfterCounterReceivesMoney(Message message, CountingContext context, int scoreAdd) {
        // Empty
    }

    public void streakDisposed(CountingStreak streak) {
        // Empty
    }

    @Override
    public String getName() {
        return super.getName() + (level > 1 ? " Mk. " + BaseSystems.intToRoman(level) : "");
    }

    public int getLevel() {
        return level;
    }

    public void upgrade() {
        level++;
    }

    protected double flatIncreaseStatFromLevel(double baseStat) {
        return baseStat * getMultiplierFromLevel();
    }

    protected double decreaseInverseStatFromLevel(double baseStat) {
        return baseStat / getMultiplierFromLevel();
    }

    protected double modifyProbabilityFromLevel(double baseProbability) {
        return Util.multiplyProbabilityThreshold(baseProbability, getMultiplierFromLevel());
    }

    protected double getMultiplierFromLevel() {
        return 1. + (level - 1.) * ADDITIONAL_FACTOR_PER_LEVEL;
    }
}
