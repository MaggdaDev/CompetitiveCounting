package CompetitiveCounting.dialogue;

import discord4j.core.object.entity.Message;

import java.util.ArrayList;
import java.util.List;

public class Dialogue {
    private int currentState = 0;
    private final List<DialogueElement> elements = new ArrayList<>();
    private final List<Runnable> thenRuns = new ArrayList<>();
    public Dialogue addNpcLine(String text, int readTimeMillis) {
        elements.add(new NpcLine(text, readTimeMillis));
        return this;
    }

    public Dialogue addForAfterDialogue(Runnable runnable) {
        thenRuns.add(runnable);
        return this;
    }

    public void play(Message message) {
        Thread thread = new Thread(() -> {
            while (currentState < elements.size()) {
                DialogueElement element = elements.get(currentState);
                element.run(message);
                currentState++;
            }
            for (Runnable runnable : thenRuns) {
                runnable.run();
            }
        });
        thread.start();
    }
}
