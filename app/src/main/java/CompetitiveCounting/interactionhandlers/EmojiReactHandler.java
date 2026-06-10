package CompetitiveCounting.interactionhandlers;

import CompetitiveCounting.CountingEmojis;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.Disposable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class EmojiReactHandler implements Consumer<ReactionAddEvent> {

    private final String channelIdAsString;
    private final static ReactionEmoji.Unicode TROPHY_UNICODE = CountingEmojis.TROPHY.asUnicodeEmoji().get();

    private final HashMap<String, List<BiFunction<Message, User, Boolean>>> emojiReactions = new HashMap<>(); // unicodeEmoji.raw -> functions

    private final ArrayList<TriFunction<Message, User, Integer, Boolean>> onNumberReact = new ArrayList<>();

    private final ArrayList<TriFunction<Message, User, ReactionEmoji.Unicode, Boolean>> onAnyReact = new ArrayList<>();

    private Disposable singleUseHandlerDisposable = null;
    private boolean isActive = false;

    public EmojiReactHandler(String channelIdAsString) {
        this.channelIdAsString = channelIdAsString;
        isActive = true;
    }

    // Use this constructor for single use handlers: initially generate an unactive hander, which gets activated as soon as the disposable subscription is available
    public EmojiReactHandler(String channelIdAsString, boolean waitForManualActivation) {
        this.channelIdAsString = channelIdAsString;
        isActive = !waitForManualActivation;
    }

    @Override
    public void accept(ReactionAddEvent event) {
        if (!event.getChannelId().asString().equals(channelIdAsString)) {
            return;
        }
        if (!isActive) {
            return;
        }
        if (event.getEmoji().asUnicodeEmoji().isEmpty()) {
            return;
        }
        ReactionEmoji.Unicode emoji = event.getEmoji().asUnicodeEmoji().get();
        User user = event.getUser().block();
        if (user == null || user.isBot()) {
            return;
        }
        onAnyReact.removeIf(func -> func.apply(event.getMessage().block(), user, emoji));
        if (Arrays.stream(CountingEmojis.ALL_NUMBER_EMOJIS).filter(e -> e.equals(emoji)).count() > 0) {
            int number = Arrays.asList(CountingEmojis.ALL_NUMBER_EMOJIS).indexOf(emoji);
            disposeIfSingleUse(onNumberReact.removeIf(func -> func.apply(event.getMessage().block(), user, number)));
            return;
        }
        String emojiRaw = emoji.getRaw();
        if (emojiReactions.containsKey(emojiRaw)) {
            List<BiFunction<Message, User, Boolean>> functions = emojiReactions.get(emojiRaw);
            disposeIfSingleUse(functions.removeIf(func -> func.apply(event.getMessage().block(), user))); // Call all functions and remove them if they return true
        }
    }

    private void disposeIfSingleUse(boolean b) {
        if (!b) {
            return;
        }
        if (singleUseHandlerDisposable != null) {
            singleUseHandlerDisposable.dispose();
            System.out.println("EmojiReactHandler for channel " + channelIdAsString + " disposed after single use.");
        }
    }

    public void addOnEmojiReact(BiFunction<Message, User, Boolean> consumer, ReactionEmoji.Unicode... emojis) {
        if (emojis.length == 0) {
            throw new IllegalArgumentException("At least one emoji must be provided");
        }
        for (ReactionEmoji.Unicode emoji : emojis) {
            String emojiRaw = emoji.getRaw();
            if (emojiReactions.containsKey(emojiRaw)) {
                emojiReactions.get(emojiRaw).add(consumer);
            } else {
                List<BiFunction<Message, User, Boolean>> functions = new ArrayList<>();
                functions.add(consumer);
                emojiReactions.put(emojiRaw, functions);
            }
        }
    }

    public void addOnTrophyReact(BiFunction<Message, User, Boolean> consumer) {
        addOnEmojiReact(consumer, TROPHY_UNICODE);
    }

    public void addOnNumberReact(TriFunction<Message, User, Integer, Boolean> consumer) {
        onNumberReact.add(consumer);
    }

    public void addOnAnyReact(TriFunction<Message, User, ReactionEmoji.Unicode, Boolean> consumer) {
        onAnyReact.add(consumer);
    }

    public boolean hasOnAnyReact(TriFunction<Message, User, ReactionEmoji.Unicode, Boolean> consumer) {
        return onAnyReact.contains(consumer);
    }

    public boolean hasOnNumberReact(TriFunction<Message, User, Integer, Boolean> consumer) {
        return onNumberReact.contains(consumer);
    }

    public void removeOnAnyReact(TriFunction<Message, User, ReactionEmoji.Unicode, Boolean> consumer) {
        onAnyReact.remove(consumer);
    }

    public void removeOnNumberReact(TriFunction<Message, User, Integer, Boolean> consumer) {
        onNumberReact.remove(consumer);
    }


    public void activateWithSingleUseMode(Disposable disposableForSingleUse) {
        isActive = true;
        singleUseHandlerDisposable = disposableForSingleUse;
    }

    public void disposeSingleUse() {
        if (!isActive) {
            throw new RuntimeException("Trying to dispose handler that is not active.");
        }
        if (singleUseHandlerDisposable == null) {
            throw new RuntimeException("Trying to dispose handler that is not single use.");
        }
        if (singleUseHandlerDisposable.isDisposed()) {
            return; // Already disposed
        }
        singleUseHandlerDisposable.dispose();
        System.out.println("EmojiReactHandler for channel " + channelIdAsString + " disposed after via external explicit disposal.");
    }

    public boolean isActive() {
        return isActive;
    }

    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

}
