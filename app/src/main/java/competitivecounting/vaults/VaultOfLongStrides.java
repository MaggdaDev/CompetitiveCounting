package competitivecounting.vaults;

import com.google.common.math.IntMath;
import competitivecounting.CountingContext;
import competitivecounting.items.equippables.Equippables;
import competitivecounting.vaults.vaultDrops.ItemDrop;
import competitivecounting.vaults.vaultDrops.MoneyDrop;
import discord4j.core.object.entity.Message;

public class VaultOfLongStrides extends Vault {
    public static final double SPAWN_CHANCE = 1. / 8.;
    private static final int MIN_STRIDE_LENGTH = 8;
    private static final String RIDDLE = "What is the greatest common divisor of {0} and {1}?";
    private final static String NAME = "Vault of Long Strides";

    public VaultOfLongStrides() {
        super(SPAWN_CHANCE, (context) -> context.getCurrentNumber() - context.getLastNumber() >= MIN_STRIDE_LENGTH);
        addLootToLootPool(new MoneyDrop(95));
        addLootToLootPool(new ItemDrop(5, Equippables.POCKET_ABACUS));
    }

    @Override
    public RiddleDialogue createRiddleDialogue(Message message, CountingContext context) {
        int x = randomInt(1, 20);
        int num1 = randomInt(1, 10)*x;
        int num2 = randomInt(1, 10)*x;
        int correctAnswer = IntMath.gcd(num1, num2);

        String riddle = RIDDLE.replace("{0}", String.valueOf(num1)).replace("{1}", String.valueOf(num2));
        String wholeRiddleText = getRiddleText(riddle, context.getCounter().getName());
        RiddleDialogue currentRiddleDialogue = new RiddleDialogue();
        currentRiddleDialogue.addNpcLine(wholeRiddleText, 0);
        currentRiddleDialogue.addWaitForCorrectSolutionAndSetWinningUserRef(correctAnswer);
        return currentRiddleDialogue.addWaitForKeyReaction();
    }

    @Override
    protected String getVaultName() {
        return NAME;
    }

    @Override
    public String getSpawnConditionsDescription() {
        return "Spawns on counts which exceed the last count by at least " + MIN_STRIDE_LENGTH + ".";
    }

}
