package CompetitiveCounting.dialogue;

import CompetitiveCounting.CountingBot;
import CompetitiveCounting.CountingEmojis;
import CompetitiveCounting.EmojiReactHandler;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EmojiReactionSubscriber extends DialogueElement {
    private final ReactionEmoji emoji;

    private final CountDownLatch latch = new CountDownLatch(1);
    private final String messageId;

    private final Supplier<String> messageIdGetter;
    private EmojiReactHandler emojiReactHandler;


    public EmojiReactionSubscriber(String idOfMessageToWaitOnAsString, ReactionEmoji emoji) {
        this.emoji = emoji;
        messageId = idOfMessageToWaitOnAsString;
        messageIdGetter = null;
    }

    public EmojiReactionSubscriber(Supplier<String> idOfMessageToWaitOnGetter, ReactionEmoji emoji) {
        this.emoji = emoji;
        messageId = null;
        this.messageIdGetter = idOfMessageToWaitOnGetter;
    }

    @Override
    public void run(Message message) {
        String channelId = message.getChannelId().asString();
        emojiReactHandler = new EmojiReactHandler(channelId, true);
        CountingBot.getInstance().subscribeSingleUseSingleMessageEmojiReactHandlerAndActivate(emojiReactHandler, messageIdGetter == null ? messageId : messageIdGetter.get());
        emojiReactHandler.addOnEmojiReact(
                (msg, user) -> {
                    latch.countDown(); // Signal that the reaction was received
                    return true; // Return true to indicate the reaction was handled and the handler can be removed
                },
                emoji.asUnicodeEmoji().get()
        );
        try {
            latch.await();
        } catch (InterruptedException e) {
            System.out.println("Emoji react waiter thread interrupted with message: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore the interrupted status
        }
    }

    @Override
    public void dispose() {
        if (emojiReactHandler != null && emojiReactHandler.isActive()) {
            emojiReactHandler.disposeSingleUse();
        }
    }

    public EmojiReactHandler getEmojiReactHandler() {
        return emojiReactHandler;
    }
}

