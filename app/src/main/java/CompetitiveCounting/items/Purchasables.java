package CompetitiveCounting.items;

import CompetitiveCounting.items.equippables.Equippables;

import java.util.HashMap;
public class Purchasables {
    public final static Item[] PURCHASABLE_ITEMS = {
            Consumables.WHITE_STREAK_ENDER,
            Consumables.HAND_BAG,
            Equippables.VAULT_LOCATOR
    };

    public static Item getPurchasableByNameOrNumber(String itemAsNameOrNumber) {
        try {
            int number = Integer.parseInt(itemAsNameOrNumber);
            if (1 <= number && number <= PURCHASABLE_ITEMS.length) {
                return PURCHASABLE_ITEMS[number - 1];
            }
        } catch(NumberFormatException e) {
            // continue
        }
        for (Item item : PURCHASABLE_ITEMS) {
            if (item.getName().equalsIgnoreCase(itemAsNameOrNumber)) {
                return item;
            }
        }
        return null;
    }

    public static boolean isValidPurchasable(String itemAsNameOrNumber) {
        try {
            int number = Integer.parseInt(itemAsNameOrNumber);
            return  1 <= number && number <= PURCHASABLE_ITEMS.length;
        } catch(NumberFormatException e) {
            // continue
        }
        for (Item item : PURCHASABLE_ITEMS) {
            if (item.getName().equalsIgnoreCase(itemAsNameOrNumber)) {
                return true;
            }
        }
        return false;
    }
}
