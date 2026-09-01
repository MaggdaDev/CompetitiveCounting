package competitivecounting.dialogue;

import discord4j.core.object.entity.Message;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

public class SleepElement extends DialogueElement {
    private final long timespan;
    private final Function<Message, Boolean> afterSleep;
    private boolean shouldCancelRemainingElements = false;
    private Thread sleepingThread = null;
    private final Dialogue.DialogStatusInfo dialogStatusInfo;

    public SleepElement(long timespan, Function<Message, Boolean> afterSleep, Dialogue.DialogStatusInfo dialogStatusInfo) {
        this.timespan = timespan;
        this.afterSleep = afterSleep;
        this.dialogStatusInfo = dialogStatusInfo;
    }

    public SleepElement(long timespan) {
        this(timespan, m -> false, new Dialogue.DialogStatusInfo(Dialogue.WaitingStatus.CREATED));
    }

    @Override
    public void run(Message message) {
        try {
            sleepingThread = Thread.currentThread();
            dialogStatusInfo.waitingStatus = Dialogue.WaitingStatus.WAITING;
            Thread.sleep(timespan * 1000L);
            synchronized (dialogStatusInfo) {
                if (dialogStatusInfo.waitingStatus != Dialogue.WaitingStatus.WAITING) {
                    return; // Has already finished somewhere else (parallel dialog)
                }
                if (sleepingThread != null && sleepingThread.isAlive()) {
                    shouldCancelRemainingElements = afterSleep.apply(message);
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Sleep interrupted: " + e.getMessage());
        }
    }

    @Override
    public boolean shouldCancelRemaningElements() {
        return shouldCancelRemainingElements;
    }

    @Override
    public void dispose() {
        if (sleepingThread != null && sleepingThread.isAlive()) {
            sleepingThread.interrupt();
        }
    }
}
