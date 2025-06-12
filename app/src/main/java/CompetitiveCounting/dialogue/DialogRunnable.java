package CompetitiveCounting.dialogue;

import discord4j.core.object.entity.Message;

import java.util.function.Consumer;

public class DialogRunnable extends DialogueElement {
    private final Consumer<Message> runnable;

    public DialogRunnable(Consumer<Message> runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run(Message message) {
        runnable.accept(message);
    }
}
