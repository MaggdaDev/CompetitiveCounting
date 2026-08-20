package competitivecounting.dialogue;

import competitivecounting.Counter;
import competitivecounting.interactionhandlers.SlashCommandHandler;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Dialogue {
    private int currentState = 0;
    private final List<DialogueElement> elements = new ArrayList<>();
    private final List<Runnable> thenRuns = new ArrayList<>();
    private final ArrayList<Integer> npcMessagesIndices = new ArrayList<>();
    private Thread thread;
    private Function<String, String> npcLineConverter;
    private boolean cancelAllRemaining = false;

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
                                            Consumer<Message> onReactCallback, AtomicReference<String> counterIdRestriction) {
        elements.add(new EmojiReactionSubscriber(emoji, cancelRemainingDialogueOnReact, (msg, user) -> {
            onReactCallback.accept(msg);
            return true;
        }, counterIdRestriction));
        return this;
    }

    public Dialogue addWaitForEmojiReaction(ReactionEmoji emoji, boolean cancelRemainingDialogueOnReact) {
        return addWaitForEmojiReaction(emoji, cancelRemainingDialogueOnReact, (m) -> {}, new AtomicReference<>());
    }

    public Dialogue addWaitForEmojiReaction(ReactionEmoji emoji, BiFunction<Message, Counter,  Boolean> onReactCallback) {
        elements.add(new EmojiReactionSubscriber(emoji, false, onReactCallback, new AtomicReference<>()));
        return this;
    }

    public Dialogue addWaitForUserAnswer(Function<Message, Boolean> testAnswer) {
        elements.add(new UserAnswerSubscriber(testAnswer));
        return this;
    }

    public Dialogue addSleep(int timeSeconds) {
        elements.add(new SleepElement(timeSeconds));
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

    public Dialogue addKeySubmissionAwaiter(SlashCommandHandler.KeySubmissionListener listener, CountDownLatch finishedAtZeroLatch,
                                            Supplier<Boolean> shouldCancelOnTimeout, int timeoutSeconds) {
        elements.add(new KeySubmissionAwaiter(listener, finishedAtZeroLatch, shouldCancelOnTimeout, timeoutSeconds));
        return this;
    }

    public void play(Message message) {
        playAtIndex(message, 0, false);
    }

    public void playBlocking(Message message) {
        playAtIndex(message, 0, true);
    }

    public void playAtIndex(Message message, int idx, boolean blocking) {
        currentState = idx;
        Runnable dialogueRunnable = () -> {
            Message currentMessage = message;
            while (currentState < elements.size()) {
                if (cancelAllRemaining) {
                    break;
                }
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
        };
        if (blocking) {
            dialogueRunnable.run();
        } else {
            thread = new Thread(dialogueRunnable);
            thread.start();
        }
    }

    public void stop() {
        currentState = elements.size(); // Mark the dialogue as finished
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
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

    public void addParallelWaitingDialogueElement(DialogueElement[] sufficient, DialogueElement[] necessary) {
        elements.add(new ParallelDialogElements(sufficient, necessary));
    }
    public void cancelAllRemaining() {
        cancelAllRemaining = true;
    }

}
