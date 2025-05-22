package CompetitiveCounting.items;

import CompetitiveCounting.Counter;
import CompetitiveCounting.CountingBot;
import CompetitiveCounting.CountingGuild;
import CompetitiveCounting.Unlockable;
import discord4j.core.object.entity.Message;

import java.util.HashMap;

public class ShopCommandHandler {
    public final static int UNLOCK_COMMAND_USAGE_PRICE = 20000;
    private final HashMap<String, CountingGuild> guilds;

    public ShopCommandHandler(HashMap<String, CountingGuild> guilds) {
        this.guilds = guilds;
    }

    public void handleShopCommand(Message message) {
        if (message.getGuildId().isEmpty()) {
            CountingBot.write(message, "This command can only be used in a server.");
        }
        String guildId = message.getGuildId().get().asString();
        String authorId = message.getAuthor().get().getId().asString();
        Counter counter = guilds.get(guildId).getCounter(authorId);
        String commandContent = message.getContent().toLowerCase().substring(1);
        if (commandContent.equals("unlock_shop")) {
            unlockShop(message, counter);
            return;
        }
        if (!counter.isShopUnlocked()) {
            shopNotUnlocked(message);
            return;
        }
        if (commandContent.equals("shop")) {
            shop(message);
            return;
        }

        if (commandContent.contains(" ")) {
            String[] splittedArgs = commandContent.split(" ");
            switch (splittedArgs[1]) {
                case "buy":
                    buy(message, commandContent.substring(9), counter);
                    return;
                default:
                    CountingBot.write(message, "Unknown shop action!");
            }
        }


    }

    private void unlockShop(Message message, Counter counter) {
        if (!counter.isUnlocked(Unlockable.UNLOCK_SHOP)) {
            CountingBot.write(message, "You need to unlock the unlock_shop command to unlock the shop. Use ~unlock unlock_shop to unlock the unlock_shop command.");
            return;
        }
        if (counter.getInventory().isShopUnlocked()) {
            CountingBot.write(message, "You have already unlocked the shop. There's nothing left to do for the unlock_shop command.");
        }
        if (!counter.canAfford(UNLOCK_COMMAND_USAGE_PRICE)) {
            CountingBot.write(message, "You need to pay " + UNLOCK_COMMAND_USAGE_PRICE + " money to unlock the shop, but you currently only have " + counter.getScore() + " money. \n(This money adds to your net worth needed to prestige)");
        }
        counter.subtractScore(UNLOCK_COMMAND_USAGE_PRICE);
        counter.getInventory().setShopUnlocked(true);
        CountingBot.write(message, "You paid " + UNLOCK_COMMAND_USAGE_PRICE + " and unlocked the shop! Use ~shop to browse for a variety of useful items.\n(This money adds to your net worth needed to prestige)");
        CountingBot.getInstance().save();
    }

    private void shop(Message message) {
        StringBuilder sb = new StringBuilder();
        sb.append("Welcome to the shop! The items you can find here are: \n\n");
        for (Purchasable item : Purchasable.values()) {
            sb.append(item.getName())
                    .append(" - ")
                    .append(item.getPrice())
                    .append(" money")
                    .append("\n");
        }
        CountingBot.write(message, sb.toString());
    }

    public static boolean isValidPurchasable(String itemToBuy) {
        for (Purchasable item : Purchasable.values()) {
            if (item.getName().equalsIgnoreCase(itemToBuy)) {
                return true;
            }
        }
        return false;
    }

    public void buy(Message message, String itemToBuy, Counter counter) {
        if (!isValidPurchasable(itemToBuy)) {
            CountingBot.write(message, "This item doesn't exist! Please try again.");
            return;
        }
        Purchasable toBuy = Purchasable.getPurchasableByLowerCaseName(itemToBuy);
        if (!counter.canAfford(toBuy.getPrice())) {
            CountingBot.write(message, "This item is too expensive for you! You only have " + counter.getScore() + " out of the needed " + toBuy.getPrice() + " money.");
            return;
        }
        counter.subtractScore(toBuy.getPrice());
        counter.getInventory().buy(toBuy);
        CountingBot.write(message, "You have bought a *" + toBuy.getName() + "* and paid " + toBuy.getPrice() + " money.");
        CountingBot.getInstance().save();
    }

    private void shopNotUnlocked(Message message) {
        CountingBot.write(message, "You haven't unlocked the shop yet! You need to use the unlock_shop command first.");
    }


}
