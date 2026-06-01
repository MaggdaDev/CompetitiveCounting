package CompetitiveCounting.vaults;

import CompetitiveCounting.CountingBot;
import CompetitiveCounting.CountingContext;
import CompetitiveCounting.CountingEmojis;
import CompetitiveCounting.dialogue.Dialogue;
import com.google.common.math.IntMath;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

import java.util.Optional;

public class VaultOfLongStrides extends Vault {
    public static final double SPAWN_CHANCE = 1;// 0.05;    // todo
    private static final int MIN_STRIDE_LENGTH = 0; //; 10;  // todo
    private static final String RIDDLE = "What is the greatest common divisor of {0} and {1}?";

    public VaultOfLongStrides() {
        super(SPAWN_CHANCE, (context) -> context.getCurrentNumber() - context.getLastNumber() >= MIN_STRIDE_LENGTH);
    }


    @Override
    public void spawn() {

    }

    @Override
    public void doRiddle(Message message, CountingContext context) {
        int min = 10;
        int max = 100;
        int num1 = randomInt(min, max);
        int num2 = randomInt(min, max);
        int correctAnswer = IntMath.gcd(num1, num2);
        String riddle = RIDDLE.replace("{0}", String.valueOf(num1)).replace("{1}", String.valueOf(num2));
        String wholeRiddleText = RIDDLE_TEXT.replace("{author}", context.getCounter().getName()).replace("{riddle}", riddle);
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
                    return answer == correctAnswer;
                })
                .addEmojiReaction(CountingEmojis.KEY)
                .addRunnable((m) -> setCurrentRiddleDialogue(null));
        setCurrentRiddleDialogue(riddleDialogue);
        riddleDialogue.play(message);
    }

    private int randomInt(int min, int max) {
        return min + (int) (Math.random()* (max - min));
    }
}
