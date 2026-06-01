package CompetitiveCounting;

import discord4j.core.object.entity.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class UserAnswerHandler {
    private List<Function<Message, Boolean>> handlers = new ArrayList<>();

    public void handleUserMessage(Message message) {
        handlers.removeIf(h -> h.apply(message));
    }

    public void addHandler(Function<Message, Boolean> handler) {
        handlers.add(handler);
    }

    public void removeHandler(Function<Message, Boolean> handler) {
        handlers.remove(handler);
    }

    public boolean hasHandler(Function<Message, Boolean> handler) {
        return handlers.contains(handler);
    }


}
