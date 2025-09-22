package CompetitiveCounting.items;

import CompetitiveCounting.Counter;
import CompetitiveCounting.CountingBot;
import CompetitiveCounting.CountingGuild;
import discord4j.core.object.entity.Message;

import java.util.HashMap;

public class InventoryCommandHandler {
    private final HashMap<String, CountingGuild> guilds;

    public InventoryCommandHandler(HashMap<String, CountingGuild> guilds) {
        this.guilds = guilds;
    }

    public void handleInventoryCommand(Message message) {
        String guildId = message.getGuildId().orElseThrow().asString();
        String authorId = message.getAuthor().orElseThrow().getId().asString();
        Counter counter = guilds.get(guildId).getCounter(authorId);
        String content = message.getContent().substring(1).toLowerCase();
        if (!content.contains(" ") && content.startsWith("inv")) {
            inventory(message, counter);
            return;
        }
        if (!content.contains(" ")) {
            return; // Todo write shit
        }
        String[] splittedArgs = content.split(" ");
        String command = splittedArgs[1];
        switch (command) {
            case "use":
                if (splittedArgs.length < 3) {
                    CountingBot.write(message, "Usage: ~inv use <item>");
                    return;
                }
                String itemName = content.substring(1 + splittedArgs[0].length() + splittedArgs[1].length()).trim();
                if (!Purchasable.isValidPurchasable(itemName)) {
                    CountingBot.write(message, "Invalid item name: " + itemName);
                    return;
                }
                Purchasable item = Purchasable.getPurchasableByString(itemName.toLowerCase());
                if(counter.getInventory().getAmountOfItem(item) <= 0) {
                    CountingBot.write(message, "You don't own this item.");
                    return;
                }
                counter.use(item, message);
                break;
            default:
                CountingBot.write(message, "Unknown inventory command: " + command);
                break;
        }
    }

    private void inventory(Message message, Counter counter) {
        StringBuilder stringBuilder = new StringBuilder("Your inventory:\n");
        for (Purchasable item : counter.getInventory().getBoughtItemTypes()) {
            stringBuilder.append(counter.getInventory().getAmountOfItem(item))
                    .append("x ")
                    .append(item.getName())
                    .append(": ")
                    .append(item.getDescription())
                    .append("\n");
        }
        CountingBot.write(message, stringBuilder.toString());
    }
}
