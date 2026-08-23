package competitivecounting.items;

import competitivecounting.items.equippables.Equippables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Inventory {
    private boolean isShopUnlocked = false;

    private HashMap<String, Integer> itemsBoughtAmount;

    public Inventory() {
        itemsBoughtAmount = new HashMap<>();
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

    public Item[] getBoughtItemTypes() {
        List<Item> list = new ArrayList<>();
        for (Item item : Item.ALL_ITEMS) {
            System.out.println("Checking item " + item );
            if (getAmountOfItem(item) > 0) {
                list.add(item);
            }
        }
        return list.toArray(new Item[0]);
    }

    public int getAmountOfItem(Item item) {
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
