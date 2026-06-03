package CompetitiveCounting.vaults;

import CompetitiveCounting.Counter;
import CompetitiveCounting.CountingBot;
import CompetitiveCounting.CountingContext;
import CompetitiveCounting.CountingEmojis;
import CompetitiveCounting.dialogue.Dialogue;
import CompetitiveCounting.vaults.vaultDrops.MoneyDrop;
import com.google.common.math.IntMath;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class VaultOfLongStrides extends Vault {
    public static final double SPAWN_CHANCE = 0.5;// 0.05;    // todo
    private static final int MIN_STRIDE_LENGTH = 10;  // todo
    private static final String RIDDLE = "What is the greatest common divisor of {0} and {1}?";
    private final static String NAME = "Vault of Long Strides";

    public VaultOfLongStrides() {
        super(SPAWN_CHANCE, (context) -> context.getCurrentNumber() - context.getLastNumber() >= MIN_STRIDE_LENGTH);
        addLootToLootPool(new MoneyDrop(80));
    }


    @Override
    public void spawn() {
        //
    }

    @Override
    public Counter doRiddleBlockingly(Message message, CountingContext context) {
        int x = randomInt(1, 20);
        int num1 = randomInt(1, 10)*x;
        int num2 = randomInt(1, 10)*x;
        int correctAnswer = IntMath.gcd(num1, num2);
        String riddle = RIDDLE.replace("{0}", String.valueOf(num1)).replace("{1}", String.valueOf(num2));
        String wholeRiddleText = RIDDLE_TEXT.replace("{author}", context.getCounter().getName()).replace("{riddle}", riddle);
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
                    return false;
                })
                .addEmojiReaction(CountingEmojis.KEY)
                .addWaitForEmojiReaction(CountingEmojis.KEY, false,
                        m -> {}, riddleSolverIdToBeFoundOut);
        setCurrentRiddleDialogue(riddleDialogue);
        riddleDialogue.playBlocking(message);
        return CountingBot.getCounter(message.getGuildId().get().asString(), riddleSolverIdToBeFoundOut.get());
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
        return "Spawns on counts which exceed the last count by at least " + MIN_STRIDE_LENGTH + ". ";
    }

    private int randomInt(int min, int max) {
        return min + (int) (Math.random() * (max - min));
    }
}
