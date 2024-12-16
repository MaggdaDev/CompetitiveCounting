package CompetitiveCounting;

import discord4j.core.object.entity.Message;

public class TrophyHandler {
    private final EmojiReactHandler reactHandler;

    public TrophyHandler(EmojiReactHandler reactHandler) {
        this.reactHandler = reactHandler;

    }

    public void considerSpawningTrophy(int number, Message message, Counter user) {
        if (randBool(trophyChanceFromNumber(number))) {
            message.addReaction(Emojis.TROPHY).subscribe();
            reactHandler.addOnTrophyReact((messageReactedTo, reactingUser) -> {
                if (messageReactedTo.getId().equals(message.getId())) {
                    String reactingUserId = reactingUser.getId().asString();
                    if(!CountingBot.getInstance().isCounter(reactingUserId)) {
                        CountingBot.write(message, "You cannot claim trophies without having counted at least once, " + reactingUser.getUsername() + "!");
                        return false;
                    }
                    Counter reactingCounter = CountingBot.getInstance().getCounter(reactingUserId);
                    if (reactingCounter.hasTrophy(number)) {
                        reactingCounter.addTrophyShard();
                        if (reactingCounter.getTrophyShards() <= 1) {
                            CountingBot.write(message, "You have already found this trophy and hence get nothing, " +  reactingCounter.getName() +
                                    "!\n\nJust joking. Congratulations for finding your first trophy shard! Trophy shards are extremely rare and can be spent in the ~shunlock shop.");
                        } else {
                            CountingBot.write(message, "You have already found this trophy and hence get an extraordinarily valuable trophy shard instead, " + reactingCounter.getName() + ". You now have " + reactingCounter.getTrophyShards() + " trophy shards!");
                        }
                    } else {
                        reactingCounter.addTrophy(number);
                        CountingBot.write(message, "Congratulations " + reactingCounter.getName() + "! You have earned the " + number + "-trophy! You now have " + reactingCounter.getTrophyAmount() + " trophies!");
                    }
                    return true;
                } else {
                    return false;
                }
            });

        }
    }

    private double trophyChanceFromNumber(double number) {
        return Math.atan( (Math.log(0.02*(number+100) + Math.E)-1.0) / 10.0) / (20.0 * Math.PI);
    }

    private boolean randBool(double prop) {
        return Math.random() < prop;
    }
}
