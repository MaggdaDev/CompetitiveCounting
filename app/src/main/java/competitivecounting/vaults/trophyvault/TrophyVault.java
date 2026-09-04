package competitivecounting.vaults.trophyvault;

import competitivecounting.Counter;
import competitivecounting.CountingContext;
import competitivecounting.CountingEmojis;
import competitivecounting.items.equippables.Equippables;
import competitivecounting.vaults.Riddle;
import competitivecounting.vaults.RiddleDialogue;
import competitivecounting.vaults.Vault;
import competitivecounting.vaults.vaultDrops.ItemDrop;
import competitivecounting.vaults.vaultDrops.MoneyDrop;
import discord4j.core.object.entity.Message;

public class TrophyVault extends Vault {
    public static final double SPAWN_CHANCE = 1. / 8.;
    private final static String NAME = "Vault of Trophies";
    private final static String SPAWN_CONDITIONS_DESCRIPTION = "Spawns on counts whose trophy you own.";

    private final TrophyVaultWikipedia wiki;

    public TrophyVault() {
        super(SPAWN_CHANCE, context -> {
            Counter counter = context.getCounter();
            int number = context.getCurrentNumber();
            return counter.getOwnedTrophies().contains(number);
        });
        wiki = new TrophyVaultWikipedia();

        addLootToLootPool(new MoneyDrop(95));
        addLootToLootPool(new ItemDrop(5, Equippables.DOWSING_ROD));
    }

    @Override
    public RiddleDialogue createRiddleDialogue(Message message, CountingContext context) {
        Riddle riddle = createRiddle();
        String wholeRiddleText = getRiddleText(riddle.getQuestion(), context.getCounter().getName());
        int correctAnswer = riddle.getAnswer();
        RiddleDialogue riddleDialogue = new RiddleDialogue();
        riddleDialogue.addNpcLine(wholeRiddleText, 0);
        riddleDialogue.addWaitForCorrectSolutionAndSetWinningUserRef((msg, answer) -> {
            if (answer == correctAnswer) {
                return true;
            }
            boolean tooHigh = answer > correctAnswer;
            msg.addReaction(tooHigh ? CountingEmojis.ARROW_DOWN : CountingEmojis.ARROW_UP).subscribe();
            return false;
        });
        return riddleDialogue.addWaitForKeyReaction(riddle.getSolutionExplanation());
    }


    public Riddle createRiddle() {
        Riddle riddle = null;
        int counter = 0;
        while (riddle == null) {
            counter++;
            try {
                WikiArticelObject obj = wiki.getRandomPage();
                if (obj == null) {
                    Thread.sleep(50);
                    continue;
                }
                riddle = TrophyRiddleFactory.createRiddle(obj);
            } catch (Exception e) {
                riddle = null;
            }
            if (riddle == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        System.out.println("Riddle created after " + counter + " attempts.");
        return riddle;
    }

    @Override
    protected String getVaultName() {
        return NAME;
    }

    @Override
    public String getSpawnConditionsDescription() {
        return SPAWN_CONDITIONS_DESCRIPTION;
    }
}
