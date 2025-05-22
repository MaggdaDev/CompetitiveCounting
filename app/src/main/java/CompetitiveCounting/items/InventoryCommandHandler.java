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
