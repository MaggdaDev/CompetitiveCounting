package competitivecounting.dialogue;

import competitivecounting.CountingBot;
import discord4j.core.object.entity.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DMResponseHandlerElement extends DialogueElement {
    private final int timespan;
    private List<Message> collectedMessages;
    private Function<Message, Boolean> handleAndShouldRemoveHandler;

    public DMResponseHandlerElement(Function<Message, Boolean> handleAndShouldRemoveHandler, int timespan) {
        this.timespan = timespan;
        this.handleAndShouldRemoveHandler = handleAndShouldRemoveHandler;
    }

    @Override
    public void run(Message message) {
        collectedMessages = new ArrayList<>();
        CountingBot.getInstance().getUserDMHandler().addHandler(handleAndShouldRemoveHandler);
        try {
            Thread.sleep(timespan * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (CountingBot.getInstance().getUserDMHandler().hasHandler(handleAndShouldRemoveHandler)) {
            CountingBot.getInstance().getUserDMHandler().removeHandler(handleAndShouldRemoveHandler);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (collectedMessages != null) {
            collectedMessages.clear();
            collectedMessages = null;
        }
        if (handleAndShouldRemoveHandler != null) {
            if (CountingBot.getInstance().getUserDMHandler().hasHandler(handleAndShouldRemoveHandler)) {
                CountingBot.getInstance().getUserAnswerHandler().removeHandler(handleAndShouldRemoveHandler);
            }
            handleAndShouldRemoveHandler = null;
        }
    }
}
