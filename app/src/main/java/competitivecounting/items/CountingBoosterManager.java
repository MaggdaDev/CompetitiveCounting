package competitivecounting.items;

import competitivecounting.Counter;
import competitivecounting.CountingBot;
import competitivecounting.Util;
import discord4j.core.object.entity.Message;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CountingBoosterManager {

    private final Counter owner;
    private final static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private boolean isCountingBoostActive = false;

    public final static int DURATION_MINUTES = 10;

    public final static double EXTRA_INCOME_MULTIPLIER = 1.5, TROPHY_SPAWN_RATE_MULTIPLIER = 2., VAULT_SPAWN_RATE_MULTIPLIER = 2.;

    private long epochSecondsAtActivation = 0;

    public CountingBoosterManager(Counter owner) {
        this.owner = owner;
    }

    public void activateCountingBoost(Message message) {
        if (isCountingBoostActive) {
            throw new IllegalStateException("Counting boost is already active.");
        }
        epochSecondsAtActivation = System.currentTimeMillis() / 1000;
        isCountingBoostActive = true;
        CountingBot.write(message, "You activated a " + Consumables.COUNTING_BOOSTER + "! For the next " + DURATION_MINUTES + " minutes, you gain:\n"
                + " - " + Util.bonusMultToAddPercString(EXTRA_INCOME_MULTIPLIER) + " extra income\n"
            + " - " + Util.bonusMultToAddPercString(TROPHY_SPAWN_RATE_MULTIPLIER) + " trophy spawn rate\n"
                + " - " + Util.bonusMultToAddPercString(VAULT_SPAWN_RATE_MULTIPLIER) + " vault spawn rate");

        scheduler.schedule(() -> {
            isCountingBoostActive = false;
            CountingBot.write(message, "Your counting boost has expired, " + owner.getPing() + "!");
        }, DURATION_MINUTES, TimeUnit.MINUTES);
    }

    public boolean isCountingBoostActive() {
        return isCountingBoostActive;
    }

    public String getTimeUntilBoostExpires() {
        if (!isCountingBoostActive) {
            return "";
        }
        long currentEpochSeconds = System.currentTimeMillis() / 1000;
        long secondsLeft = (epochSecondsAtActivation + DURATION_MINUTES * 60) - currentEpochSeconds;
        if (secondsLeft <= 0) {
            isCountingBoostActive = false;
            return "No active counting boost.";
        }
        long minutes = secondsLeft / 60;
        long seconds = secondsLeft % 60;
        return String.format("%02d:%02d min", minutes, seconds);
    }

    public double getCurrentIncomeFact() {
        if (isCountingBoostActive) {
            return EXTRA_INCOME_MULTIPLIER;
        } else {
            return 1.;
        }
    }

    public double modifyTrophyRate(double spawnThreshold) {
        return multiplyThreshold(spawnThreshold, isCountingBoostActive? TROPHY_SPAWN_RATE_MULTIPLIER : 1.);
    }

    public double modifyVaultRate(double spawnThreshold) {
        return multiplyThreshold(spawnThreshold, isCountingBoostActive? VAULT_SPAWN_RATE_MULTIPLIER : 1.);
    }

    private double multiplyThreshold(double threshold, double multiplier) {
        return 1. - Math.pow(1. - threshold, multiplier);
    }
}
