package CompetitiveCounting.dialogue;

import discord4j.core.object.entity.Message;

import java.util.concurrent.CompletableFuture;

public class ParallelDialogElements extends DialogueElement{
    private final DialogueElement[] elements;
    private boolean shouldCancelRemainingElements = false;

    public ParallelDialogElements(DialogueElement... elements) {
        this.elements = elements;
    }

    @Override
    public void run(Message message) {
        CompletableFuture<DialogueElement>[] futures = new CompletableFuture[elements.length];
        for (int i = 0; i < elements.length; i++) {
            DialogueElement element = elements[i];
            futures[i] = CompletableFuture.supplyAsync(() -> {
                element.run(message);
                return element;
            });
        }
        DialogueElement element = (DialogueElement) CompletableFuture.anyOf(futures).join(); // Wait for any of the elements to complete
        for (DialogueElement currEl: elements) {
            if (currEl != element) {
                currEl.dispose(); // Dispose of the other elements
            }
        }
        shouldCancelRemainingElements = element.shouldCancelRemaningElements();
    }

    @Override
    public void dispose() {
        super.dispose();
        for (DialogueElement element: elements) {
            element.dispose();
        }
    }

    @Override
    public boolean shouldCancelRemaningElements() {
        return shouldCancelRemainingElements;
    }
}
