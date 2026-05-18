package CompetitiveCounting.dialogue;

import discord4j.core.object.entity.Message;

import java.util.Optional;

public abstract class DialogueElement {
    public abstract void run(Message message);

    public Optional<Message> getNewMessage() {
        return Optional.empty();
    };

    public boolean shouldCancelRemaningElements() {
        return false;
    }

    public void dispose() {
        // Empty
    }
}
