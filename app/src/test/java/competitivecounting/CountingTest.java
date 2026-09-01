package competitivecounting;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.MessageCreateMono;
import discord4j.core.spec.MessageCreateSpec;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CountingTest {

    protected Counter counter;
    protected CountingContext context;
    protected Message message;

    private final Sinks.Many<ReactionAddEvent> reactionEvents =
            Sinks.many().multicast().onBackpressureBuffer();
    private GatewayDiscordClient mockedClient;
    private final HashMap<String, User> mockedUsers = new HashMap<>();
    protected final static String CHANNEL_ID = "28754";
    protected final static String MESSAGE_ID = "184723984";
    protected final static String COUNTER_ID = "23582734", OTHER_COUNTER_ID = "92848378";
    protected final static String GUILD_ID = "3847201";
    protected List<String> output;
    private Counter otherCounter;

    @BeforeEach
    protected void setUp() {
        mockedClient = Mockito.mock(GatewayDiscordClient.class);
        when(mockedClient.on(any(Class.class), any(Function.class)))
                .thenReturn(Flux.empty());
        when(mockedClient.on(any(Class.class))).thenAnswer(invocation -> {
            Class<?> eventType = invocation.getArgument(0);
            if (eventType == ReactionAddEvent.class) {
                return reactionEvents.asFlux().publishOn(Schedulers.boundedElastic());
            }
            return Flux.empty();
        });
        when(mockedClient.getUserById(any(Snowflake.class)))
                .thenAnswer(invocation -> {
                    Snowflake id = invocation.getArgument(0);
                    return Mono.justOrEmpty(mockedUsers.get(id.asString()));
                });
        when(mockedClient.getMessageById(any(Snowflake.class), any(Snowflake.class)))
                .thenAnswer(invocation -> {
                    Snowflake channelId = invocation.getArgument(0);
                    Snowflake messageId = invocation.getArgument(1);
                    if (channelId.asString().equals(CHANNEL_ID) && messageId.asString().equals(MESSAGE_ID)) {
                        return Mono.just(message);
                    }
                    return Mono.empty();
                });
        mockedUsers.put(COUNTER_ID, mock(User.class));
        mockedUsers.put(OTHER_COUNTER_ID, mock(User.class));
        when(mockedUsers.get(COUNTER_ID).getId()).thenReturn(Snowflake.of(COUNTER_ID));
        when(mockedUsers.get(OTHER_COUNTER_ID).getId()).thenReturn(Snowflake.of(OTHER_COUNTER_ID));

        CountingBot bot = new CountingBot(mockedClient);
        CountingGuild guild = new CountingGuild(GUILD_ID);
        CountingBot.getInstance().getGuilds().put(GUILD_ID, guild);
        counter = new Counter(GUILD_ID, COUNTER_ID, "user1");
        otherCounter = new Counter(GUILD_ID, OTHER_COUNTER_ID, "user2");
        guild.addNewCounter(COUNTER_ID, counter.getName());
        guild.addNewCounter(OTHER_COUNTER_ID, otherCounter.getName());
        CountingStreak streak = new CountingStreak(CHANNEL_ID, 10, "");
        context = new CountingContext(counter, 0, 1, streak, 0, "");

        // Message
        output = new ArrayList<>();
        message = mock(Message.class);
        when(message.addReaction(any())).thenReturn(Mono.empty());
        MessageChannel channel = mock(MessageChannel.class);
        when(message.getChannel()).thenReturn(Mono.just(channel));
        when(message.getChannelId()).thenReturn(Snowflake.of(CHANNEL_ID));
        when(message.getId()).thenReturn(Snowflake.of(MESSAGE_ID));
        when(message.getGuildId()).thenReturn(Optional.of(Snowflake.of(GUILD_ID)));

        MessageCreateMono messageCreateMono = mock(MessageCreateMono.class);
        when(messageCreateMono.block())
                .thenReturn(message);
        when(channel.createMessage(anyString()))
                .thenAnswer(invocation -> {
                    String s = invocation.getArgument(0);
                    output.add(s);
                    return messageCreateMono;
                });
        when(channel.createMessage(any(MessageCreateSpec.class)))
                .thenReturn(Mono.empty());

    }

    protected void simulateEmojiReaction(String reactorUserId, ReactionEmoji emoji) throws InterruptedException {
        ReactionAddEvent event = new ReactionAddEvent(mockedClient, null, Long.parseLong(reactorUserId), Long.parseLong(CHANNEL_ID),
                Long.parseLong(MESSAGE_ID),null,emoji, null, Long.parseLong(counter.getId()));
        reactionEvents.tryEmitNext(event);
        Thread.sleep(100);
    }

    protected void simulateEmojiReaction(ReactionEmoji emoji) throws InterruptedException {
        ReactionAddEvent event = new ReactionAddEvent(mockedClient, null, Long.parseLong(COUNTER_ID), Long.parseLong(CHANNEL_ID),
                Long.parseLong(MESSAGE_ID),null,emoji, null, Long.parseLong(counter.getId()));
        reactionEvents.tryEmitNext(event);
        Thread.sleep(100);
    }
}
