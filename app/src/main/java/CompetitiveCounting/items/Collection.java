package CompetitiveCounting.items;

import CompetitiveCounting.CountingBot;
import CompetitiveCounting.items.equippables.Equippable;
import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.List;

public class Collection {
    private final static int DEFAULT_MAX_SIZE = 6;
    private final static double BONUS_PER_ITEM = 0.2;
    private List<Equippable> equippables = new ArrayList<>();
    private int maxSize;

    public Collection() {
        maxSize = DEFAULT_MAX_SIZE;
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

    public double getBonus() {
        return equippables.size() * BONUS_PER_ITEM;
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
                    "to your collection buy calling `~inv use` on them. \nBe ready for a global multiplicative bonus scaling with the amount of unique items in your collection!";
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
        return sb.toString();
    }
}
