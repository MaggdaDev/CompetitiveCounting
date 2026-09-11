package competitivecounting.dialogue;

import discord4j.core.object.entity.Message;

import java.util.function.Function;

public class SleepElement extends ParallelizableDialogueElement {
    private final long timespan;
    private final Function<Message, Boolean> afterSleep;
    private Thread sleepingThread = null;

    public SleepElement(long timespan, Function<Message, Boolean> afterSleep, Finishable parentLock) {
        super(parentLock);
        this.timespan = timespan;
        this.afterSleep = afterSleep;
    }

    public SleepElement(long timespan) {
        this(timespan, m -> false,null);
    }

    @Override
    public void run(Message message) {
        try {
            sleepingThread = Thread.currentThread();
            Thread.sleep(timespan * 1000L);
            synchronized (parentLock) {
                if (parentLock.isFinished()) {
                    return;
                }
                if (sleepingThread != null && sleepingThread.isAlive()) {
                    setCancelRemainingElementsIfNotAlreadyCanceled(afterSleep.apply(message));
                }
                setFinished();
            }

        } catch (InterruptedException e) {
            System.out.println("Sleep interrupted: " + e.getMessage());
            cancelRemainingElements();
            setFinished();
        }
    }




    @Override
    public void dispose() {
        if (sleepingThread != null && sleepingThread.isAlive()) {
            sleepingThread.interrupt();
        }
    }
}
