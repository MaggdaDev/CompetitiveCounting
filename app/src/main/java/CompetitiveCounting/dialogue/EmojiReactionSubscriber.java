package CompetitiveCounting.dialogue;

import CompetitiveCounting.CountingBot;
import CompetitiveCounting.interactionhandlers.EmojiReactHandler;
import com.google.common.base.Objects;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class EmojiReactionSubscriber extends DialogueElement {
    private final ReactionEmoji emoji;

    private final CountDownLatch latch = new CountDownLatch(1);

    private EmojiReactHandler emojiReactHandler;
    private boolean shouldCancelRemainingDialogueOnReact = false;

    private Consumer<Message> onReactCallback = null;
    private final AtomicReference<String> counterIdRestriction;

    private Thread waitingThread = null;

    public EmojiReactionSubscriber(ReactionEmoji emoji, boolean shouldCancelRemainingDialogueOnReact,
                                   Consumer<Message> onReactCallback, AtomicReference<String> counterIdRestriction) {
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
                    latch.countDown(); // Signal that the reaction was received
                    return true; // Return true to indicate the reaction was handled and the handler can be removed
                },
                emoji.asUnicodeEmoji().get()
        );
        waitingThread = Thread.currentThread();
        try {
            latch.await();
            onReactCallback.accept(message);
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

