package CompetitiveCounting.items.equippables;

import CompetitiveCounting.CountingBot;
import CompetitiveCounting.CountingContext;
import CompetitiveCounting.Price;
import CompetitiveCounting.items.Collection;
import CompetitiveCounting.items.Item;
import discord4j.core.object.entity.Message;

import java.util.Optional;

public abstract class Equippable extends Item {
    public Equippable(Price price, String name, String description) {
        super(price, name, description);
    }

    public abstract String getCollectionDescription();

    public abstract Equippable createObject();

    public void equip(Message message, Collection collection) {
        if (collection.isFull()) {
            CountingBot.write(message, "Your collection is full!"); // todo: wenn es unquip gibt: "Use unequip to remove"
            return;
        }
        if (collection.containsEquippable(this)) {
            CountingBot.write(message, "You have already equipped a " + getName() + "!");
            return;
        }
        collection.addItem(createObject());
        CountingBot.write(message, "You have equipped a " + getName() + "!");
    }

    // Override and return true if collection-usable
    public boolean doCollectionUse(Message message, CountingContext context) {
        return false;
    }
}
