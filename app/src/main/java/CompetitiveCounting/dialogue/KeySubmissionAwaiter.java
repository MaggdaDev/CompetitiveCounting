package CompetitiveCounting.dialogue;

import CompetitiveCounting.CountingBot;
import CompetitiveCounting.CountingEmojis;
import CompetitiveCounting.interactionhandlers.EmojiReactHandler;
import CompetitiveCounting.interactionhandlers.SlashCommandHandler;
import discord4j.core.object.entity.Message;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class KeySubmissionAwaiter extends DialogueElement {

    private final SlashCommandHandler.KeySubmissionListener listener;
    private final CountDownLatch finishedAtZeroLatch;
    private final Supplier<Boolean> shouldCancelOnTimeout;
    private boolean shouldCancelRemaning = false;
    private final int timeoutSeconds;
    private String channelId;

    public KeySubmissionAwaiter(SlashCommandHandler.KeySubmissionListener listener, CountDownLatch finishedAtZeroLatch, Supplier<Boolean> shouldCancelOnTimeout,
                                int timeoutSeconds) {
        this.listener = listener;
        this.finishedAtZeroLatch = finishedAtZeroLatch;
        this.shouldCancelOnTimeout = shouldCancelOnTimeout;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public void run(Message message) {
        channelId = message.getChannelId().asString();
        CountingBot.getInstance().getSlashCommandHandler().addKeySubmissionConsumer(channelId, listener);

        try {
            if (!finishedAtZeroLatch.await(Math.max(0,timeoutSeconds-6), TimeUnit.SECONDS)) {
                message.addReaction(CountingEmojis.THREE).subscribe();
                Thread.sleep(2000);
                message.addReaction(CountingEmojis.TWO).subscribe();
                Thread.sleep(2000);
                message.addReaction(CountingEmojis.ONE).subscribe();
                Thread.sleep(2000);
                if (shouldCancelOnTimeout.get()) {
                    shouldCancelRemaning = true;
                }
            }
            CountingBot.getInstance().getSlashCommandHandler().removeKeySubmissionConsumer(channelId, listener);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean shouldCancelRemaningElements() {
        return shouldCancelRemaning;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (channelId != null) {
            CountingBot.getInstance().getSlashCommandHandler().removeKeySubmissionConsumer(channelId, listener);
        }
    }
}
