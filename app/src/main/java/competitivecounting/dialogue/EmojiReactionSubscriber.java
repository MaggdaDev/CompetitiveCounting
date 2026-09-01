package competitivecounting.dialogue;

import competitivecounting.Counter;
import competitivecounting.CountingBot;
import competitivecounting.interactionhandlers.EmojiReactHandler;
import com.google.common.base.Objects;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Function;

public class EmojiReactionSubscriber extends DialogueElement {
    private final ReactionEmoji emoji;

    private final CountDownLatch latch = new CountDownLatch(1);

    private EmojiReactHandler emojiReactHandler;
    private boolean shouldCancelRemainingDialogue = false;

    private BiFunction<Message, Counter, Boolean> onReactCallback = null;
    private final AtomicReference<String> counterIdRestriction;

    private Thread waitingThread = null;
    private final long timeoutSeconds;
    private final Function<Message, Boolean> onTimeout;
    private final Dialogue.DialogStatusInfo dialogStatusInfo;

    public EmojiReactionSubscriber(ReactionEmoji emoji, boolean shouldCancelRemainingDialogueOnReact,
                                   BiFunction<Message, Counter, Boolean> onReactCallback, AtomicReference<String> counterIdRestriction,
                                   long timeoutSeconds, Function<Message, Boolean> onTimeoutCallback) {
        this(emoji, shouldCancelRemainingDialogueOnReact, onReactCallback, counterIdRestriction, timeoutSeconds, onTimeoutCallback,
                new Dialogue.DialogStatusInfo(Dialogue.WaitingStatus.CREATED));
    }

    public EmojiReactionSubscriber(ReactionEmoji emoji, boolean shouldCancelRemainingDialogueOnReact,
                                   BiFunction<Message, Counter, Boolean> onReactCallback, AtomicReference<String> counterIdRestriction,
                                   long timeoutSeconds, Function<Message, Boolean> onTimeoutCallback,
                                   Dialogue.DialogStatusInfo dialogStatusInfo) {
        this.emoji = emoji;
        this.shouldCancelRemainingDialogue = shouldCancelRemainingDialogueOnReact;
        this.onReactCallback = onReactCallback;
        this.counterIdRestriction = counterIdRestriction;
        this.timeoutSeconds = timeoutSeconds;
        this.onTimeout = onTimeoutCallback;
        this.dialogStatusInfo = dialogStatusInfo;
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
                    synchronized (dialogStatusInfo) { // Do not timeout while in this block for the case that onReactCallback takes a long time
                        if (onReactCallback.apply(message, CountingBot.getCounter(msg.getGuildId().get().asString(), user.getId().asString()))) {
                            latch.countDown(); // Signal that the reaction was received
                            dialogStatusInfo.waitingStatus = Dialogue.WaitingStatus.FINISHED;   // In case timeout is waiting for onReactCallback, it will not execute onTimeout
                            return true; // Return true to indicate the reaction was handled and the handler can be removed
                        }
                    }
                    return false;
                },
                emoji.asUnicodeEmoji().get()
        );
        waitingThread = Thread.currentThread();
        try {
            dialogStatusInfo.waitingStatus = Dialogue.WaitingStatus.WAITING;
            latch.await(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS); // Wait for the reaction or timeout
            synchronized (dialogStatusInfo) {
                if (dialogStatusInfo.waitingStatus == Dialogue.WaitingStatus.FINISHED) {
                    return;
                }
                boolean waitSuccessful = latch.getCount() == 0;
                if (waitingThread == null || !waitingThread.isAlive()) {
                    waitSuccessful = true;
                }
                if (!waitSuccessful) { // timeout
                    if (onTimeout != null) {
                        shouldCancelRemainingDialogue = onTimeout.apply(message);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean shouldCancelRemaningElements() {
        return shouldCancelRemainingDialogue;
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

