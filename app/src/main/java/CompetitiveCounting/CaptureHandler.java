package CompetitiveCounting;

import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CaptureHandler {
    private final static long MIN_TIME_BETWEEN_CAPTURES = 420000;
    private final double CAPTURE_CHANCE = 0.05;
    private final EmojiReactHandler emojiReactHandler;

    private final List<Capture> captures;
    public CaptureHandler(EmojiReactHandler emojiReactHandler) {
        this.emojiReactHandler = emojiReactHandler;
        this.captures = CountingBot.getInstance().getStorage().loadCaptures();
    }

    public boolean raisedCapture(Message message, int number, String userId, HashMap<String,Long> lastCaptureTimes, Runnable onCaptureFailed, Runnable onCaptureSucceeded, TrophyHandler trophyHandler) {
        long lastCaptureTime = lastCaptureTimes.getOrDefault(userId, System.currentTimeMillis());
        if (System.currentTimeMillis() - lastCaptureTime < MIN_TIME_BETWEEN_CAPTURES) {
            return false;
        }
        if (System.currentTimeMillis() - lastCaptureTime > 5 * MIN_TIME_BETWEEN_CAPTURES) {
            lastCaptureTimes.put(userId, System.currentTimeMillis());
            return false;
        }
        if (Math.random() < CAPTURE_CHANCE) {
            doCapture(message, number, userId, onCaptureFailed, onCaptureSucceeded, trophyHandler);
            lastCaptureTimes.put(userId, System.currentTimeMillis());
            return true;
        }
        return false;
    }

    private void doCapture(Message message, int number, String userId, Runnable onCaptureFailed, Runnable onCaptureSucceeded, TrophyHandler trophyHandler) {
        Capture capture;
        boolean secret = Math.random() < 1.0 / (double)captures.size();
        if (secret) {
            capture = Capture.SECRET_CAPTURE;
        } else {
            capture = captures.get((int)(Math.random() * (double)captures.size()));
        }
        String msg = "Oi, this is the anti-scripting police! Pull over to the side and take your time to answer this challenging captcha to prove you are not a vicious scripter, <@!" + userId + ">!\n\n" + capture.getQuestion();
        CountingBot.write(message, msg, (sentCaptureMessage) -> {
            if(secret) {
                emojiReactHandler.addOnAnyReact((reactedMessage, user, reactedEmoji) -> {
                    if(Arrays.asList(CountingEmojis.ALL_NUMBER_EMOJIS).contains(reactedEmoji)) {
                        CountingBot.write(reactedMessage, "Alright, for now you don't seem to be a malevolent scripter, <@!" + userId + ">. You can continue counting now!");
                        onCaptureSucceeded.run();
                        return true;
                    }
                    if("\uD83C\uDDFA".equals(reactedEmoji.getRaw())) { // u emoji
                        trophyHandler.spawnMooseTrophy(sentCaptureMessage, capture.question, reactedEmoji);
                        CountingBot.write(reactedMessage, "Alright, for now moose don't soose to be a maloosevolent scrooster, <@!" + userId + ">. You can continue moosing now!");
                        onCaptureSucceeded.run();
                        return true;
                    }
                    return false;
                });
            }
            Mono<Void>[] reactions = new Mono[10];
            for(int i = 0; i < 10; i ++) {
                reactions[i] = sentCaptureMessage.addReaction(CountingEmojis.ALL_NUMBER_EMOJIS[i]);
            }
            Mono.when(reactions).doOnSuccess((v) -> {
                emojiReactHandler.addOnNumberReact((reactedMessage, user, reactedNumber) -> {
                    if(reactedMessage.getId().equals(sentCaptureMessage.getId()) && user.getId().asString().equals(userId)) {
                        if(reactedNumber == capture.getAnswer()) {
                            CountingBot.write(reactedMessage, "Alright, for now you don't seem to be a malevolent scripter, <@!" + userId + ">. You can continue counting now!");
                            onCaptureSucceeded.run();
                        } else {
                            CountingBot.write(reactedMessage, "You failed the captcha, <@!" + userId + ">! Nefarious scripter!");
                            onCaptureFailed.run();
                        }
                        return true;
                    }
                    return false;
                });

            }).subscribe();
        });

    }

    public static class Capture {
        private String question;
        private int answer;

        public Capture(String question, int answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() {
            return question;
        }

        public int getAnswer() {
            return answer;
        }

        public static Capture SECRET_CAPTURE = new Capture("Imagine a mouse observing a pacman collecting bottles of beer. The mouse reacts to this message with twice the amount of beer bottles that the pacman drank. What emoji would it react with, if the pacman drank 3 bottles?", 6);
    }
}
