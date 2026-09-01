package competitivecounting.dialogue;

import competitivecounting.CountingEmojis;
import competitivecounting.CountingTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class DialogueTest extends CountingTest {
    int successes = 0;
    int fails = 0;
    int timeouts = 0;

    @BeforeEach
    public void setUp() {
        System.out.println("Setup!");
        super.setUp();
        successes = 0;
        fails = 0;
        timeouts = 0;
    }

    @Test
    void basicThumbsConfirmationFunctionality() throws InterruptedException{
        System.out.println(message == null);
        new Dialogue()
                .addNpcLine("test",0)
                .addSinglePersonThumbsUpDownConfirmation(
                m -> {
                    successes++;
                }, m -> {
                    fails++;
                }, false, new AtomicReference<>(null), 2, m -> {
                    timeouts++;
                    return false;
                })
                .play(message);
        Thread.sleep(1000);
        assertEquals(0, successes);
        assertEquals(0, fails);
        assertEquals(0, timeouts);
        super.simulateEmojiReaction(CountingEmojis.THUMBS_UP);
        assertEquals(1, successes);
        super.simulateEmojiReaction(CountingEmojis.THUMBS_UP);
        assertEquals(1, successes);
        super.simulateEmojiReaction(CountingEmojis.THUMBS_DOWN);
        assertEquals(1, successes);
        assertEquals(0, fails);
    }

    @Test
    void cancelRemainingFunctionality() throws InterruptedException{
        // Cancel remaining after timeout
        createDialogueWithMessageAfterThumbsCheck(true, true).play(message);
        Thread.sleep(1300);
        super.simulateEmojiReaction(CountingEmojis.THUMBS_UP);
        assertEquals(0, successes);
        assertEquals(1, timeouts);
        assertEquals(0, fails);
        assertEquals("test", output.get(output.size()-1));

        // Cancel remaining after thumbs down iff wished
        createDialogueWithMessageAfterThumbsCheck(true, false).play(message);
        Thread.sleep(200);
        super.simulateEmojiReaction(CountingEmojis.THUMBS_DOWN);
        assertEquals(0, successes);
        assertEquals(1, timeouts);
        assertEquals(1, fails);
        assertEquals("test",output.get(output.size()-1));

        // Do not cancel remaining after thumbs down if not wished
        createDialogueWithMessageAfterThumbsCheck(false, true).play(message);
        Thread.sleep(200);
        super.simulateEmojiReaction(CountingEmojis.THUMBS_DOWN);
        assertEquals(0, successes);
        assertEquals(1, timeouts);
        assertEquals(2, fails);
        Thread.sleep(200);
        assertEquals("Second message", output.get(output.size()-1));

        // Do not cancel remaining after timeout down if not wished
        createDialogueWithMessageAfterThumbsCheck(true, false).play(message);
        Thread.sleep(1300);
        super.simulateEmojiReaction(CountingEmojis.THUMBS_DOWN);
        assertEquals(0, successes);
        assertEquals(2, timeouts);
        assertEquals(2, fails);
        assertEquals("Second message",output.get(output.size()-1));
    }

    @Test
    void testOnlyCorrectPersonCanPassThumbsCheck() throws InterruptedException {
        AtomicReference<String> counterIdRestriction = new AtomicReference<>(OTHER_COUNTER_ID);
        new Dialogue()
                .addNpcLine("test",0)
                .addSinglePersonThumbsUpDownConfirmation(
                        m -> {
                            successes++;
                        }, m -> {
                            fails++;
                        }, false, counterIdRestriction, 2, m -> {
                            timeouts++;
                            return false;
                        })
                .play(message);
        Thread.sleep(200);
        super.simulateEmojiReaction(COUNTER_ID, CountingEmojis.THUMBS_UP);
        assertEquals(0, successes);
        assertEquals(0, fails);
        assertEquals(0, timeouts);
        super.simulateEmojiReaction(COUNTER_ID, CountingEmojis.THUMBS_UP);
        assertEquals(0, successes);
        assertEquals(0, fails);
        assertEquals(0, timeouts);
        super.simulateEmojiReaction(OTHER_COUNTER_ID, CountingEmojis.THUMBS_UP);
        assertEquals(1, successes);
        assertEquals(0, fails);
        assertEquals(0, timeouts);
        super.simulateEmojiReaction(OTHER_COUNTER_ID, CountingEmojis.THUMBS_DOWN);
        assertEquals(1, successes);
        assertEquals(0, fails);
        assertEquals(0, timeouts);
    }


    private Dialogue createDialogueWithMessageAfterThumbsCheck(boolean cancelRemaningAfterThumbsDown, boolean cancelRemaningAfterTimeout) {
        return new Dialogue()
                .addNpcLine("test",0)
                .addSinglePersonThumbsUpDownConfirmation(
                        m -> {
                            successes++;
                        }, m -> {
                            fails++;
                        }, cancelRemaningAfterThumbsDown, new AtomicReference<>(null), 1, m -> {
                            timeouts++;
                            return cancelRemaningAfterTimeout;
                        })
                .addNpcLine("Second message",0);
    }

    @Test
    void testNoTimeoutDueToLongRunningSuccess() throws InterruptedException{
        assertEquals(0, successes);
        assertEquals(0, fails);
        assertEquals(0, timeouts);
        new Dialogue()
                .addNpcLine("test",0)
                .addSinglePersonThumbsUpDownConfirmation(
                        m -> {
                            System.out.println("Long running success started");
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            System.out.println("Long running success finished");
                            successes++;
                        }, m -> {
                            fails++;
                        }, false, new AtomicReference<>(null), 1, m -> {
                            timeouts++;
                            return false;
                        })
                .play(message);
        Thread.sleep(50);
        super.simulateEmojiReaction(CountingEmojis.THUMBS_UP);
        System.out.println("Measure!");
        Thread.sleep(100);

        // Nothing happened since success was started
        assertEquals(0, successes);
        assertEquals(0, fails);
        assertEquals(0, timeouts);
        Thread.sleep(1100);

        // The dialogue has timed out, but is waiting for success to finish
        assertEquals(0, successes);
        assertEquals(0, fails);
        assertEquals(0, timeouts);

        // Wait for the long running success to finish
        Thread.sleep(1000);
        System.out.println("Waited long enough that long-running success must be finished");
        assertEquals(0, timeouts);
        assertEquals(1, successes);
        assertEquals(0, fails);
    }
}