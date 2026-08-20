package competitivecounting.dialogue;

import competitivecounting.Counter;
import competitivecounting.CountingBot;
import competitivecounting.interactionhandlers.EmojiReactHandler;
import com.google.common.base.Objects;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class EmojiReactionSubscriber extends DialogueElement {
    private final ReactionEmoji emoji;

    private final CountDownLatch latch = new CountDownLatch(1);

    private EmojiReactHandler emojiReactHandler;
    private boolean shouldCancelRemainingDialogueOnReact = false;

    private BiFunction<Message, Counter, Boolean> onReactCallback = null;
    private final AtomicReference<String> counterIdRestriction;

    private Thread waitingThread = null;

    public EmojiReactionSubscriber(ReactionEmoji emoji, boolean shouldCancelRemainingDialogueOnReact,
                                   BiFunction<Message, Counter, Boolean> onReactCallback, AtomicReference<String> counterIdRestriction) {
        this.emoji = emoji;
        this.shouldCancelRemainingDialogueOnReact = shouldCancelRemainingDialogueOnReact;
        this.onReactCallback = onReactCallback;
        this.counterIdRestriction = counterIdRestriction;
    }

    @Override
    public void run(Message message) {
        String channelId = message.getChannelId().asString();
        emojiReactHandler = new EmojiReactHandler(channelId, true);
        String messageId = message.getId().asString();
        CountingBot.getInstance().subscribeSingleUseSingleMessageEmojiReactHandlerAndActivate(emojiReactHandler, messageId);
        emojiReactHandler.addOnEmojiReact(
                (msg, user) -> {
                    if (counterIdRestriction.get() != null) {
                        if (!Objects.equal(counterIdRestriction.get(), user.getId().asString())) {
                            return false;
                        }
                    }
                    Counter counter;
                    if(onReactCallback.apply(message, CountingBot.getCounter(msg.getGuildId().get().asString(), user.getId().asString()))) {
                        latch.countDown(); // Signal that the reaction was received
                        return true; // Return true to indicate the reaction was handled and the handler can be removed
                    }
                    return false;
                },
                emoji.asUnicodeEmoji().get()
        );
        waitingThread = Thread.currentThread();
        try {
            latch.await();
        } catch (InterruptedException e) {
            System.out.println("Emoji react waiter thread interrupted with message: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore the interrupted status
        }
    }

    @Override
    public boolean shouldCancelRemaningElements() {
        return shouldCancelRemainingDialogueOnReact;
    }

    @Override
    public void dispose() {
        if (emojiReactHandler != null && emojiReactHandler.isActive()) {
            emojiReactHandler.disposeSingleUse();
        }

        if (waitingThread != null) {
            waitingThread.interrupt();
        }
    }

    public EmojiReactHandler getEmojiReactHandler() {
        return emojiReactHandler;
    }
}

