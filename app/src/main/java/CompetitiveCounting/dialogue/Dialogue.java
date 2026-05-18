package CompetitiveCounting.dialogue;

import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class Dialogue {
    private int currentState = 0;
    private final List<DialogueElement> elements = new ArrayList<>();
    private final List<Runnable> thenRuns = new ArrayList<>();
    private final ArrayList<Integer> npcMessagesIndices = new ArrayList<>();
    private Thread thread;
    private Function<String, String> npcLineConverter;

    public Dialogue addNpcLine(String text, int readTimeMillis) {
        elements.add(new NpcLine(text, readTimeMillis, str -> npcLineConverter != null ? npcLineConverter.apply(str) : str));
        npcMessagesIndices.add(elements.size() - 1);
        return this;
    }

    public Dialogue addRunnable(Consumer<Message> runnable) {
        elements.add(new DialogRunnable(runnable));
        return this;
    }

    public ParallelDialogElementsBuilder initializeParallelDialogElements() {
        ParallelDialogElementsBuilder builder = new ParallelDialogElementsBuilder(this);
        return builder;
    }

    public Dialogue addWaitForEmojiReaction(ReactionEmoji emoji, boolean cancelRemainingDialogueOnReact,
                                            Consumer<Message> onReactCallback, Optional<String> counterIdRestriction) {
        elements.add(new EmojiReactionSubscriber(emoji, cancelRemainingDialogueOnReact, onReactCallback, counterIdRestriction));
        return this;
    }

    public Dialogue addWaitForEmojiReaction(ReactionEmoji emoji, boolean cancelRemainingDialogueOnReact) {
        return addWaitForEmojiReaction(emoji, cancelRemainingDialogueOnReact, (m) -> {}, Optional.empty());
    }

    public Dialogue addWaitForAnyDialogueElement(DialogueElement... elementsToWaitFor) {
        elements.add(new ParallelDialogElements(elementsToWaitFor));
        return this;
    }

    public Dialogue addEmojiReaction(ReactionEmoji emoji) {
        elements.add(new EmojiReaction((emoji)));
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
            Message currentMessage = message;
            while (currentState < elements.size()) {
                DialogueElement element = elements.get(currentState);
                element.run(currentMessage);
                if (element.shouldCancelRemaningElements()) {
                    currentState = elements.size();
                }
                if (element.getNewMessage().isPresent()) {
                    currentMessage = element.getNewMessage().get();
                }
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

    public Dialogue setNpcLineConverter(Function<String, String> npcLineConverter) {
        this.npcLineConverter = npcLineConverter;
        return this;
    }

    public List<DialogueElement> getElements() {
        return elements;
    }
}
