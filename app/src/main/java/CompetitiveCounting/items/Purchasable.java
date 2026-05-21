package CompetitiveCounting.items;

import java.util.HashMap;

public enum Purchasable {
    HAND_BAG(1050505, "Glamorous crocodile-leather Lacoste purse", "A crocodile-branded handbag made of the finest crocodile leather. Truly an accessory made for kings."),
    FAKE_HAND_BAG(1050505, "Cheap plastic-leather Lakosde purse", "A crocodile-branded handbag made of some leather from Turkey.");


    public final static Purchasable[] BUYABLES = {
            HAND_BAG
    };
    private final static HashMap<String, Purchasable> purchasablesByLowerCaseName;
    static {
        purchasablesByLowerCaseName = new HashMap<>();
        for (Purchasable purchasable : Purchasable.values()) {
            purchasablesByLowerCaseName.put(purchasable.getName().toLowerCase(), purchasable);
        }
    }
    public static Purchasable getPurchasableByString(String name) {
        try {
            int number = Integer.parseInt(name);
            return Purchasable.BUYABLES[number - 1];
        } catch (NumberFormatException e) {
            // continue
        }
        return purchasablesByLowerCaseName.get(name);
    }
    private final int price;
    private final String name;
    private final String description;
    Purchasable(int price, String name, String description) {
        this.price = price;
        this.name = name;
        this.description = description;
    }

    public static boolean isValidPurchasable(String itemToBuy) {
        try {
            int number = Integer.parseInt(itemToBuy);
            return  1 <= number && number <= Purchasable.BUYABLES.length;
        } catch(NumberFormatException e) {
            // continue
        }
        for (Purchasable item : Purchasable.values()) {
            if (item.getName().equalsIgnoreCase(itemToBuy)) {
                return true;
            }
        }
        return false;
    }

    public int getPrice() { return price; }

    public String getName() { return name; }
    public String getDescription() { return description; }
}
