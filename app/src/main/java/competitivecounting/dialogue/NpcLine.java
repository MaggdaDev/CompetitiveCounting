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
    private boolean useMessageAsCurrentMessage;
    public NpcLine(String text, int readTimeMillis, Function<String, String> npcLineConverter, boolean useMessageAsCurrentMessage) {
        this.text = text;
        this.sleepDuration = readTimeMillis;
        this.npcLineConverter = npcLineConverter;
        this.useMessageAsCurrentMessage = useMessageAsCurrentMessage;
    }

    @Override
    public void run(Message message) {
        try {
            sentMessage = CountingBot.writeBlocking(message, npcLineConverter == null ? text : npcLineConverter.apply(text));
            Thread.sleep(sleepDuration);
        } catch (InterruptedException e) {
            cancelRemainingElements();
            System.out.println("Sleep of npc line (" + text + ") interrupted: " + e.getMessage());
        }
    }

    @Override
    public Optional<Message> getNewMessage() {
        return useMessageAsCurrentMessage?Optional.of(sentMessage):Optional.empty();
    }

    public String getSentMessageId() {
        return sentMessage.getId().asString();
    }
}
