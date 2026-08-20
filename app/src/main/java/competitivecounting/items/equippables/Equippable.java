package competitivecounting.items.equippables;

import competitivecounting.*;
import competitivecounting.items.Item;
import discord4j.core.object.entity.Message;

public abstract class Equippable extends Item {
    protected transient Counter owner;
    public Equippable(Price price, String name, String description, Counter owner) {
        super(price, name, description);
        initialize(owner);
    }

    public void initialize(Counter owner) {
        this.owner = owner;
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
}
