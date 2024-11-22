package CompetitiveCounting;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.BaseSubscriber;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class EmojiReactHandler implements Consumer<ReactionAddEvent> {

        private final String channelIdAsString;
        private final static ReactionEmoji.Unicode TROPHY_UNICODE = Emojis.TROPHY.asUnicodeEmoji().get();

        private final ArrayList<BiFunction<Message, User, Boolean>> onTrophyReact = new ArrayList<>();
        public EmojiReactHandler(String channelIdAsString) {
            this.channelIdAsString = channelIdAsString;
        }

        @Override
        public void accept(ReactionAddEvent event) {
            if(!event.getChannelId().asString().equals(channelIdAsString)) {
                return;
            }
            if (!event.getEmoji().asUnicodeEmoji().isPresent() || !event.getEmoji().asUnicodeEmoji().get().equals(TROPHY_UNICODE)) {
                return;
            }
            User user = event.getUser().block();
            if(user == null || user.isBot()) {
                return;
            }
            onTrophyReact.removeIf(func -> func.apply(event.getMessage().block(), user));   // Call all functions and remove them if they return true
        }

        public void addOnTrophyReact(BiFunction<Message, User,Boolean> consumer) {
            onTrophyReact.add(consumer);
        }

}
