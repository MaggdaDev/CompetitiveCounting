package competitivecounting.dialogue;

import competitivecounting.CountingBot;
import discord4j.core.object.entity.Message;

import java.util.Optional;
import java.util.function.Function;

public class NpcLine extends DialogueElement{
    private final String text;
    private final int sleepDuration;

    private Message sentMessage;
    private final Function<String, String> npcLineConverter;

    public NpcLine(String text, int readTimeMillis, Function<String, String> npcLineConverter) {
        this.text = text;
        this.sleepDuration = readTimeMillis;
        this.npcLineConverter = npcLineConverter;
    }

    @Override
    public void run(Message message) {
        sentMessage = CountingBot.writeBlocking(message, npcLineConverter == null ? text : npcLineConverter.apply(text));
        try {
            Thread.sleep(sleepDuration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Message> getNewMessage() {
        return Optional.of(sentMessage);
    }

    public String getSentMessageId() {
        return sentMessage.getId().asString();
    }
}
