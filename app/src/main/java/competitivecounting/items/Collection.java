package competitivecounting.items;

import competitivecounting.Counter;
import competitivecounting.CountingBot;
import competitivecounting.CountingContext;
import competitivecounting.CountingStreak;
import competitivecounting.dialogue.Dialogue;
import competitivecounting.items.equippables.*;
import com.google.common.base.Objects;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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
        getEquippable(item).ifPresentOrElse(
                Equippable::upgrade,
                () -> equippables.add(item)
        );
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
        for (Item item : equippables) {
            if (item.isMeantBy(equippable)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        if (getEquippables().isEmpty()) {
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

    public Optional<Equippable> getEquippable(Equippable eq) {
        for (Equippable equippable : equippables) {
            if (equippable.isMeantBy(eq)) {
                return Optional.of(equippable);
            }
        }
        return Optional.empty();
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
                if (equippable.isMeantBy(itemIdentifier)) {
                    return Optional.of(equippable);
                }
            }
        }
        return Optional.empty();
    }

    public Mono<Boolean> equipAsync(Message message, Equippable equipableTemplate) {
        if (!checkEquipability(message, equipableTemplate)) {
            return Mono.just(false);
        }
        Dialogue d = new Dialogue();
        if (containsEquippable(equipableTemplate)) {
            Equippable equippedEquippable = getEquippable(equipableTemplate).orElseThrow();
            d.addNpcLine("Your collection already contains a " + equippedEquippable.getName()
                            + "! Do you want to upgrade it by one level?", 0)
                    .addSinglePersonThumbsUpDownConfirmation(
                            m -> {
                            },
                            m -> CountingBot.write(message, "You have declined to upgrade your " + equippedEquippable.getName() + "."),
                            true, new AtomicReference<>(owner.getId()),
                            30,
                            m2 -> {
                                CountingBot.write(message, "Upgrading your " + equippedEquippable.getName()
                                        + " timed out, " + owner.getName() + ".");
                                return true;
                            });
        } else {
            d.addNpcLine("New item! Do you want to extend your collection with a " + equipableTemplate.getName()
                            + "? This action cannot be reverted.", 0)
                    .addSinglePersonThumbsUpDownConfirmation(
                            m -> {
                            },
                            m -> CountingBot.write(message, "You have declined to add the " + equipableTemplate.getName() + " to your collection."),
                            true, new AtomicReference<>(owner.getId()),
                            30,
                            m2 -> {
                                CountingBot.write(message, "Adding the " + equipableTemplate.getName()
                                        + " to your collection timed out, " + owner.getName() + ".");
                                return true;
                            });
        }
        d.addRunnable(m -> {
                    if (checkEquipability(message, equipableTemplate)) {
                        boolean upgraded = containsEquippable(equipableTemplate);
                        String oldName = upgraded ?
                                getEquippable(equipableTemplate).orElseThrow().getName() : equipableTemplate.getName();
                        addItem(equipableTemplate.createObject(owner));
                        owner.getInventory().removeItem(equipableTemplate);
                        Equippable equippedEquippable = getEquippable(equipableTemplate).orElseThrow();
                        if (upgraded) {
                            CountingBot.write(message, "You have upgraded your " + oldName + " to a " + equippedEquippable.getName() + "!");
                        } else {
                            CountingBot.write(message, "You have added a " + equippedEquippable.getName() + " to your collection!");
                        }
                        CountingBot.getInstance().save();
                    }
                })
                .play(message);
        return null;
    }

    private boolean checkEquipability(Message message, Equippable equippable) {
        if (isFull()) {
            CountingBot.write(message, "Your collection is full!");
            return false;
        }
        if (owner.getInventory().getAmountOfItem(equippable) <= 0) {
            CountingBot.write(message, "You don't own a " + equippable.getName() + "!");
            return false;
        }
        return true;
    }


    public void streakDisposed(CountingStreak streak) {
        equippables.forEach(e -> e.streakDisposed(streak));
    }

    public double modifyTrophyRateFromEquippables(double trophyChance, CountingContext context) {
        int number = context.getCurrentNumber();
        // Dowsing Rod
        Optional<Equippable> maybeDowsingRod = getEquippable(Equippables.DOWSING_ROD);
        if (maybeDowsingRod.isPresent()) {
            trophyChance = ((DowsingRod) maybeDowsingRod.get()).modifyTrophyRate(trophyChance, number);
        }

        // rest
        for (Equippable equippable : equippables) {
            if (equippable instanceof TrophyRateModifier && !(equippable instanceof DowsingRod)) {  // TODO also dowsing rod
                trophyChance = ((TrophyRateModifier) equippable).modifyTrophyRate(trophyChance, context);
            }
        }

        return trophyChance;
    }

    public double modifyVaultRateFromEquippables(double vaultChance, CountingContext context) {
        for (Equippable equippable : equippables) {
            if (equippable instanceof VaultRateModifier) {
                vaultChance = ((VaultRateModifier) equippable).modifyVaultRate(vaultChance, context);
            }
        }
        return vaultChance;
    }
}
