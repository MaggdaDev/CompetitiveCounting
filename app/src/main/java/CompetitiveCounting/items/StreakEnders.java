package CompetitiveCounting.items;

import CompetitiveCounting.Counter;
import CompetitiveCounting.CountingBot;
import CompetitiveCounting.CountingEmojis;
import CompetitiveCounting.CountingStreak;
import CompetitiveCounting.dialogue.Dialogue;
import CompetitiveCounting.dialogue.ParallelDialogElementsBuilder;
import discord4j.core.object.entity.Message;

import java.time.Instant;
import java.util.*;

public class StreakEnders {
    private final static double PERC_COUNTERS_NEEDED = 2. / 3.;
    private final CountingStreak streak;

    private Dialogue currentWhiteStreakEnderDialogue;
    private long epochSecondAtLastWhiteStreakEnderRequested = 0;
    private final static long WHITE_STREAK_ENDER_REQUEST_TIMEOUT_SECONDS = 60 * 60 * 3;    // 3 hours
    public StreakEnders(CountingStreak streak) {
        this.streak = streak;
    }

    private long now() {
        return Instant.now().getEpochSecond();
    }

    public void whiteUsed(Message message, Counter itemUser) {
        //
        if(currentWhiteStreakEnderDialogue != null) {
            if(now() - epochSecondAtLastWhiteStreakEnderRequested < WHITE_STREAK_ENDER_REQUEST_TIMEOUT_SECONDS) {
                CountingBot.write(message, "A Streak:white_circle:Ender in this streak is still waiting for reactions! If no one reacts, it will timeout " + (WHITE_STREAK_ENDER_REQUEST_TIMEOUT_SECONDS / 3600) + " hours after it has been requested. ");
                return;
            } else {
                CountingBot.write(message, "Cancelling a previous Streak:white_circle:Ender due to timeout...");
                currentWhiteStreakEnderDialogue.stop();
                currentWhiteStreakEnderDialogue = null;
            }
        }
        String[] sortedIds = streak.getAmountOfCountsPerCounter().entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .toArray(String[]::new);
        ArrayList<String> idsOfCountersNeededToContribute = new ArrayList<>();
        double totalCounts = streak.getTotalAmountOfCounts();
        double countsContributedOfSoFarIncludedCounters = streak.getAmountOfCountsPerCounter().get(itemUser.getId());
        boolean enoughCounters = countsContributedOfSoFarIncludedCounters / totalCounts >= PERC_COUNTERS_NEEDED;
        int index = 0;
        while (!enoughCounters) {
            if (index >= sortedIds.length) {
                throw new IllegalStateException("All counters should add up to total score but dont!");
            }
            String nextCounterId = sortedIds[index];
            if (Objects.equals(nextCounterId, itemUser.getId())) {
                index++;
                continue;
            }
            idsOfCountersNeededToContribute.add(nextCounterId);
            countsContributedOfSoFarIncludedCounters += streak.getAmountOfCountsPerCounter().get(nextCounterId);
            enoughCounters = countsContributedOfSoFarIncludedCounters / totalCounts >= PERC_COUNTERS_NEEDED;
            index++;
        }
        System.out.println("Ids of counters needed to contribute: " + idsOfCountersNeededToContribute);
        if (!idsOfCountersNeededToContribute.isEmpty()) {
            String countersPings = "";
            for (int i = 0; i < idsOfCountersNeededToContribute.size(); i++) {
                String counterId = idsOfCountersNeededToContribute.get(i);
                countersPings += "<@" + counterId + ">";
                if (i == idsOfCountersNeededToContribute.size() - 2) {
                    countersPings += " and ";
                } else if (i < idsOfCountersNeededToContribute.size() - 2) {
                    countersPings += ", ";
                }
            }
            countersPings += idsOfCountersNeededToContribute.size() > 1 ? " need" : " needs";
            currentWhiteStreakEnderDialogue = new Dialogue();
            epochSecondAtLastWhiteStreakEnderRequested = now();
            ParallelDialogElementsBuilder builder = currentWhiteStreakEnderDialogue.
                    addNpcLine("To end this streak, players representing at least " + Math.round(PERC_COUNTERS_NEEDED * 100) +
                            "% of the counts in the current streak need to agree. " +
                            "For that, " + countersPings + " to react to this message with " + CountingEmojis.THUMBS_UP.asUnicodeEmoji().get().getRaw() + "!", 0)
                    .addEmojiReaction(CountingEmojis.THUMBS_UP)
                    .addEmojiReaction(CountingEmojis.THUMBS_DOWN)
                    .initializeParallelDialogElements();    // Todo
            for (String contributingId : idsOfCountersNeededToContribute) {
                builder.addWaitForEmojiReaction(CountingEmojis.THUMBS_UP, false, m -> {
                    // On react up
                }, Optional.of(contributingId), ParallelDialogElementsBuilder.ParallelDialogElementType.NECESSARY);
                builder.addWaitForEmojiReaction(CountingEmojis.THUMBS_DOWN, true, m -> {
                    // On react down
                    Counter decliner = CountingBot.getCounter(streak.getGuildId(), contributingId);
                    CountingBot.write(message, "The Streak:white_circle:Ender was cancelled by " + decliner.getName() + ".");
                    currentWhiteStreakEnderDialogue = null;
                }, Optional.of(contributingId), ParallelDialogElementsBuilder.ParallelDialogElementType.SUFFICIENT);
            }
            builder.finishParallelDialogElementsAndAdd()
                    .addRunnable((msg) -> {
                        // All agreed
                        if (itemUser.getInventory().getAmountOfItem(Consumables.WHITE_STREAK_ENDER) <= 0) {
                            CountingBot.write(message, "Oops, " + itemUser.getName() + "' Streak:white_circle:Ender has already been used somewhere else in the mean time!");
                            currentWhiteStreakEnderDialogue = null;
                            return;
                        }
                        itemUser.getInventory().removeItem(Consumables.WHITE_STREAK_ENDER);
                        CountingBot.write(message, itemUser.getName() + " used a " + Consumables.WHITE_STREAK_ENDER.getName() + "...");
                        streak.streakPayout(message, "", 0, null, 0, null);
                        currentWhiteStreakEnderDialogue = null;
                        CountingBot.getInstance().disposeStreak(streak.getKey());
                    })
                    .play(message);
        } else {
            CountingBot.write(message, "As you have contributed more than " + Math.round(PERC_COUNTERS_NEEDED * 100)
                    + "% of the counts in the current streak, you need not ask anyone for permission to end this streak!");
            if (itemUser.getInventory().getAmountOfItem(Consumables.WHITE_STREAK_ENDER) <= 0) {
                CountingBot.write(message, "Oops, you no longer own a Streak:white_circle:Ender.");
                return;
            }
            CountingBot.write(message, "You used a " + Consumables.WHITE_STREAK_ENDER.getName() + "...");
            itemUser.getInventory().removeItem(Consumables.WHITE_STREAK_ENDER);
            streak.streakPayout(message, null, 0, null, 0, null); // todo testen ob man da irgendwas schreiben muss so "a streak ender was used heureker jeder kriegt geld" oder soos
            CountingBot.getInstance().disposeStreak(streak.getKey());
        }
    }

    public void dispose() {
        if (currentWhiteStreakEnderDialogue != null) {
            System.out.println("Stopping streak ender dialogue...");    // todo does not work
            currentWhiteStreakEnderDialogue.stop();
        }
    }
}
