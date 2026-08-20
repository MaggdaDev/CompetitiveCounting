package competitivecounting.items.equippables;

import competitivecounting.Counter;
import competitivecounting.CountingBot;
import competitivecounting.CountingContext;
import discord4j.core.object.entity.Message;

public class PocketAbacus extends Equippable {
    public final static String NAME = "Pocket:abacus:Abacus";
    private final static String DESCRIPTION = "When equipped, can be used to calculate the next count!";
    private final static String COLLECTION_DESCRIPTION = "When _used_, tells you the next correct count. ({0})\n-# Numbers calculated: {1}";
    private int uses = 0;
    private long lastUseSeconds = 0;
    private final static long COOLDOWN_SECONDS = 60;
    public PocketAbacus(Counter owner) {
        super(null, NAME, DESCRIPTION, owner);
    }

    @Override
    public String getCollectionDescription() {
        long now = java.time.Instant.now().getEpochSecond();
        String cdString = "";
        if (now - lastUseSeconds < COOLDOWN_SECONDS) {
            long secondsLeft = COOLDOWN_SECONDS - (now - lastUseSeconds);
            cdString = "*On cooldown*: " + secondsLeft + "s left...";
        } else {
            cdString = "Cooldown: {0}s".replace("{0}", String.valueOf(COOLDOWN_SECONDS));
        }
        return COLLECTION_DESCRIPTION
                .replace("{0}", cdString)
                .replace("{1}", String.valueOf(uses));
    }

    @Override
    public Equippable createObject(Counter owner) {
        return new PocketAbacus(owner);
    }

    @Override
    public boolean doCollectionUse(Message message, CountingContext context) {
        if (context == null) {
            CountingBot.write(message, "Please try again later!");
        }
        long now = java.time.Instant.now().getEpochSecond();
        if (now - lastUseSeconds < COOLDOWN_SECONDS) {
            long secondsLeft = COOLDOWN_SECONDS - (now - lastUseSeconds);
            CountingBot.write(message, "Your " + NAME + " is on cooldown! Please wait " + secondsLeft + " more seconds before using it again.");
            return true;
        }
        lastUseSeconds = now;
        uses++;
        String s = "Using your " + NAME + ", you computed that the next correct number will be " + context.getStreak().getNextCorrectNumberInBase() + ".";
        CountingBot.write(message, s);
        return true;
    }
}
