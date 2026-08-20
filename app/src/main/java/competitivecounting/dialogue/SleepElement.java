package competitivecounting.dialogue;

import discord4j.core.object.entity.Message;

public class SleepElement extends DialogueElement {
    private final int timespan;

    public SleepElement(int timespan) {
        this.timespan = timespan;
    }

    @Override
    public void run(Message message) {
        try {
            Thread.sleep(timespan * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
