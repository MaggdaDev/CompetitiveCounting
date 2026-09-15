package competitivecounting.vaults;

import competitivecounting.Counter;
import competitivecounting.CountingBot;
import competitivecounting.CountingContext;
import competitivecounting.interactionhandlers.SlashCommandHandler;
import competitivecounting.items.equippables.GoodBadUgly;
import competitivecounting.vaults.vaultDrops.ItemDrop;
import competitivecounting.vaults.vaultDrops.MoneyDrop;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class CommunityVault extends Vault {
    public final static int MIN_KEY = 0, MAX_KEY = 100;
    private final static double EXTRA_CONDITION_FACT = 2.;
    public final static String NAME = "Community Vault";
    private final static double SPAWN_CHANCE = 1. / 40.;
    private final static String RIDDLE = "The key for this vault will be determined in {0} seconds. " +
            "Everyone may suggest a number between " + MIN_KEY + " and " + MAX_KEY + " using the command `/" +
            SlashCommandHandler.SUBMIT_KEY_COMMAND + "`. Win by being the first to suggest a key meeting one of the following 2 win conditions: " +
            "Be either the closest to 2/3 of the average, or at least " + EXTRA_CONDITION_FACT + "x as large!{1}";
    private final static int TOTAL_RIDDLE_TIME = 20;
    private final static long TIME_INTERVAL_COMMUNITY_COUNT = 20;

    private double lastAverage = -1;

    public CommunityVault() {
        super(SPAWN_CHANCE, context -> {
            long now = Instant.now().getEpochSecond();
            int differentCountersOtherThanCurrentInLastSeconds = 0;
            for (Map.Entry<String, Long> entry : context.getStreak().getLastCountingTimesPerCounter().entrySet()) {
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
    public RiddleDialogue createRiddleDialogue(Message message, CountingContext context) {
        String riddleText = getRiddleText(
                RIDDLE.replace("{0}", String.valueOf(TOTAL_RIDDLE_TIME)), context.getCounter().getName())
                .replace("{1}", lastAverage == -1 ?
                        "" : "\n-# Last average in this streak: " + String.format(Locale.US, "%.2f", lastAverage) + ".");
        LinkedHashMap<String, Integer> submittedKeysByUserId = new LinkedHashMap<>();
        RiddleDialogue riddleDialogue = new RiddleDialogue();
        riddleDialogue.addNpcLine(riddleText, 0)
                .addKeySubmissionAwaiter((userId, key) -> {
                    InteractionApplicationCommandCallbackSpec.Builder specBuilder = InteractionApplicationCommandCallbackSpec.builder()
                            .ephemeral(true);
                    int keyInt = (int) key;
                    if (submittedKeysByUserId.containsKey(userId)) {
                        specBuilder.content("You have already submitted a key suggestion for this vault!");
                    } else {
                        submittedKeysByUserId.put(userId, keyInt);
                        specBuilder.content("You have submitted the key " + keyInt + ".");
                    }
                    return specBuilder.build();
                }, new CountDownLatch(1), () -> false, TOTAL_RIDDLE_TIME)
                .addRunnable(m -> {
                    double totalSum = 0;
                    for (int submittedKey : submittedKeysByUserId.values()) {
                        totalSum += submittedKey;
                    }
                    double average = totalSum / (submittedKeysByUserId.size());
                    System.out.println("Average: " + average);
                    String reducedAverage = String.format(Locale.US, "%.1f", average * 2. / 3.);
                    String winningUserId = determineWinner(average, submittedKeysByUserId);
                    riddleDialogue.setRiddleSolverId(winningUserId);
                    if (submittedKeysByUserId.isEmpty()) {
                        CountingBot.write(m, "No key suggestions were submitted - this vault will remain locked!");
                        riddleDialogue.cancelAllRemaining();
                        return;
                    }
                    lastAverage = average;
                    String timesUpMsg = getTimesUpString(submittedKeysByUserId, reducedAverage, average);
                    CountingBot.write(m,  timesUpMsg );
                });
        riddleDialogue.addWaitForCorrectSolutionAndSetWinningUserRef((msg, answer) -> {
            String authorId = msg.getAuthor().get().getId().asString();
            Counter counter = CountingBot.getCounter(msg.getGuildId().get().asString(), authorId);
            if (!submittedKeysByUserId.containsKey(authorId)) {
                CountingBot.write(msg, "You did not submit a key suggestion for this vault, " + counter.getName() + "!");
                return false;
            }
            if (!Objects.equals(submittedKeysByUserId.get(authorId), answer)) {
                CountingBot.write(msg, "This is not the key you submitted, " + counter.getName() + "!");
                return false;
            }
            return authorId.equals(riddleDialogue.getRiddleSolverRef().get());
        });
        return riddleDialogue.addWaitForKeyReaction();
    }

    private static String getTimesUpString(LinkedHashMap<String, Integer> submittedKeysByUserId, String reducedAverage, double average) {
        String pluralOrSingular = submittedKeysByUserId.size() == 1 ? " suggestion was" : " suggestions were";
        String timesUpMsg = "Time's up! " + submittedKeysByUserId.size() + pluralOrSingular + " submitted with average " + format(average) +  ". Win condition";
        if (average * EXTRA_CONDITION_FACT <= MAX_KEY) {
            timesUpMsg += "s:\n- Key ≥ " + (int) Math.ceil(average * EXTRA_CONDITION_FACT)
             + "\n- 2) Key closest to " + reducedAverage;
        } else {
            timesUpMsg +=":\n- Key closest to " + reducedAverage;
        }
        return timesUpMsg + "\nTo find out who got the key right, please"
                + " all write your suggestions into this channel now, using the syntax `~[key]`!";
    }

    /**
     *
     * @param average
     * @param submittedKeysByUserId
     * @return the winning key, or -1 if the bot won
     */
    private String determineWinner(double average, LinkedHashMap<String, Integer> submittedKeysByUserId) {
        double reducedAverage = average * 2. / 3.;
        // Check extra condition
        String extraConditionWinner = "";
        for (Map.Entry<String, Integer> entries : submittedKeysByUserId.entrySet()) {
            if (entries.getValue() >=  (average * EXTRA_CONDITION_FACT)) {
                extraConditionWinner = entries.getKey();
                break;
            }
        }
        String usualWinnerId = "";
        double closestDistance = Double.MAX_VALUE;
        for (Map.Entry<String, Integer> entries : submittedKeysByUserId.entrySet()) {
            double distance = Math.abs(entries.getValue() - reducedAverage);
            if (distance < closestDistance) {
                closestDistance = distance;
                usualWinnerId = entries.getKey();
            }
        }
        for (Map.Entry<String, Integer> entries : submittedKeysByUserId.entrySet()) {
            if (Objects.equals(entries.getKey(), extraConditionWinner) || Objects.equals(entries.getKey(), usualWinnerId)) {
                return entries.getKey();
            }
        }
        return "";
    }

    private void sendNotUnderstoodMessage(Message message) {
        CountingBot.write(message, "Please check again the syntax of your command. Keep in mind that most of the commands do not work in DMs.");
    }

    @Override
    public String getVaultName() {
        return NAME;
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

    private static String format(double d) {
        return String.format(Locale.US, "%.1f",d);
    }
}
