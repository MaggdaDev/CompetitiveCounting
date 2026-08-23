package competitivecounting.vaults.trophyvault;

import competitivecounting.Counter;
import competitivecounting.CountingBot;
import competitivecounting.CountingContext;
import competitivecounting.CountingEmojis;
import competitivecounting.dialogue.Dialogue;
import competitivecounting.items.equippables.Equippables;
import competitivecounting.vaults.Riddle;
import competitivecounting.vaults.Vault;
import competitivecounting.vaults.vaultDrops.ItemDrop;
import competitivecounting.vaults.vaultDrops.MoneyDrop;
import discord4j.core.object.entity.Message;

import java.util.concurrent.atomic.AtomicReference;

public class TrophyVault extends Vault {
    public static final double SPAWN_CHANCE = 0.1;
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
    public Counter doRiddleBlockingly(Message message, CountingContext context) {
        Riddle riddle = createRiddle();
        String wholeRiddleText = getRiddleText(riddle.getQuestion(), context.getCounter().getName());
        int correctAnswer = riddle.getAnswer();
        AtomicReference<String> riddleSolverIdToBeFoundOut = new AtomicReference<>();
        Dialogue riddleDialogue = new Dialogue().addNpcLine(wholeRiddleText, 0)
                .addWaitForUserAnswer((msg) -> {
                    String content = msg.getContent().trim().toLowerCase();
                    if (!content.startsWith("~")) {
                        return false;
                    }
                    String answerStr = content.substring(1);
                    int answer;
                    try {
                        answer = Integer.parseInt(answerStr);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    if (answer == correctAnswer && msg.getAuthor().isPresent()) {
                        riddleSolverIdToBeFoundOut.set(msg.getAuthor().get().getId().asString());
                        return true;
                    }
                    boolean tooHigh = answer > correctAnswer;
                    msg.addReaction(tooHigh ? CountingEmojis.ARROW_DOWN : CountingEmojis.ARROW_UP).subscribe();
                    return false;
                })
                .addEmojiReaction(CountingEmojis.KEY)
                .addNpcLineButKeepOldMessage(riddle.getSolutionExplanation(), 0)
                .addWaitForEmojiReaction(CountingEmojis.KEY, false,
                        m -> {}, riddleSolverIdToBeFoundOut);
        setCurrentRiddleDialogue(riddleDialogue);
        riddleDialogue.playBlocking(message);
        return CountingBot.getCounter(message.getGuildId().get().asString(), riddleSolverIdToBeFoundOut.get());
    }


    public Riddle createRiddle() {
        Riddle riddle = null;
        int counter = 0;
        while(riddle == null) {
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
    public void spawn() {
        //
    }



    @Override
    public void reset() {
        setCurrentRiddleDialogue(null);
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
