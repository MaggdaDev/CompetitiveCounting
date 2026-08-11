package CompetitiveCounting.interactionhandlers;

import CompetitiveCounting.Counter;
import CompetitiveCounting.CountingBot;
import CompetitiveCounting.CountingEmojis;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.MessageEditSpec;
import discord4j.discordjson.possible.Possible;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class TrophyHandler {
    private final EmojiReactHandler reactHandler;

    private final static double ILLEGAL_CHARACTER_TROPHY_CHANCE = 0.05;
    int lastCountWhileIllegalCharacter = -1;

    public TrophyHandler(EmojiReactHandler reactHandler) {
        this.reactHandler = reactHandler;

    }

    public void considerSpawningIllegalCharacterTrophy(Message message, int count) {
        if (count == lastCountWhileIllegalCharacter) {
            return; // Only allow 1 illegal character message spawn chance per count
        }
        if (randBool(ILLEGAL_CHARACTER_TROPHY_CHANCE)) {
            spawnTrophy(message, -200);
        }
        lastCountWhileIllegalCharacter = count;

    }

    public void spawnSecondHandbagTrophy(Message message) {
        spawnTrophy(message, -1050505);
    }

    public void considerSpawningTrophy(int number, Message message, Counter user) {
        if (randBool(trophyChanceFromNumber(number))) {
            spawnTrophy(message, number);
        }
    }

    private void spawnTrophy(Message message, int number) {
        message.addReaction(CountingEmojis.TROPHY).subscribe();
        reactHandler.addOnTrophyReact((messageReactedTo, reactingUser) -> {
            if (messageReactedTo.getId().equals(message.getId())) {
                String reactingUserId = reactingUser.getId().asString();
                if (!CountingBot.getInstance().isCounter(messageReactedTo.getGuildId().get().asString(), reactingUserId)) {
                    CountingBot.write(messageReactedTo, "You cannot claim trophies without having counted at least once, " + reactingUser.getUsername() + "!");
                    return false;
                }
                String guildId = messageReactedTo.getGuildId().get().asString();
                Counter reactingCounter = CountingBot.getInstance().getCounter(guildId, reactingUserId);
                if (reactingCounter.hasTrophy(number)) {
                    if (number > 0) {
                        reactingCounter.addTrophyShard();
                        if (reactingCounter.getTrophyShards() <= 1) {
                            CountingBot.write(messageReactedTo, "You have already found this trophy and hence get nothing, " + reactingCounter.getName() +
                                    "!\n\nJust joking. Congratulations for finding your first trophy shard! Trophy shards are extremely rare and can be spent in the ~shunlock shop.");
                        } else {
                            CountingBot.write(messageReactedTo, "You have already found this trophy and hence get an extraordinarily valuable trophy shard instead, " + reactingCounter.getName() + ". You now have " + reactingCounter.getTrophyShards() + " trophy shards!");
                        }
                    } else {
                        CountingBot.write(messageReactedTo, "You have already found this special trophy and hence get nothing. But at least you stopped other counters from being able to claim this trophy. You truly are a competitive counter, " + reactingCounter.getName() + "!");
                    }
                } else {
                    reactingCounter.addTrophy(number);
                    int trophyAmount = reactingCounter.getTrophyAmount();
                    String s = "Congratulations " + reactingCounter.getName() + "!" +
                            " You have earned the " + getTrophyDescription(number) + "! You now have " + trophyAmount;
                    s += trophyAmount > 1 ? " trophies!" : " trophy.";
                    CountingBot.write(messageReactedTo, s);
                }
                return true;
            } else {
                return false;
            }
        });
    }

    public void spawnMooseTrophy(Message message, String originalContent, ReactionEmoji uEmoji) {
        String newContent = originalContent.replaceAll("(?i)mouse", "moose");
        message.removeReactions(uEmoji)
                .then(message.edit(MessageEditSpec.create().withContent(Possible.of(Optional.of(newContent)))))
                .flatMap((Message msg) -> {
                    CountingBot.write(msg, "https://c.tenor.com/ZsBt_qqcXtcAAAAd/tenor.gif");
                    spawnTrophy(msg, -2019);
                    return Mono.empty();
                }).subscribe();

    }

    public static String getTrophyDescription(int trophy) {
        if (trophy > 0) {
            return "trophy #" + trophy;
        }
        String startText = "trophy #" + trophy + ": ";
        switch (trophy) {
            case -753:
                return startText + "_Relic of Prestige from the Fallen Empire_";
            case -2147483648:
                return startText + "_To Infinity!_";
            case -200:
                return startText + "_Dirty methods_";
            case -2019:
                return startText + "_Moose_";
            case -1050505:
                return startText + "_Gucci Fendi & Prada Collector_";
            default:
                return startText;
        }
    }


    private double trophyChanceFromNumber(double number) {
        return 1.0 / (100.0 + 300.0 * Math.exp(-0.05 * number) + 100.0 * Math.exp(-0.01 * number));
    }

    private boolean randBool(double prop) {
        return Math.random() < prop;
    }


}
