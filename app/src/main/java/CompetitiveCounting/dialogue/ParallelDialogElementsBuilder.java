package CompetitiveCounting.dialogue;

import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ParallelDialogElementsBuilder {

    private final List<DialogueElement> sufficientDialogueElements = new ArrayList<>(), necessaryDialogueElements = new ArrayList<>();
    private final Dialogue dialogue;

    ParallelDialogElementsBuilder(Dialogue dialogue) {
        this.dialogue = dialogue;
    }

    public enum ParallelDialogElementType {
        SUFFICIENT, NECESSARY
    }

    public ParallelDialogElementsBuilder addWaitForEmojiReaction(ReactionEmoji emoji,
                                                                 boolean cancelRemainingDialogueOnReact,
                                                                 Consumer<Message> onReactCallback, Optional<String> counterIdRestriction,
                                                                 ParallelDialogElementType type) {
        List<DialogueElement> listToAddTo = type == ParallelDialogElementType.SUFFICIENT ? sufficientDialogueElements : necessaryDialogueElements;
        listToAddTo.add(new EmojiReactionSubscriber(emoji, cancelRemainingDialogueOnReact, onReactCallback, counterIdRestriction));
        return this;
    }

    public ParallelDialogElementsBuilder addWaitForEmojiReaction(ReactionEmoji emoji,
                                                                 boolean cancelRemainingDialogueOnReact,
                                                                 ParallelDialogElementType type) {
        List<DialogueElement> listToAddTo = type == ParallelDialogElementType.SUFFICIENT ? sufficientDialogueElements : necessaryDialogueElements;
        listToAddTo.add(new EmojiReactionSubscriber(emoji, cancelRemainingDialogueOnReact, m -> {}, Optional.empty()));
        return this;
    }

    public Dialogue finishParallelDialogElementsAndAdd() {
        dialogue.addParallelWaitingDialogueElement(sufficientDialogueElements.toArray(new DialogueElement[]{}),
            necessaryDialogueElements.toArray(new DialogueElement[]{}));
        return dialogue;
    }


}
