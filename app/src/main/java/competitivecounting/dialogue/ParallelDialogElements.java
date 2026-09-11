package competitivecounting.dialogue;

import discord4j.core.object.entity.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

public class ParallelDialogElements extends DialogueElement implements Finishable {
    private final List<ParallelizableDialogueElement> sufficientElements = new ArrayList<>(),
            necessaryElements = new ArrayList<>();
    @Override
    public boolean isFinished() {
        for (ParallelizableDialogueElement currEl: sufficientElements) {
            if (currEl.isFinished()) {
                return true;
            }
        }
        if (necessaryElements.isEmpty()) {
            return false;
        }
        for (ParallelizableDialogueElement currEl: necessaryElements) {
            if (!currEl.isFinished()) {
                return false;
            }
        }
        return true;
    }

    public void addSufficientElement(ParallelizableDialogueElement element) {
        sufficientElements.add(element);
    }

    public void addNecessaryElement(ParallelizableDialogueElement element) {
        necessaryElements.add(element);
    }

    private void executeAsyncAndCheckFinished(ParallelizableDialogueElement element, Message message, CountDownLatch finishedLatch) {
        CompletableFuture.supplyAsync(() -> {
            element.run(message);
            synchronized (this) {
                if (isFinished()) {
                    finishedLatch.countDown();
                }
            }
            return element;
        }).exceptionally(e -> {
            System.out.println("THe completable future was finished exceptionally");
            return null;
        }).thenAccept(e -> {
            if (e != null && e.shouldCancelRemaningElements()) {
                cancelRemainingElements();
            }
        });

    }

    @Override
    public void run(Message message) {
        CountDownLatch parallelizationFinishedLatch = new CountDownLatch(1);
        necessaryElements.forEach(e -> executeAsyncAndCheckFinished(e, message, parallelizationFinishedLatch));
        sufficientElements.forEach(e -> executeAsyncAndCheckFinished(e, message, parallelizationFinishedLatch));
        try {
            parallelizationFinishedLatch.await();
        } catch (InterruptedException e) {
            cancelRemainingElements();
            System.out.println("Parallel Dialog elements interrupted: " + e.getMessage());
        }
        for (DialogueElement currEl: sufficientElements) {
            currEl.dispose();
        }
        for (DialogueElement currEl: necessaryElements) {
            currEl.dispose();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        for (DialogueElement element: sufficientElements) {
            element.dispose();
        }
        for (DialogueElement element: necessaryElements) {
            element.dispose();
        }
    }

}
