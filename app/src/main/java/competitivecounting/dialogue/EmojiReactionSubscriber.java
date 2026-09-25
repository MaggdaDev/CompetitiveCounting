package competitivecounting.dialogue;

import competitivecounting.Counter;
import competitivecounting.CountingBot;
import competitivecounting.interactionhandlers.EmojiReactHandler;
import com.google.common.base.Objects;
import discord4j.core.object.emoji.Emoji;
import discord4j.core.object.entity.Message;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

public class EmojiReactionSubscriber extends ParallelizableDialogueElement {
    private final Emoji emoji;

    private final CountDownLatch latch = new CountDownLatch(1);

    private EmojiReactHandler emojiReactHandler;

    private BiFunction<Message, Counter, Boolean> onReactCallback = null;
    private final AtomicReference<String> counterIdRestriction;

    private Thread waitingThread = null;
    private final long timeoutSeconds;
    private final Function<Message, Boolean> onTimeout;

    public EmojiReactionSubscriber(Emoji emoji, boolean shouldCancelRemainingDialogueOnReact,
                                   BiFunction<Message, Counter, Boolean> onReactCallback, AtomicReference<String> counterIdRestriction,
                                   long timeoutSeconds, Function<Message, Boolean> onTimeoutCallback,
                                   Finishable parentLock) {
        super(parentLock);
        this.emoji = emoji;
        setCancelRemainingElementsIfNotAlreadyCanceled(shouldCancelRemainingDialogueOnReact);
        this.onReactCallback = onReactCallback;
        this.counterIdRestriction = counterIdRestriction;
        this.timeoutSeconds = timeoutSeconds;
        this.onTimeout = onTimeoutCallback;
    }

    public EmojiReactionSubscriber(Emoji emoji, boolean shouldCancelRemainingDialogueOnReact,
                                   BiFunction<Message, Counter, Boolean> onReactCallback, AtomicReference<String> counterIdRestriction,
                                   long timeoutSeconds, Function<Message, Boolean> onTimeoutCallback) {
        this(emoji, shouldCancelRemainingDialogueOnReact, onReactCallback, counterIdRestriction, timeoutSeconds, onTimeoutCallback, null);
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
                    boolean reactionSuccessful;
                    synchronized (parentLock) {
                        if (parentLock.isFinished()) {
                            System.out.println("Parent lock is already finished, ignoring reaction");
                            return true;
                        }
                        reactionSuccessful = onReactCallback.apply(message, CountingBot.getCounter(msg.getGuildId().get().asString(), user.getId().asString()));
                        if (reactionSuccessful) {
                            setFinished();
                        }
                    }
                    if (reactionSuccessful) {
                        latch.countDown(); // Signal that the reaction was received
                        return true;
                    }
                    return false;
                },
                emoji.asUnicodeEmoji().get()
        );
        waitingThread = Thread.currentThread();
        try {
            latch.await(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS); // Wait for the reaction or timeout
            boolean waitSuccessful = latch.getCount() == 0;
            if (waitingThread == null || !waitingThread.isAlive()) {
                waitSuccessful = true;
            }
            if (!waitSuccessful) { // timeout
                synchronized (parentLock) {
                    if (parentLock.isFinished()) {
                        System.out.println("Parent lock is already finished, ignoring timeout");
                        return;
                    }
                    if (onTimeout != null) {
                        setCancelRemainingElementsIfNotAlreadyCanceled(onTimeout.apply(message));
                    }
                    setFinished();
                }

            }
        } catch (InterruptedException e) {
            System.out.println("EmojiReactionSubscriber waiting thread interrupted: " + e.getMessage());
            cancelRemainingElements();
        } finally {
            setFinished();
        }
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

