package CompetitiveCounting;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;

import java.util.function.Consumer;

public class TrophyHandler {
    private final static double BASE_TROPHY_CHANCE = 0.007d;
    private final EmojiReactHandler reactHandler;

    public TrophyHandler(EmojiReactHandler reactHandler) {
        this.reactHandler = reactHandler;

    }

    public void considerSpawningTrophy(int number, Message message, Counter user) {
        if (randBool(BASE_TROPHY_CHANCE)) {
            message.addReaction(Emojis.TROPHY).subscribe();
            reactHandler.addOnTrophyReact((messageReactedTo, reactingUser) -> {
                if (messageReactedTo.getId().equals(message.getId())) {
                    if (user.hasTrophy(number)) {
                        user.addTrophyShard();
                        if (user.getTrophyShards() <= 1) {
                            CountingBot.write(message, "You have already found this trophy and hence get nothing, " +  user.getName() +
                                    "!\n\nJust joking. Congratulations for finding your first trophy shard! Trophy shards are extremely rare and can be spent in the ~shunlock shop.");
                        } else {
                            CountingBot.write(message, "You have already found this trophy and hence get an extraordinarily valuable trophy shard instead, " + user.getName() + ". You now have " + user.getTrophyShards() + " trophy shards!");
                        }
                    } else {
                        user.addTrophy(number);
                        CountingBot.write(message, "Congratulations " + user.getName() + "! You have earned the " + number + "-trophy! You now have " + user.getTrophyAmount() + " trophies!");
                    }
                    return true;
                } else {
                    return false;
                }
            });

        }
    }

    private boolean randBool(double prop) {
        return Math.random() < prop;
    }
}
