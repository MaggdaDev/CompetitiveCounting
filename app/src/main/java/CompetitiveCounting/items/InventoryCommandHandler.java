package CompetitiveCounting.items;

import CompetitiveCounting.Counter;
import CompetitiveCounting.CountingBot;
import CompetitiveCounting.CountingGuild;
import CompetitiveCounting.CountingStreak;
import discord4j.core.object.entity.Message;

import java.util.HashMap;
import java.util.Optional;

public class InventoryCommandHandler {
    private final static String INV_USE_SYNTAX = "`~inv use <item_name>` or  `~inv use <item_number>`\n-# (items are enumerated in the `~inv` command by their item_number)";
    private final HashMap<String, CountingGuild> guilds;

    public InventoryCommandHandler(HashMap<String, CountingGuild> guilds) {
        this.guilds = guilds;
    }

    public void handleInventoryCommand(Message message, Optional<CountingStreak> nullableStreak) {
        String guildId = message.getGuildId().orElseThrow().asString();
        String authorId = message.getAuthor().orElseThrow().getId().asString();
        Counter counter = guilds.get(guildId).getCounter(authorId);
        String content = message.getContent().substring(1).toLowerCase();
        if (!content.contains(" ") && content.startsWith("inv")) {
            inventory(message, counter);
            return;
        }

        String[] splittedArgs = content.split(" ");
        String command = splittedArgs[1];
        switch (command) {
            case "use":
                if (splittedArgs.length < 3) {
                    CountingBot.write(message, "Usage: " + INV_USE_SYNTAX);
                    return;
                }
                String itemName = content.substring(1 + splittedArgs[0].length() + splittedArgs[1].length()).trim();
                int itemNumber = -1;
                try {
                    int num = Integer.parseInt(itemName);
                    itemNumber = num;
                } catch (NumberFormatException e) {
                    // continue
                }
                Purchasable item;
                if (itemNumber == -1) {
                    // Not a number, maybe a name?
                    if (!Purchasable.isValidPurchasable(itemName)) {
                        CountingBot.write(message, "Invalid item name: " + itemName);
                        return;
                    }
                    item = Purchasable.getPurchasableByString(itemName.toLowerCase());
                } else {
                    if (itemNumber <= 0) {
                        CountingBot.write(message, "Item number must be greater than or equal to 1, but is " + itemNumber +
                                ".\nUse `~inv` to see the available items in your inventory, enumerated by their corresponding number.");
                        return;
                    }
                    if( counter.getInventory().getBoughtItemTypes().length < itemNumber) {
                        CountingBot.write(message, "Item number too large: " + itemNumber + ", you only have " +
                                counter.getInventory().getBoughtItemTypes().length + " different items in your inventory." +
                                ".\nUse `~inv` to see the available items in your inventory, enumerated by their corresponding number.");
                        return;
                    }
                    item = counter.getInventory().getItemByItemNumber(itemNumber);

                }


                if(counter.getInventory().getAmountOfItem(item) <= 0) {
                    CountingBot.write(message, "You don't own this item.");
                    return;
                }
                counter.use(item, message, nullableStreak);
                break;
            default:
                CountingBot.write(message, "Unknown inventory command: " + command);
                break;
        }
    }

    private void inventory(Message message, Counter counter) {
        if (counter.getInventory().getBoughtItemTypes().length == 0) {
            CountingBot.write(message, "Your inventory is empty. You can buy items in the shop using `~shop buy <item>`.");
            return;
        }
        StringBuilder stringBuilder = new StringBuilder("Your inventory:\n\n");
        for(int i = 1; i <= counter.getInventory().getBoughtItemTypes().length; i++) {
            Purchasable item = counter.getInventory().getItemByItemNumber(i);
            int amount = counter.getInventory().getAmountOfItem(item);
            stringBuilder.append(i)
                    .append(") ").append(amount)
                    .append("x ").append(item.getName())
                    .append("\n-# ").append(item.getDescription())
                    .append("\n");
        }
        stringBuilder.append("\nYou can use items with " + INV_USE_SYNTAX);
        CountingBot.write(message, stringBuilder.toString());
    }
}
