package competitivecounting.dialogue;

import discord4j.core.object.entity.Message;

import java.util.function.Function;

public class MaybeCancelRestOfDialog extends DialogueElement{

    private final Function<Message, Boolean> messageBooleanFunction;
    private boolean cancelRest = false;

    public MaybeCancelRestOfDialog(Function<Message, Boolean> shouldCancelCallback) {
        messageBooleanFunction = shouldCancelCallback;
    }
    @Override
    public void run(Message message) {
        cancelRest = messageBooleanFunction.apply(message);
    }

    @Override
    public boolean shouldCancelRemaningElements() {
        return cancelRest;
    }
}
