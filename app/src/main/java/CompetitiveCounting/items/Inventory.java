package CompetitiveCounting.items;

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

    public void buy(Purchasable toBuy) {
        if (itemsBoughtAmount.containsKey(toBuy.toString())) {
            itemsBoughtAmount.put(toBuy.toString(), itemsBoughtAmount.get(toBuy.toString()) + 1);
        } else {
            itemsBoughtAmount.put(toBuy.toString(), 1);
        }
    }

    public Purchasable[] getBoughtItemTypes() {
        List<Purchasable> list = new ArrayList<>();
        for (Purchasable purchasable : Purchasable.values()) {
            if (getAmountOfItem(purchasable) > 0) {
                list.add(purchasable);
            }
        }
        return list.toArray(new Purchasable[0]);
    }

    public int getAmountOfItem(Purchasable purchasable) {
        return itemsBoughtAmount.getOrDefault(purchasable.toString(), 0);
    }

    public void removeItem(Purchasable item) {
        String itemId = item.toString();
        itemsBoughtAmount.put(itemId, getAmountOfItem(item) - 1);
    }
}
