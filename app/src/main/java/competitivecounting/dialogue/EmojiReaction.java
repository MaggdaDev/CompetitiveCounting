package competitivecounting.dialogue;

import discord4j.core.object.emoji.Emoji;
import discord4j.core.object.entity.Message;

public class EmojiReaction extends DialogueElement{
    private final Emoji emoji;
    public EmojiReaction(Emoji emoji) {
        this.emoji = emoji;
    }

    @Override
    public void run(Message message) {
        message.addReaction(emoji).subscribe();
    }
}
