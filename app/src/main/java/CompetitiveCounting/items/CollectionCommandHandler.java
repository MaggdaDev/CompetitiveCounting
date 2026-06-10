package CompetitiveCounting.items;

import CompetitiveCounting.Counter;
import CompetitiveCounting.CountingBot;
import CompetitiveCounting.CountingStreak;
import CompetitiveCounting.items.equippables.Equippable;
import CompetitiveCounting.items.equippables.Equippables;
import discord4j.core.object.entity.Message;

import java.util.Optional;

public class CollectionCommandHandler {
    public final static String COMMAND_INDICATOR = "col";

    public static void handleCollectionCommand(Message message, Optional<CountingStreak> streak) {
        String guildId = message.getGuildId().orElseThrow().asString();
        String authorId = message.getAuthor().orElseThrow().getId().asString();
        Counter counter = CountingBot.getCounter(guildId, authorId);
        String content = message.getContent().substring(1).toLowerCase();
        if (!content.contains(" ") && content.startsWith("col")) {
            collection(message, counter);
            return;
        }

        String[] splittedArgs = content.split(" ");
        String command = splittedArgs[1];
        switch (command) {
            case "use":
                if (splittedArgs.length < 3) {
                    CountingBot.write(message, "Please specify an item to use!");
                    return;
                }
                String itemIdentifier = content.substring(content.indexOf("use") + 4);
                counter.getCollection().getEquippableByNameOrNumber(itemIdentifier).ifPresentOrElse((eq) -> {
                    if (streak.isEmpty()) {
                        CountingBot.write(message, "You can only use items during a counting streak!");
                        return;
                    }
                    if (!eq.doCollectionUse(message, streak.get().getLastContext())) {
                        CountingBot.write(message, "A " + eq.getName() + " cannot be actively used!");
                    };
                }, ()-> CountingBot.write(message, "Cannot find your specified equippable."));
                break;
            default:
                CountingBot.write(message, "Unknown collection action!");
        }

    }

    private static void collection(Message message, Counter counter) {
        CountingBot.write(message, counter.getCollection().toString());

    }
}
