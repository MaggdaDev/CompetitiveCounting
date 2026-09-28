package competitivecounting.items;

import competitivecounting.Price;
import competitivecounting.items.equippables.Equippables;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Item {
    private final transient Price price;
    private final String name;  // Unique: Used to determine item and class
    private final transient String description;

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

            Item[] extraItems = new Item[] {
                    CrocStonk.instance
            };

            int extraItemsCount = extraItems.length;

            int globalItemsCount = consumablesCount + equippablesCount + extraItemsCount;
            ALL_ITEMS = new Item[globalItemsCount];
            for (int i = 0; i < consumablesCount; i++) {
                ALL_ITEMS[i] = consumables.get(i);
            }
            for (int i = 0; i < equippablesCount; i++) {
                ALL_ITEMS[consumablesCount + i] = equippables.get(i);
            }
            for (int i = 0; i < extraItemsCount; i++) {
                ALL_ITEMS[consumablesCount + equippablesCount + i] = extraItems[i];
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
        return removeEmojis(name);
    }

    public Price getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public String getNameWithoutEmojis() {
        return removeExpliciteEmojis(removeEmojis(name));
    }

    public String getDescription() {
        return description;
    }

    public final static String EMOJI_REGEX = "<:[A-Za-z0-9_]+:\\d+>|[\\x{1F000}-\\x{1FAFF}\\x{2600}-\\x{27BF}\\x{2300}-\\x{23FF}]";
    public static String removeEmojis(String input) {
        return input.replaceAll(EMOJI_REGEX, "");
    }

    public static String removeExpliciteEmojis(String input) {
        return input.replaceAll("<[^>]*>", "").replaceAll(":[^:]*:", "");
    }

    public boolean isMeantBy(String name) {
        return getNameWithoutEmojis().equalsIgnoreCase(removeExpliciteEmojis(removeEmojis(name)));
    }

    public boolean isMeantBy(Item item) {
        return isMeantBy(item.toString());
    }

    public static Item getItemByName(String name) {
        for (Item item : ALL_ITEMS) {
            if (item.isMeantBy(name)) {
                return item;
            }
        }
        return null;
    }
}
