package CompetitiveCounting;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.BaseSubscriber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class EmojiReactHandler implements Consumer<ReactionAddEvent> {

        private final String channelIdAsString;
        private final static ReactionEmoji.Unicode TROPHY_UNICODE = Emojis.TROPHY.asUnicodeEmoji().get();

        private final ArrayList<BiFunction<Message, User, Boolean>> onTrophyReact = new ArrayList<>();

        private final ArrayList<TriFunction<Message, User, Integer, Boolean>> onNumberReact = new ArrayList<>();
        public EmojiReactHandler(String channelIdAsString) {
            this.channelIdAsString = channelIdAsString;
        }

        @Override
        public void accept(ReactionAddEvent event) {
            if(!event.getChannelId().asString().equals(channelIdAsString)) {
                return;
            }
            if (event.getEmoji().asUnicodeEmoji().isEmpty()) {
                return;
            }
            ReactionEmoji.Unicode emoji = event.getEmoji().asUnicodeEmoji().get();
            User user = event.getUser().block();
            if(user == null || user.isBot()) {
                return;
            }

            if (emoji.equals(TROPHY_UNICODE)) {
                onTrophyReact.removeIf(func -> func.apply(event.getMessage().block(), user));   // Call all functions and remove them if they return true
            } else if (Arrays.stream(Emojis.ALL_NUMBER_EMOJIS).filter(e -> e.equals(emoji)).count() > 0) {
                int number = Arrays.asList(Emojis.ALL_NUMBER_EMOJIS).indexOf(emoji);
                onNumberReact.removeIf(func -> func.apply(event.getMessage().block(), user, number));
            }


        }

        public void addOnTrophyReact(BiFunction<Message, User,Boolean> consumer) {
            onTrophyReact.add(consumer);
        }

        public void addOnNumberReact(TriFunction<Message, User, Integer, Boolean> consumer) {
            onNumberReact.add(consumer);
        }

        @FunctionalInterface
        public interface TriFunction<T, U, V, R> {
            R apply(T t, U u, V v);
        }

}
