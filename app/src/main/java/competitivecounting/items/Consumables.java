package competitivecounting.items;

import competitivecounting.Price;

public class Consumables {
    public final static Item HAND_BAG = new Item(new Price(1050505),
            "Glamorous crocodile-leather Lacoste purse",
                     "A crocodile-branded handbag made of the finest crocodile leather. Truly an accessory made for kings."),
    FAKE_HAND_BAG = new Item(new Price(1050505), "Cheap plastic-leather Lakosde purse",
                          "A crocodile-branded handbag made of some leather from Turkey."),
    WHITE_STREAK_ENDER = new Item(new Price(20000), "Streak⚪Ender",
                               "If the majority of contributing counters agrees, this item is consumed, the streak ends and everyone gets their payout."),
    PRIME_COIN = new Item(null, "Prime:coin:Coin",
            "When consumed, you obtain money equal to the current worth of one bit coin in US dollars.");
}
