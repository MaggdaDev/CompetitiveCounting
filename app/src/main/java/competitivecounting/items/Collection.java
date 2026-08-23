package competitivecounting.items;

import competitivecounting.Counter;
import competitivecounting.CountingBot;
import competitivecounting.CountingStreak;
import competitivecounting.items.equippables.DowsingRod;
import competitivecounting.items.equippables.Equippable;
import com.google.common.base.Objects;
import competitivecounting.items.equippables.Equippables;
import discord4j.core.object.entity.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Collection {
    private final static int DEFAULT_MAX_SIZE = 6;
    private final static double BONUS_PER_ITEM = 0.2;
    private List<Equippable> equippables = new ArrayList<>();
    private int maxSize;
    private transient Counter owner;

    public Collection(Counter owner) {
        maxSize = DEFAULT_MAX_SIZE;
        initialize(owner);
    }

    public void initialize(Counter owner) {
        this.owner = owner;
        equippables.forEach(eq -> eq.initialize(owner));
    }

    public void addItem(Equippable item) {
        equippables.add(item);
    }

    public List<Equippable> getEquippables() {
        return equippables;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public double getBonusFact() {
        return 1.0 + equippables.size() * BONUS_PER_ITEM;
    }

    public boolean isFull() {
        return equippables.size() >= maxSize;
    }

    public boolean containsEquippable(Equippable equippable) {
        for (Item item: equippables) {
            if (Objects.equal(item.getName(), equippable.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        if (getEquippables().size() == 0) {
            return "Your collection is empty! You can add up to " + getMaxSize() + " different equippables " +
                    "to your collection by calling `~inv use` on them. \nBe ready for a global multiplicative bonus scaling with the amount of unique items in your collection!";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Your collection:\n\n");
        for (int i = 0; i < getMaxSize(); i++) {
            if (i < getEquippables().size()) {
                Equippable eq = getEquippables().get(i);
                sb.append(i + 1)
                        .append(") ")
                        .append(eq.getName())
                        .append(": ")
                        .append(eq.getCollectionDescription())
                        .append("\n");
            } else {
                sb.append(i + 1).append(") ").append("___empty___").append("\n");
            }
        }
        sb.append("\n-# Hint: Some equippables can be _used_ with `~col use <item number or name>`.");
        return sb.toString();
    }

    public Equippable getEquippable(Equippable eq) {
        for (Equippable equippable : equippables) {
            if (Objects.equal(equippable.getName(), eq.getName())) {
                return equippable;
            }
        }
        throw new IllegalArgumentException("Equippable not found in collection: " + eq.getName());
    }

    public Optional<Equippable> getEquippableByNameOrNumber(String itemIdentifier) {
        try {
            int index = Integer.parseInt(itemIdentifier) - 1;
            if (index >= 0 && index < equippables.size()) {
                return Optional.of(equippables.get(index));
            }
        } catch (NumberFormatException e) {
            // Not a number, try by name
            for (Equippable equippable : equippables) {
                if (equippable.getName().equalsIgnoreCase(itemIdentifier)) {
                    return Optional.of(equippable);
                }
            }
        }
        return Optional.empty();
    }

    public void equip(Message message, Equippable equippable) {
        if (isFull()) {
            CountingBot.write(message, "Your collection is full!"); // todo: wenn es unquip gibt: "Use unequip to remove"
            return;
        }
        if (containsEquippable(equippable)) {
            CountingBot.write(message, "You have already equipped a " + equippable.getName() + "!");
            return;
        }
        addItem(equippable.createObject(owner));
        CountingBot.write(message, "You have equipped a " + equippable.getName() + "!");
    }


    public void streakDisposed(CountingStreak streak) {
        equippables.forEach(e -> e.streakDisposed(streak));
    }

    public double modifyTrophyRateFromEquippables(double trophyChance, int number) {
        // Dowsing Rod
        Optional<Equippable> maybeDowsingRod = getEquippableByNameOrNumber(Equippables.DOWSING_ROD.getName());
        if (maybeDowsingRod.isPresent()) {
            trophyChance = ((DowsingRod) maybeDowsingRod.get()).modifyTrophyRate(trophyChance, number);
        }

        return trophyChance;
    }
}
