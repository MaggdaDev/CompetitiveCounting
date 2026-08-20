package competitivecounting.dialogue;

import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

public class EmojiReaction extends DialogueElement{
    private final ReactionEmoji emoji;
    public EmojiReaction(ReactionEmoji emoji) {
        this.emoji = emoji;
    }

    @Override
    public void run(Message message) {
        message.addReaction(emoji).subscribe();
    }
}
