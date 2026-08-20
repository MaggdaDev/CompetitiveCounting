package competitivecounting.items;

import competitivecounting.*;
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
        if (commandContent.equals("shop") || commandContent.equals("shop buy")) {
            shop(message);
            return;
        }

        if (commandContent.contains(" ")) {
            String[] splittedArgs = commandContent.split(" ");
            switch (splittedArgs[1]) {
                case "buy":
                    if(splittedArgs.length < 3 || commandContent.length() <= 9) {
                        shop(message);
                        return;
                    }
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
            return;
        }
        if (!counter.canAfford(UNLOCK_COMMAND_USAGE_PRICE)) {
            CountingBot.write(message, "You need to pay " + UNLOCK_COMMAND_USAGE_PRICE + " money to unlock the shop, but you currently only have " + counter.getScore() + " money.");
            return;
        }
        counter.subtractScore(UNLOCK_COMMAND_USAGE_PRICE);
        counter.getInventory().setShopUnlocked(true);
        boolean isBankUnlocked = guilds.get(message.getGuildId().get().asString()).getBank().isUnlocked();
        String unlock_shop_message = "You paid " + UNLOCK_COMMAND_USAGE_PRICE + " and unlocked the shop! Use ~shop to browse for a variety of useful items. Bought items can be viewed using `" + "~inv" + "`.";
        unlock_shop_message += isBankUnlocked ? "" : "\n-# Legends say that one of these items could change your server forever...";
        CountingBot.write(message, unlock_shop_message);
        CountingBot.getInstance().save();
    }

    private void shop(Message message) {
        StringBuilder sb = new StringBuilder();
        sb.append("Welcome to the shop! The items you can find here are: \n\n");
        int itemCounter = 1;
        for (Item item : Purchasables.PURCHASABLE_ITEMS) {
            sb.append(itemCounter + ") " + item.getName())
                    .append(" - ")
                    .append(item.getPrice())
                    .append("\n");
            itemCounter += 1;
        }
        sb.append("\nTo buy an item, use `~shop buy <item name>` or `~shop buy <item number>`.\n");
        CountingBot.write(message, sb.toString());
    }

    public void buy(Message message, String itemToBuy, Counter counter) {
        if (!Purchasables.isValidPurchasable(itemToBuy)) {
            CountingBot.write(message, "This item doesn't exist! Please try again.");
            return;
        }
        Item toBuy = Purchasables.getPurchasableByNameOrNumber(itemToBuy);
        if (!counter.canAfford(toBuy.getPrice())) {
            CountingBot.write(message, "This item is too expensive for you! You only have " + counter.getScore() + " out of the needed " + toBuy.getPrice() + " money.");
            return;
        }
        counter.subtractScore(toBuy.getPrice());
        if (toBuy == Consumables.HAND_BAG) {
            CountingBot.getInstance().handBagBought(message);
        } else {
            counter.getInventory().addItem(toBuy);
            writeItemBoughtMessage(message, toBuy);
            CountingBot.getInstance().save();
        }
    }

    private void writeItemBoughtMessage(Message message, Item item) {
        CountingBot.write(message, "You have bought a *" + item.getName() + "* and paid " + item.getPrice() + ". Use `" + "~inv" + "` to check out your inventory!");
    }

    public void acquireHandBag(Message message, String guildId, String counterId) {
        CountingGuild guild = guilds.get(guildId);
        Counter counter = guild.getCounter(counterId);
        counter.getInventory().addItem(Consumables.FAKE_HAND_BAG);
        writeItemBoughtMessage(message, Consumables.FAKE_HAND_BAG);
    }

    private void shopNotUnlocked(Message message) {
        CountingBot.write(message, "You haven't unlocked the shop yet! You need to use the unlock_shop command first.\nIn the shop, you can buy some useful items! (more will be added later™...)");
    }



}
