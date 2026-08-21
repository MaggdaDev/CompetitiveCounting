package competitivecounting.items;

import com.ibm.icu.impl.Assert;
import competitivecounting.*;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateMono;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CountingBoosterManagerTest {

    private Counter counter;
    private CountingContext context;
    private Message message;

    @BeforeEach
    void setUp() {
        GatewayDiscordClient mockedClient = Mockito.mock(GatewayDiscordClient.class);
        when(mockedClient.on(any(Class.class), any(Function.class)))
                .thenReturn(Flux.empty());
        when(mockedClient.on(any(Class.class)))
                .thenReturn(Flux.empty());
        CountingBot bot = new CountingBot(mockedClient);
        String guildId = "testGuild";
        CountingGuild guild = new CountingGuild(guildId);
        CountingBot.getInstance().getGuilds().put(guildId, guild);
        counter = new Counter(guildId, "", "");
        CountingStreak streak = new CountingStreak("", 10, "");
        context = new CountingContext(counter, 0, 1, streak, 0, "");

        // Message
        List<String> output = new ArrayList<>();
        message = mock(Message.class);
        MessageChannel channel = mock(MessageChannel.class);
        when(message.getChannel()).thenReturn(Mono.just(channel));

        when(channel.createMessage(anyString()))
                .thenAnswer(invocation -> {
                    String s = invocation.getArgument(0);
                    output.add(s);
                    return MessageCreateMono.of(channel);
                });
        when(channel.createMessage(any(MessageCreateSpec.class)))
                .thenReturn(Mono.empty());
    }

    @Test
    void testCountingBoosterIncomeMultiplier() {


        Assertions.assertEquals(0, counter.getPossibleTotal());
        counter.notifyCountAndGetScoreAdd(1, context);
        Assertions.assertEquals(1, counter.getPossibleTotal());

        counter.getInventory().addItem(Consumables.COUNTING_BOOSTER);
        counter.getInventory().addItem(Consumables.COUNTING_BOOSTER);
        Assertions.assertEquals(2, counter.getInventory().getAmountOfItem(Consumables.COUNTING_BOOSTER));
        counter.use(Consumables.COUNTING_BOOSTER, message, Optional.empty());

        counter.notifyCountAndGetScoreAdd(2, context);
        Assertions.assertEquals(4, counter.getPossibleTotal()); // 1 + 2 * 1.5 = 4
        Assertions.assertEquals(1, counter.getInventory().getAmountOfItem(Consumables.COUNTING_BOOSTER));
        counter.use(Consumables.COUNTING_BOOSTER, message, Optional.empty());
        Assertions.assertEquals(1, counter.getInventory().getAmountOfItem(Consumables.COUNTING_BOOSTER));
    }

    @Test
    void testSpawnRateModification() {
        CountingBoosterManager countingBoosterManager = new CountingBoosterManager(counter);
        double baseRate = 1e-4;
        assertTrue(Math.abs(baseRate - countingBoosterManager.modifyTrophyRate(baseRate)) < 1e-9);
        countingBoosterManager.activateCountingBoost(message);
        assertTrue(Math.abs(baseRate*CountingBoosterManager.TROPHY_SPAWN_RATE_MULTIPLIER - countingBoosterManager.modifyTrophyRate(baseRate)) < 1e-6);
    }

}