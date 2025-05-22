package CompetitiveCounting.items;

import java.util.HashMap;

public enum Purchasable {
    HAND_BAG(2000000, "Glamorous crocodile-leather Lacoste purse", "A crocodile-branded handbag made of the finest leather in Turkey. Truly an accessory made for kings.");

    private final static HashMap<String, Purchasable> purchasablesByLowerCaseName;
    static {
        purchasablesByLowerCaseName = new HashMap<>();
        for (Purchasable purchasable : Purchasable.values()) {
            purchasablesByLowerCaseName.put(purchasable.getName().toLowerCase(), purchasable);
        }
    }
    public static Purchasable getPurchasableByLowerCaseName(String name) {
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

    public int getPrice() { return price; }

    public String getName() { return name; }
    public String getDescription() { return description; }
}
