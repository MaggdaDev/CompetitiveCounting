package competitivecounting.dialogue;

import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

public class ParallelDialogElementsBuilder {

    private final List<DialogueElement> sufficientDialogueElements = new ArrayList<>(), necessaryDialogueElements = new ArrayList<>();
    private final Dialogue dialogue;
    private final Dialogue.DialogStatusInfo dialogStatusInfo = new Dialogue.DialogStatusInfo(Dialogue.WaitingStatus.CREATED);
    ParallelDialogElementsBuilder(Dialogue dialogue) {
        this.dialogue = dialogue;
    }

    public enum ParallelDialogElementType {
        SUFFICIENT, NECESSARY
    }

    public ParallelDialogElementsBuilder addWaitForEmojiReaction(ReactionEmoji emoji,
                                                                 boolean cancelRemainingDialogueOnReact,
                                                                 Consumer<Message> onReactCallback, AtomicReference<String> counterIdRestriction,
                                                                 ParallelDialogElementType type) {
        addToList(new EmojiReactionSubscriber(emoji, cancelRemainingDialogueOnReact, (msg, counter) -> {
            onReactCallback.accept(msg);
            return true;
        }, counterIdRestriction, Long.MAX_VALUE, m -> false, dialogStatusInfo), type);
        return this;
    }

    public ParallelDialogElementsBuilder addWaitForEmojiReaction(ReactionEmoji emoji,
                                                                 boolean cancelRemainingDialogueOnReact,
                                                                 ParallelDialogElementType type) {
        addToList(new EmojiReactionSubscriber(emoji, cancelRemainingDialogueOnReact, (m, c) -> true,
                new AtomicReference<>(), Long.MAX_VALUE, m -> false, dialogStatusInfo), type);
        return this;
    }

    public ParallelDialogElementsBuilder addRunnable(Consumer<Message> runnable, ParallelDialogElementType type) {
        addToList(new DialogRunnable(runnable), type);
        return this;
    }

    public ParallelDialogElementsBuilder addDMResponseCollector(Function<Message, Boolean> dmAcceptedFilter,
                                                                int timespan, ParallelDialogElementType type) {
        addToList(new DMResponseHandlerElement(dmAcceptedFilter, timespan), type);
        return this;
    }

    public ParallelDialogElementsBuilder addSleepElement(int timespan, ParallelDialogElementType type) {
        addToList(new SleepElement(timespan), type);
        return this;
    }

    public ParallelDialogElementsBuilder addTimeoutElement(long timespan,Function<Message, Boolean> onTimeout) {
        addToList(new SleepElement(timespan, onTimeout, dialogStatusInfo), ParallelDialogElementType.SUFFICIENT);
        return this;
    }

    public ParallelDialogElementsBuilder addSubDialogue(Dialogue subDialogue, ParallelDialogElementType type) {
        addToList(new SubDialogue(subDialogue), type);
        return this;
    }

    private void addToList(DialogueElement element, ParallelDialogElementType type) {
        List<DialogueElement> listToAddTo = type == ParallelDialogElementType.SUFFICIENT ? sufficientDialogueElements : necessaryDialogueElements;
        listToAddTo.add(element);
    }

    public Dialogue finishParallelDialogElementsAndAdd(long timeoutSeconds, Function<Message, Boolean> onTimeoutCallback) {
        addTimeoutElement(timeoutSeconds, onTimeoutCallback);
        dialogue.addParallelWaitingDialogueElement(sufficientDialogueElements.toArray(new DialogueElement[]{}),
            necessaryDialogueElements.toArray(new DialogueElement[]{}));
        return dialogue;
    }



}
