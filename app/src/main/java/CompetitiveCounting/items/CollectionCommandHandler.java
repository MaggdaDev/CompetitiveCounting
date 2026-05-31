package CompetitiveCounting.items;

import CompetitiveCounting.Counter;
import CompetitiveCounting.CountingBot;
import discord4j.core.object.entity.Message;

public class CollectionCommandHandler {
    public final static String COMMAND_INDICATOR = "col";

    public static void handleCollectionCommand(Message message) {
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
            default:
                CountingBot.write(message, "Unknown collection action!");
        }

    }

    private static void collection(Message message, Counter counter) {
        CountingBot.write(message, counter.getCollection().toString());

    }
}
