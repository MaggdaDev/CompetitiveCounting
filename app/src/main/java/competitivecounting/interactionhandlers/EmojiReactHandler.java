package competitivecounting.interactionhandlers;

import competitivecounting.CountingEmojis;
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
    private final static ReactionEmoji SPECIAL_TROPHY_REACTION_EMOJI = CountingEmojis.SPECIAL_TROPHY.asCustomEmoji().get();

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
        ReactionEmoji emoji = event.getEmoji();
        User user = event.getUser().block();
        if (user == null || user.isBot()) {
            return;
        }

        if (emoji.asUnicodeEmoji().isPresent()) {
            ReactionEmoji.Unicode unicodeEmoji = emoji.asUnicodeEmoji().get();

            onAnyReact.removeIf(func -> func.apply(event.getMessage().block(), user, unicodeEmoji));

            List<ReactionEmoji> numberList = Arrays.asList(CountingEmojis.ALL_NUMBER_EMOJIS);
            if (numberList.contains(unicodeEmoji)) {
                int number = numberList.indexOf(unicodeEmoji);
                disposeIfSingleUse(onNumberReact.removeIf(func -> func.apply(event.getMessage().block(), user, number)));
                return;
            }
        }

        String emojiKey = getEmojiKey(emoji);
        if (emojiReactions.containsKey(emojiKey)) {
            List<BiFunction<Message, User, Boolean>> functions = emojiReactions.get(emojiKey);
            disposeIfSingleUse(functions.removeIf(func -> func.apply(event.getMessage().block(), user)));
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

    private String getEmojiKey(ReactionEmoji emoji) {
        if (emoji instanceof ReactionEmoji.Unicode) {
            return ((ReactionEmoji.Unicode) emoji).getRaw();
        } else if (emoji instanceof ReactionEmoji.Custom) {
            ReactionEmoji.Custom custom = (ReactionEmoji.Custom) emoji;
            return custom.isAnimated()
                    ? "<a:" + custom.getName() + ":" + custom.getId().asString() + ">"  // starts with <a: if animated
                    : "<:" + custom.getName() + ":" + custom.getId().asString() + ">";
        }
        return emoji.toString();
    }

    public void addOnEmojiReact(BiFunction<Message, User, Boolean> consumer, ReactionEmoji... emojis) {
        if (emojis.length == 0) {
            throw new IllegalArgumentException("At least one emoji must be provided");
        }
        for (ReactionEmoji emoji : emojis) {
            String emojiRaw = getEmojiKey(emoji);
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
        addOnEmojiReact(consumer, TROPHY_UNICODE, SPECIAL_TROPHY_REACTION_EMOJI);
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
