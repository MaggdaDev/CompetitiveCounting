package CompetitiveCounting.items;

import CompetitiveCounting.Price;
import CompetitiveCounting.items.equippables.Equippables;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Item {
    private final Price price;
    private final String name;
    private final String description;

    public static Item[] ALL_ITEMS;

    public static void initializeItems() {
        try {
            // consumables
            List<Item> consumables = new ArrayList<>();
            for (Field field : Consumables.class.getDeclaredFields()) {
                consumables.add((Item)field.get(null));
            }
            // equippables
            List<Item> equippables = new ArrayList<>();
            for (Field field : Equippables.class.getDeclaredFields()) {
                equippables.add((Item)field.get(null));
            }

            int consumablesCount = consumables.size();
            int equippablesCount = equippables.size();
            int globalItemsCount = consumablesCount + equippablesCount;
            ALL_ITEMS = new Item[globalItemsCount];
            for (int i = 0; i < consumablesCount; i++) {
                ALL_ITEMS[i] = consumables.get(i);
            }
            for (int i = 0; i < equippablesCount; i++) {
                ALL_ITEMS[consumablesCount + i] = equippables.get(i);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize items", e);
        }
    }

    public Item(Price price, String name, String description) {
        this.price = price;
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString() {
        return name;
    }

    public Price getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
