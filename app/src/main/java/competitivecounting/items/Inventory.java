package competitivecounting.items;

import competitivecounting.items.equippables.Equippables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Inventory {
    private boolean isShopUnlocked = false;

    private final HashMap<String, Integer> itemsBoughtAmount;

    public Inventory() {
        itemsBoughtAmount = new HashMap<>();
    }

    public void initialize() {
        HashMap<String, Integer> newMap = new HashMap<>();
        for (String itemName : itemsBoughtAmount.keySet()) {
            Item item = Item.getItemByName(Item.removeExpliciteEmojis(itemName));
            if (item == null) {
                System.err.println("Found item with illegal name in inventory: " + itemName + "-> delete");
                continue;
            }
            int value = itemsBoughtAmount.get(itemName);
            newMap.put(item.toString(), value);
        }
        itemsBoughtAmount.clear();
        itemsBoughtAmount.putAll(newMap);
    }

    public boolean isShopUnlocked() {
        return isShopUnlocked;
    }

    public void setShopUnlocked(boolean shopUnlocked) {
        isShopUnlocked = shopUnlocked;
    }

    public void addItem(Item toBuy) {
        if (itemsBoughtAmount.containsKey(toBuy.toString())) {
            itemsBoughtAmount.put(toBuy.toString(), itemsBoughtAmount.get(toBuy.toString()) + 1);
        } else {
            itemsBoughtAmount.put(toBuy.toString(), 1);
        }
    }

    public void addItems(Item toBuy, int amount) {
        if (itemsBoughtAmount.containsKey(toBuy.toString())) {
            itemsBoughtAmount.put(toBuy.toString(), itemsBoughtAmount.get(toBuy.toString()) + amount);
        } else {
            itemsBoughtAmount.put(toBuy.toString(), amount);
        }
    }

    public void removeItems(Item toRemove, int amount) {
        String key = toRemove.toString();
        if (amount > itemsBoughtAmount.getOrDefault(key, 0)) {
            throw new IllegalStateException("Contract tries to remove more items than available in inventory: " + amount + " > " + getAmountOfItem(toRemove));
        }
        if (itemsBoughtAmount.containsKey(key)) {
            itemsBoughtAmount.put(key, itemsBoughtAmount.get(key) - amount);
        }
    }

    public Item[] getBoughtItemTypes() {
        List<Item> list = new ArrayList<>();
        for (Item item : Item.ALL_ITEMS) {
            if (getAmountOfItem(item) > 0) {
                list.add(item);
            }
        }
        return list.toArray(new Item[0]);
    }

    public int getAmountOfItem(Item item) {
        if (item == null) {
            return 0;
        }
        return itemsBoughtAmount.getOrDefault(item.toString(), 0);
    }

    public void removeItem(Item item) {
        String itemId = item.toString();
        itemsBoughtAmount.put(itemId, getAmountOfItem(item) - 1);
    }

    /**
     *
     * @param itemNumber beginning with 1!!!
     * @return
     */
    public Item getItemByItemNumber(int itemNumber) {
        return getBoughtItemTypes()[itemNumber - 1];
    }

}
