package CompetitiveCounting.dialogue;

import discord4j.core.object.entity.Message;

public abstract class DialogueElement {
    public abstract void run(Message message);

    public void dispose() {
        // Empty
    }
}
