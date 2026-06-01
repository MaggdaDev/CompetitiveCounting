package CompetitiveCounting.vaults;

import CompetitiveCounting.CountingContext;
import CompetitiveCounting.dialogue.Dialogue;
import discord4j.core.object.entity.Message;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Vault {
    private final double spawnChance;
    private final Function<CountingContext, Boolean> requirementsChecker;
    private Dialogue currentRiddleDialogue = null;
    protected final static String RIDDLE_TEXT = "{author} has located a locked vault :satellite:! To find the key, solve the following riddle:\n"
        + "> {riddle}\n-# To submit the key, use `~<key>`(e.g. `~42`).";
    public Vault(double spawnChance, Function<CountingContext, Boolean> requirementsChecker) {
        this.spawnChance = spawnChance;
        this.requirementsChecker = requirementsChecker;
    }

    public abstract void spawn();

    public abstract void doRiddle(Message message, CountingContext context);

    public boolean maybeSpawn(CountingContext context) {
        double rand = Math.random();
        boolean spawn = (requirementsChecker.apply(context) && rand < spawnChance);
        if (spawn) {
            spawn();
        }
        return spawn;
    }

    public void dispose() {
        if (currentRiddleDialogue != null) {
            currentRiddleDialogue.stop();
        }
    }

    protected void setCurrentRiddleDialogue(Dialogue currentRiddleDialogue) {
        this.currentRiddleDialogue = currentRiddleDialogue;
    }
}
