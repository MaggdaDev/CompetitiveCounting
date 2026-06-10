package CompetitiveCounting.vaults;

import CompetitiveCounting.Counter;
import CompetitiveCounting.CountingContext;
import CompetitiveCounting.dialogue.Dialogue;
import CompetitiveCounting.vaults.vaultDrops.VaultDrop;
import CompetitiveCounting.vaults.vaultDrops.VaultLootPool;
import discord4j.core.object.entity.Message;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Vault {
    private final double spawnChance;
    private final Function<CountingContext, Boolean> requirementsChecker;
    private Dialogue currentRiddleDialogue = null;
    private final VaultLootPool lootPool;
    protected final static String RIDDLE_TEXT = "{author} has located a locked vault :satellite:! To find the key, solve the following riddle:\n"
        + "> {riddle}\n-# To submit the key, use `~<key>`(e.g. `~42`).";
    public Vault(double spawnChance, Function<CountingContext, Boolean> requirementsChecker) {
        this.spawnChance = spawnChance;
        this.requirementsChecker = requirementsChecker;
        lootPool = new VaultLootPool();
    }

    protected void addLootToLootPool(VaultDrop drop) {
        lootPool.addDrop(drop);
    }

    public abstract void spawn();

    /**
     *
     * @param message
     * @param context
     * @return the riddle solver
     */
    public abstract Counter doRiddleBlockingly(Message message, CountingContext context);

    public void loot(Message message, Counter riddleSolver) {
        Dialogue dialogue = new Dialogue();
        dialogue.addNpcLine(riddleSolver.getName() + " opened a " + getVaultName() + "...", 1000);
        VaultDrop drop = lootPool.drawDrop();
        drop.payout(message, dialogue, riddleSolver);
        dialogue.playBlocking(message);
    }

    public abstract void reset();

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

    protected abstract String getVaultName();
    public abstract String getSpawnConditionsDescription();

    public boolean canSpawn(CountingContext context) {
        return requirementsChecker.apply(context);
    }

    public double getSpawnChance() {
        return spawnChance;
    }

}
