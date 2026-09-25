package competitivecounting.dialogue;

import discord4j.core.object.emoji.Emoji;
import discord4j.core.object.entity.Message;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class ParallelDialogElementsBuilder {
    private final Dialogue dialogue;
    private final ParallelDialogElements builtParallelDialogElements = new ParallelDialogElements();
    ParallelDialogElementsBuilder(Dialogue dialogue) {
        this.dialogue = dialogue;
    }

    public enum ParallelDialogElementType {
        SUFFICIENT, NECESSARY
    }

    public ParallelDialogElementsBuilder addWaitForEmojiReaction(Emoji emoji,
                                                                 boolean cancelRemainingDialogueOnReact,
                                                                 Consumer<Message> onReactCallback, AtomicReference<String> counterIdRestriction,
                                                                 ParallelDialogElementType type) {
        addToList(new EmojiReactionSubscriber(emoji, cancelRemainingDialogueOnReact, (msg, counter) -> {
            onReactCallback.accept(msg);
            return true;
        }, counterIdRestriction, Long.MAX_VALUE, m -> false, builtParallelDialogElements), type);
        return this;
    }


    private ParallelDialogElementsBuilder addTimeoutElement(long timespan,Function<Message, Boolean> onTimeout) {
        addToList(new SleepElement(timespan, onTimeout, builtParallelDialogElements), ParallelDialogElementType.SUFFICIENT);
        return this;
    }

    private void addToList(ParallelizableDialogueElement element, ParallelDialogElementType type) {
        switch (type) {
            case SUFFICIENT:
                builtParallelDialogElements.addSufficientElement(element);
                break;
            case NECESSARY:
                builtParallelDialogElements.addNecessaryElement(element);
                break;
        }
    }

    public Dialogue finishParallelDialogElementsAndAdd(long timeoutSeconds, Function<Message, Boolean> onTimeoutCallback) {
        addTimeoutElement(timeoutSeconds, onTimeoutCallback);
        dialogue.addParallelWaitingDialogueElement(builtParallelDialogElements);
        return dialogue;
    }



}
