package CompetitiveCounting.dialogue;

import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ParallelDialogElementsBuilder {

    private final List<DialogueElement> dialogueElements = new ArrayList<>();

    private final Dialogue dialogue;

    ParallelDialogElementsBuilder(Dialogue dialogue) {
        this.dialogue = dialogue;
    }

    public ParallelDialogElementsBuilder addWaitForEmojiReaction(ReactionEmoji emoji,
                                                                 boolean cancelRemainingDialogueOnReact,
                                                                 Consumer<Message> onReactCallback, Optional<String> counterIdRestriction) {
        dialogueElements.add(new EmojiReactionSubscriber(emoji, cancelRemainingDialogueOnReact, onReactCallback, counterIdRestriction));
        return this;
    }

    public ParallelDialogElementsBuilder addWaitForEmojiReaction(ReactionEmoji emoji,
                                                                 boolean cancelRemainingDialogueOnReact) {
        dialogueElements.add(new EmojiReactionSubscriber(emoji, cancelRemainingDialogueOnReact, m -> {}, Optional.empty()));
        return this;
    }

    public Dialogue finishParallelDialogElementsAndAdd() {
        dialogue.addWaitForAnyDialogueElement(dialogueElements.toArray(new DialogueElement[]{}));
        return dialogue;
    }


}
