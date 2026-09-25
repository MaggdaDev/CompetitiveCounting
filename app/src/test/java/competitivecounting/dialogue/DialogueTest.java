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
    int timeouts = 0,
    firstCalls = 0,
    secondCalls = 0,
    thirdCalls = 0;

    @BeforeEach
    public void setUp() {
        System.out.println("Setup!");
        super.setUp();
        successes = 0;
        fails = 0;
        timeouts = 0;
        firstCalls = 0;
        secondCalls = 0;
        thirdCalls = 0;
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
        System.out.println(output);
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
    void testCancelRemainingForMultiPersonParallelCheck() throws InterruptedException {
        // Test multiperson
        new Dialogue().initializeParallelDialogElements()
                .addWaitForEmojiReaction(CountingEmojis.THUMBS_UP, false, m -> {
                    firstCalls++;
                }, new AtomicReference<>(COUNTER_ID), ParallelDialogElementsBuilder.ParallelDialogElementType.NECESSARY)
                .addWaitForEmojiReaction(CountingEmojis.THUMBS_DOWN, false, m -> {
                    secondCalls++;
                }, new AtomicReference<>(OTHER_COUNTER_ID), ParallelDialogElementsBuilder.ParallelDialogElementType.NECESSARY)
                .finishParallelDialogElementsAndAdd(1, m -> {
                    timeouts++;
                    return true;
                }).addRunnable(m -> {
                    thirdCalls++;
                }).play(message);
        sleep(200);
        simulateEmojiReaction(CountingEmojis.THUMBS_UP);
        assertEquals(1, firstCalls);
        assertEquals(0, secondCalls);
        assertEquals(0, thirdCalls);
        assertEquals(0, timeouts);
        sleep(1200);
        assertEquals(1, firstCalls);
        assertEquals(0, secondCalls);
        assertEquals(0, thirdCalls);
        assertEquals(1, timeouts);
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
                            System.out.println("TIMOUT");
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

    @Test
    void testMultiPersonParallelCheck() throws InterruptedException{

        assertEquals(0, secondCalls);    // If one sufficient has run, do not call the other!
        assertEquals(0, firstCalls);
        new Dialogue().addNpcLine("test", 0).initializeParallelDialogElements()
                .addWaitForEmojiReaction(CountingEmojis.THUMBS_UP, false, m-> {
                    System.out.println("FIRST");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {

                    }
                    firstCalls++;
                }, new AtomicReference<>(COUNTER_ID), ParallelDialogElementsBuilder.ParallelDialogElementType.SUFFICIENT)
                .addWaitForEmojiReaction(CountingEmojis.THUMBS_UP, false, m -> {
                    System.out.println("SECOND!");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {
                    }
                    secondCalls++;
                }, new AtomicReference<>(OTHER_COUNTER_ID), ParallelDialogElementsBuilder.ParallelDialogElementType.SUFFICIENT)
                .finishParallelDialogElementsAndAdd(2, m-> {
                    fails++;
                    return false;
                }).play(message);
        Thread.sleep(200);
        simulateEmojiReaction(OTHER_COUNTER_ID, CountingEmojis.THUMBS_UP);
        simulateEmojiReaction(COUNTER_ID, CountingEmojis.THUMBS_UP);
        Thread.sleep(1200);
        assertEquals(1, secondCalls);    // If one sufficient has run, do not call the other!
        assertEquals(0, firstCalls);
        Thread.sleep(1000);
        assertEquals(0, fails);
    }

    @Test
    void assertTimeoutExecutedWhenNecessaryConditionsFulfilled() throws InterruptedException {
        new Dialogue().addNpcLine("test", 0).initializeParallelDialogElements()
                .addWaitForEmojiReaction(CountingEmojis.THUMBS_UP, false, m-> {
                    firstCalls++;
                    System.out.println("FIRST");
                }, new AtomicReference<>(COUNTER_ID), ParallelDialogElementsBuilder.ParallelDialogElementType.NECESSARY)
                .addWaitForEmojiReaction(CountingEmojis.THUMBS_UP, false, m -> {
                    secondCalls++;
                }, new AtomicReference<>(OTHER_COUNTER_ID), ParallelDialogElementsBuilder.ParallelDialogElementType.NECESSARY)
                .finishParallelDialogElementsAndAdd(5, m-> {
                    timeouts++;
                    return false;
                }).play(message);
        Thread.sleep(200);
        simulateEmojiReaction(COUNTER_ID, CountingEmojis.THUMBS_UP);
        Thread.sleep(200);
        assertEquals(1, firstCalls);
        assertEquals(0, secondCalls);
        Thread.sleep(5000);
        assertEquals(0, successes);
        assertEquals(0, fails);
        assertEquals(1, timeouts);
    }

    @Test
    void noTimeoutDueToLongRunningSuccessInLinearDialogue() throws InterruptedException{
        new Dialogue()
                .addWaitForEmojiReaction(CountingEmojis.THUMBS_UP, (msg, c) -> {
                    System.out.println("Long running success started");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Long running success finished");
                    successes++;
                    return true;
                }, 1, m -> {
                    timeouts++;
                    return true;
                }).play(message);
        Thread.sleep(200);
        assertEquals(0, successes);
        assertEquals(0, timeouts);
        simulateEmojiReaction(CountingEmojis.THUMBS_UP);
        Thread.sleep(1000);
        assertEquals(0, successes);
        assertEquals(0, timeouts);
        Thread.sleep(1000);
        assertEquals(1, successes);
        assertEquals(0, timeouts);

        new Dialogue()
                .addWaitForEmojiReaction(CountingEmojis.THUMBS_UP, (msg, c) -> {
                    System.out.println("Long running success started");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Long running success finished");
                    return false;
                }, 1, m -> {
                    timeouts++;
                    return true;
                }).addRunnable(m ->  successes++).play(message);
        Thread.sleep(200);
        assertEquals(1, successes);
        assertEquals(0, timeouts);
        simulateEmojiReaction(CountingEmojis.THUMBS_UP);
        Thread.sleep(1000);
        assertEquals(1, successes);
        assertEquals(0, timeouts);
        Thread.sleep(1000);
        assertEquals(1, successes);
        assertEquals(1, timeouts);
    }

    @Test
    void testSufficientNecessaryTimeout() throws InterruptedException {
        new Dialogue().addNpcLine("test",0)
                .initializeParallelDialogElements()
                .addWaitForEmojiReaction(CountingEmojis.THUMBS_DOWN, false, m-> {
                    firstCalls++;
                    System.out.println("FIRST");
                }, new AtomicReference<>(COUNTER_ID), ParallelDialogElementsBuilder.ParallelDialogElementType.NECESSARY)
                .addWaitForEmojiReaction(CountingEmojis.TWO, false, m-> {
                    secondCalls++;
                    System.out.println("SECOND");
                }, new AtomicReference<>(COUNTER_ID), ParallelDialogElementsBuilder.ParallelDialogElementType.NECESSARY)
                .addWaitForEmojiReaction(CountingEmojis.THUMBS_UP, false, m-> {
                    System.out.println("THIRD started...");
                    sleep(2000);
                    thirdCalls++;
                    System.out.println("THIRD");
                }, new AtomicReference<>(COUNTER_ID), ParallelDialogElementsBuilder.ParallelDialogElementType.SUFFICIENT)
                .finishParallelDialogElementsAndAdd(1, m-> {
                    System.out.println("TIMEOUT");
                    timeouts++;
                    return false;
                }).play(message);
        sleep(200);
        simulateEmojiReaction(COUNTER_ID, CountingEmojis.THUMBS_DOWN);
        sleep(200);
        simulateEmojiReaction(COUNTER_ID, CountingEmojis.THUMBS_UP);
        sleep(200);
        assertEquals(1, firstCalls);
        assertEquals(0, secondCalls);
        assertEquals(0, thirdCalls);
        assertEquals(0, timeouts);
        sleep(200);
        assertEquals(1, firstCalls);
        assertEquals(0, secondCalls);
        assertEquals(0, thirdCalls);
        assertEquals(0, timeouts);
        sleep(1000);
        assertEquals(1, firstCalls);
        assertEquals(0, secondCalls);
        assertEquals(0, thirdCalls);
        assertEquals(0, timeouts);
        sleep(1000);
        assertEquals(1, firstCalls);
        assertEquals(0, secondCalls);
        assertEquals(1, thirdCalls);
        assertEquals(0, timeouts);

    }

    @Test
    void testOnCanceled() throws InterruptedException {
       createOnCanceledDialogue().play(message);
       sleep(200);
       simulateEmojiReaction(CountingEmojis.THUMBS_UP);
       sleep(1000);
       assertEquals(0, firstCalls);
       assertEquals(0, secondCalls);
       assertEquals(1, successes);
       assertEquals(0, fails);
       assertEquals(0, timeouts);
       assertEquals(1, thirdCalls);

        createOnCanceledDialogue().play(message);
        sleep(200);
        simulateEmojiReaction(CountingEmojis.THUMBS_DOWN);
        sleep(1000);
        assertEquals(1, firstCalls);
        assertEquals(1, secondCalls);
        assertEquals(1, successes);
        assertEquals(1, fails);
        assertEquals(0, timeouts);
        assertEquals(1, thirdCalls);

        createOnCanceledDialogue().play(message);
        sleep(200);
        sleep(1000);
        assertEquals(2, firstCalls);
        assertEquals(2, secondCalls);
        assertEquals(1, successes);
        assertEquals(1, fails);
        assertEquals(1, timeouts);
        assertEquals(1, thirdCalls);

    }

    private Dialogue createOnCanceledDialogue() {
        return  new Dialogue()
                .addOnCanceled(m -> {
                    firstCalls++;
                }).addOnCanceled(m -> {
                    secondCalls++;
                }).addSinglePersonThumbsUpDownConfirmation(m -> {
                    successes++;
                }, m -> {
                    fails++;
                }, true, new AtomicReference<>(null), 1, m -> {
                    timeouts++;
                    return true;
                }).addRunnable(m -> {
                    thirdCalls++;
                });
    }
}