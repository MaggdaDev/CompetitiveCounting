package CompetitiveCounting.dialogue;

import discord4j.core.object.entity.Message;

import java.util.concurrent.CompletableFuture;

public class ParallelDialogElements extends DialogueElement{
    private final DialogueElement[] sufficientElements, necessaryElements;
    private boolean shouldCancelRemainingElements = false;

    public ParallelDialogElements(DialogueElement[] sufficient, DialogueElement[] necessary) {
        this.sufficientElements = sufficient;
        this.necessaryElements = necessary;
    }

    @Override
    public void run(Message message) {
        CompletableFuture<DialogueElement>[] necessaryFutures = new CompletableFuture[necessaryElements.length];
        CompletableFuture<DialogueElement>[] sufficientFutures = new CompletableFuture[sufficientElements.length];
        for (int i = 0; i < necessaryFutures.length; i++) {
            necessaryFutures[i] = elementToFuture(message, necessaryElements[i]);
        }
        for (int i = 0; i < sufficientFutures.length; i++) {
            sufficientFutures[i] = elementToFuture(message, sufficientElements[i]);
        }
        CompletableFuture<Object> anySufficientFuture = CompletableFuture.anyOf(sufficientFutures);
        CompletableFuture<Void> allNecessaryFutures = necessaryFutures.length == 0? new CompletableFuture<>() : CompletableFuture.allOf(necessaryFutures);
        Object result = CompletableFuture.anyOf(anySufficientFuture, allNecessaryFutures).join();
        shouldCancelRemainingElements = false;
        if (result instanceof DialogueElement) {
            shouldCancelRemainingElements = ((DialogueElement)result).shouldCancelRemaningElements();
        }
        for (DialogueElement currEl: sufficientElements) {
            currEl.dispose();
        }
        for (DialogueElement currEl: necessaryElements) {
            currEl.dispose();
        }
    }

    private CompletableFuture<DialogueElement> elementToFuture(Message message, DialogueElement element) {
        return CompletableFuture.supplyAsync(() -> {
            element.run(message);
            return element;
        });
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

    @Override
    public boolean shouldCancelRemaningElements() {
        return shouldCancelRemainingElements;
    }
}
