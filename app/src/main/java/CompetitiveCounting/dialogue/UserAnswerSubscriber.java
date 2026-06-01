package CompetitiveCounting.dialogue;

import CompetitiveCounting.CountingBot;
import CompetitiveCounting.UserAnswerHandler;
import com.google.common.base.Objects;
import discord4j.core.object.entity.Message;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Function;

public class UserAnswerSubscriber extends DialogueElement {
    private final Function<Message, Boolean> testAnswer;
    private final CountDownLatch latch = new CountDownLatch(1);

    private Message usersAnswerMessage;

    private Function<Message, Boolean> answerCallback = null;

    private Thread waitingThread = null;

    public UserAnswerSubscriber(Function<Message, Boolean> testAnswer) {
        this.testAnswer = testAnswer;
    }

    @Override
    public void run(Message message) {
        answerCallback = (msg) -> {
            if (!Objects.equal(msg.getChannelId().asString(), message.getChannelId().asString())) {
                return false;
            }
            boolean wasSuccessful = testAnswer.apply(msg);
            if (wasSuccessful) {
                usersAnswerMessage = msg;
                latch.countDown();
            }
            return wasSuccessful;
        };
        CountingBot.getInstance().getUserAnswerHandler().addHandler(answerCallback);
        waitingThread = Thread.currentThread();
        try {
            latch.await();
        } catch (InterruptedException e) {
            System.out.println("User answer react waiter thread interrupted with message: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore the interrupted status
        }
    }

    @Override
    public Optional<Message> getNewMessage() {
        return Optional.ofNullable(usersAnswerMessage);
    }

    @Override
    public void dispose() {
        UserAnswerHandler handler = CountingBot.getInstance().getUserAnswerHandler();
        if (waitingThread != null) {
            waitingThread.interrupt();
        }
        if (answerCallback != null && handler.hasHandler(answerCallback)) {
            handler.removeHandler(answerCallback);
        }
    }
}
