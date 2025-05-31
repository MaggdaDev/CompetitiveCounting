package CompetitiveCounting.dialogue;

import CompetitiveCounting.CountingBot;
import discord4j.core.object.entity.Message;

public class NpcLine extends DialogueElement{
    private final String text;
    private final int sleepDuration;

    public NpcLine(String text, int readTimeMillis) {
        this.text = text;
        this.sleepDuration = readTimeMillis;
    }

    @Override
    public void run(Message message) {
        CountingBot.write(message, text);
        try {
            Thread.sleep(sleepDuration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
