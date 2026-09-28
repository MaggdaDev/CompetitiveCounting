package competitivecounting.items.equippables;

import competitivecounting.*;
import discord4j.core.object.entity.Message;

public class PocketAbacus extends Equippable {
    public final static String NAME = "Pocket\uD83E\uDDEEAbacus";
    private final static String DESCRIPTION = "When equipped, can be used to calculate the next count!";
    private final static String COLLECTION_DESCRIPTION = "When _used_, tells you the next correct count. ({0})\n-# Numbers calculated: {1}";
    private int uses = 0;
    private long lastUseSeconds = 0;
    private final static long BASE_COOLDOWN_SECONDS = 60;
    public PocketAbacus(Counter owner) {
        super(null, NAME, DESCRIPTION, owner);
    }

    @Override
    public String getCollectionDescription() {
        long now = java.time.Instant.now().getEpochSecond();
        String cdString = "";
        long timeSinceLastUse = now - lastUseSeconds;
        if (timeSinceLastUse < getCooldownSeconds()) {
            cdString = "*On cooldown*: " + getCooldownString(timeSinceLastUse) + " left...";
        } else {
            cdString = "Ready to use! Cooldown: " + getCooldownString(0);
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
        long timeSinceLastUse = now - lastUseSeconds;
        if (timeSinceLastUse < getCooldownSeconds()) {
            CountingBot.write(message, "Your " + NAME + " is on cooldown! Please wait " +
                    getCooldownString(timeSinceLastUse)+ " before using it again.");
            return true;
        }
        lastUseSeconds = now;
        uses++;
        CountingStreak streak = context.getStreak();
        String s = "Using your " + NAME + ", you computed that the next correct number will be " + Util.getNumberInBaseString(streak.getNextNum(), streak.getBase(), true) + ".";
        CountingBot.write(message, s);
        return true;
    }

    private String getCooldownString(long timeSinceLastUse) {
        int secondsLeft = (int)(getCooldownSeconds() - timeSinceLastUse);
        int baseSecondsLeft = (int)(BASE_COOLDOWN_SECONDS - timeSinceLastUse);
        return Util.valueAndValueWithBoniToString(baseSecondsLeft, secondsLeft) + "s";
    }

    private long getCooldownSeconds() {
        return Math.round(decreaseInverseStatFromLevel(BASE_COOLDOWN_SECONDS));
    }
}
