package CompetitiveCounting.dialogue;

import CompetitiveCounting.EmojiReactHandler;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class Dialogue {
    private int currentState = 0;
    private final List<DialogueElement> elements = new ArrayList<>();
    private final List<Runnable> thenRuns = new ArrayList<>();
    private final ArrayList<Integer> npcMessagesIndices = new ArrayList<>();
    private Thread thread;

    public Dialogue addNpcLine(String text, int readTimeMillis) {
        elements.add(new NpcLine(text, readTimeMillis));
        npcMessagesIndices.add(elements.size() - 1);
        return this;
    }

    public Dialogue addRunnable(Consumer<Message> runnable) {
        elements.add(new DialogRunnable(runnable));
        return this;
    }

    public Dialogue addWaitForEmojiReactionOnNthNpcLine(int n, String string, ReactionEmoji emoji) {
        if (n >= npcMessagesIndices.size()) {
            throw new IllegalArgumentException("n is larger than the number of NPC lines added so far!");
        }
        int index = npcMessagesIndices.get(n);
        elements.add(new EmojiReactionSubscriber(() -> ((NpcLine) elements.get(index)).getSentMessageId(), emoji));
        return this;
    }

    public Dialogue addForAfterDialogue(Runnable runnable) {
        thenRuns.add(runnable);
        return this;
    }

    public void play(Message message) {
        playAtIndex(message, 0);
    }

    public void playAtIndex(Message message, int idx) {
        currentState = idx;
        thread = new Thread(() -> {
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

    public void stop() {
        currentState = elements.size(); // Mark the dialogue as finished
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        } else {
            System.err.println("Trying to stop a dialogue that is not running or has already finished.");
        }
        for (DialogueElement element : elements) {
            element.dispose();
        }
    }


}
