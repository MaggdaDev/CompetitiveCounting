package competitivecounting.dialogue;

import discord4j.core.object.entity.Message;

import java.util.Optional;

public abstract class DialogueElement {
    private boolean cancelRemainingElements = false;
    public abstract void run(Message message);

    public Optional<Message> getNewMessage() {
        return Optional.empty();
    };

    public final boolean shouldCancelRemaningElements() {
        return cancelRemainingElements;
    }

    public void cancelRemainingElements() {
        cancelRemainingElements = true;
    }
    public void setCancelRemainingElementsIfNotAlreadyCanceled(Boolean cancel) {
        if (!cancelRemainingElements) {
            cancelRemainingElements = cancel;
        }
    }

    public void dispose() {
        // Empty
    }
}
