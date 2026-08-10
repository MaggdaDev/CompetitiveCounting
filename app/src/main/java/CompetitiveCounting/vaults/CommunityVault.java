package CompetitiveCounting.vaults;

import CompetitiveCounting.Counter;
import CompetitiveCounting.CountingBot;
import CompetitiveCounting.CountingContext;
import CompetitiveCounting.CountingEmojis;
import CompetitiveCounting.dialogue.Dialogue;
import CompetitiveCounting.dialogue.ParallelDialogElementsBuilder;
import CompetitiveCounting.interactionhandlers.SlashCommandHandler;
import CompetitiveCounting.items.equippables.GoodBadUgly;
import CompetitiveCounting.vaults.vaultDrops.ItemDrop;
import CompetitiveCounting.vaults.vaultDrops.MoneyDrop;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class CommunityVault extends Vault {
    private final static double SPAWN_CHANCE = 0.03;
    private final static String RIDDLE = "The key for this vault will be determined in {0} seconds. The initial suggestion for the key is {1}, "
            + "but everyone may suggest their own key using the command `/" + SlashCommandHandler.SUBMIT_KEY_COMMAND +  "`. "
            + "In the end, the vault will be locked using the key that is closest to 2/3 of the average over all the submitted keys and the initially suggested"
            + " key. ";
    private final static int TOTAL_RIDDLE_TIME = 30;
    private final static long TIME_INTERVAL_COMMUNITY_COUNT = 20;
    private Dialogue currentRiddleDialogue = null;

    public CommunityVault() {
        super(SPAWN_CHANCE, context -> {
            long now = Instant.now().getEpochSecond();
            int differentCountersOtherThanCurrentInLastSeconds = 0;
            for(Map.Entry<String,Long> entry : context.getStreak().getLastCountingTimesPerCounter().entrySet()) {
                String counterId = entry.getKey();
                long lastCountingSecond = entry.getValue();
                if (counterId.equals(context.getCounter().getId())) {
                    continue;
                }
                if (now - lastCountingSecond <= TIME_INTERVAL_COMMUNITY_COUNT) {
                    differentCountersOtherThanCurrentInLastSeconds++;
                }
                if (differentCountersOtherThanCurrentInLastSeconds >= 2) {
                    return true;
                }
            }
            return false;
        });
        super.addLootToLootPool(new MoneyDrop(95));
        super.addLootToLootPool(new ItemDrop(5, new GoodBadUgly(null)));
    }

    @Override
    public void spawn() {
        //

    }

    @Override
    public Counter doRiddleBlockingly(Message message, CountingContext context) {
        int x = randomInt(10, 100);
        String vaultId = String.valueOf(randomInt(1000, 9999));
        String riddleText = getRiddleText(
                RIDDLE.replace("{0}", String.valueOf(TOTAL_RIDDLE_TIME))
                        .replace("{1}", String.valueOf(x))
                        .replace("{2}", vaultId), context.getCounter().getName());
        HashMap<String, Integer> submittedKeysByUserId = new HashMap<>();
        AtomicReference<String> winningUserIdRef = new AtomicReference<>();
        currentRiddleDialogue = new Dialogue();
                currentRiddleDialogue.addNpcLine(riddleText, 0)
                        .addKeySubmissionAwaiter((userId, key) -> {
                            InteractionApplicationCommandCallbackSpec.Builder specBuilder = InteractionApplicationCommandCallbackSpec.builder()
                                    .ephemeral(true);
                            if (key < 0) {
                                specBuilder.content("Please submit a non-negative integer as key suggestion!");
                            } else if (key > Integer.MAX_VALUE / 100) {
                                specBuilder.content("Please do not submit a suggestion near the integer limit!");
                            } else {
                                int keyInt = (int) key;
                                if (submittedKeysByUserId.containsKey(userId)) {
                                    specBuilder.content("You have already submitted a key suggestion for this vault!");
                                } else {
                                    submittedKeysByUserId.put(userId, keyInt);
                                    specBuilder.content("You have submitted the key " + keyInt + ".");
                                }
                            }
                            return specBuilder.build();
                        }, new CountDownLatch(1), () -> false,TOTAL_RIDDLE_TIME)
                .addRunnable(m -> {
                    double totalSum = x;
                    for (int submittedKey : submittedKeysByUserId.values()) {
                        totalSum += submittedKey;
                    }
                    double average = totalSum / (submittedKeysByUserId.size() + 1);
                    String reducedAverage = String.format(Locale.US, "%.2f", average * 2 / 3);
                    String winningUserId = getWinningUserID(average, x, submittedKeysByUserId);
                    winningUserIdRef.set(winningUserId);
                    if (submittedKeysByUserId.isEmpty()) {
                        CountingBot.write(m, "No key suggestions were submitted - this vault will remain locked!");
                        currentRiddleDialogue.cancelAllRemaining();
                        return;
                    } else if (winningUserId.isEmpty()) {
                        CountingBot.write(m, "Even though " + submittedKeysByUserId.size() + " suggestions were submitted, but the initial suggestion of " + x + " was closest to 2/3 of the average, which is " + reducedAverage + "."
                                + " Try again with the next " + getVaultName() + "!");
                        currentRiddleDialogue.cancelAllRemaining();
                        return;
                    }
                    CountingBot.write(m, "Time's up! " + submittedKeysByUserId.size() + " suggestions were submitted. The vault is now locked "
                            + " with the key that is closest to " + reducedAverage + ". To find out who got the key right, please"
                            + " all write your suggestions into this channel now, using the syntax `~[key]`!");
                })
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
                    String authorId = msg.getAuthor().get().getId().asString();
                    Counter counter = CountingBot.getCounter(msg.getGuildId().get().asString(), authorId);
                    if (!submittedKeysByUserId.containsKey(authorId)) {
                        CountingBot.write(msg, "You did not submit a key suggestion for this vault, " + counter.getName() + "!");
                        return false;
                    }
                    if (submittedKeysByUserId.get(authorId) != answer) {
                        CountingBot.write(msg, "This is not the key you submitted, " + counter.getName() + "!");
                        return false;
                    }
                    return authorId.equals(winningUserIdRef.get());
                })
                .addEmojiReaction(CountingEmojis.KEY)
                .addWaitForEmojiReaction(CountingEmojis.KEY, false,
                        m -> {
                        }, winningUserIdRef);
        ;
        currentRiddleDialogue.playBlocking(message);
        return CountingBot.getCounter(message.getGuildId().get().asString(), winningUserIdRef.get());
    }

    /**
     *
     * @param average
     * @param x
     * @param submittedKeysByUserId
     * @return the winning key, or -1 if the bot won
     */
    private static String getWinningUserID(double average, int x, HashMap<String, Integer> submittedKeysByUserId) {
        double reducedAverage = average * 2 / 3;
        String winningUserId = "";
        double closestDistance = Double.MAX_VALUE;
        for (Map.Entry<String, Integer> entries : submittedKeysByUserId.entrySet()) {
            double distance = Math.abs(entries.getValue() - reducedAverage);
            if (distance < closestDistance) {
                closestDistance = distance;
                winningUserId = entries.getKey();
            }
        }
        if (Math.abs(x - reducedAverage) < closestDistance) {
            winningUserId = "";
        }
        return winningUserId;
    }

    private void sendNotUnderstoodMessage(Message message) {
        CountingBot.write(message, "Please check again the syntax of your command. Keep in mind that most of the commands do not work in DMs.");
    }

    @Override
    public void reset() {
        if (currentRiddleDialogue != null) {
            currentRiddleDialogue.stop();
        }
        currentRiddleDialogue = null;
    }

    @Override
    protected String getVaultName() {
        return "Community Vault";
    }

    @Override
    public String getSpawnConditionsDescription() {
        return "Spawns if at least two counters other than you have counted recently.";
    }
    @Override
    protected String getRiddleText(String riddle, String author) {
        String text = RIDDLE_TEXT.replace("{author}", author).replace("{riddle}", riddle);
        text = text.substring(0, text.indexOf("-#"));
        return text;
    }
}
