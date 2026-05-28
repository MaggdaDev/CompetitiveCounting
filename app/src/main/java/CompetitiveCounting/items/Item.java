package CompetitiveCounting.items;

import CompetitiveCounting.Price;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Item {
    private final Price price;
    private final String name;
    private final String description;

    public static Item[] ALL_ITEMS;

    static {
        try {
            List<Item> consumables = new ArrayList<>();
            for (Field field : Consumables.class.getDeclaredFields()) {
                consumables.add((Item)field.get(null));
            }
            int globalItemsCount = consumables.size();
            ALL_ITEMS = new Item[globalItemsCount];
            for (int i = 0; i < consumables.size(); i++) {
                ALL_ITEMS[i] = consumables.get(i);
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
