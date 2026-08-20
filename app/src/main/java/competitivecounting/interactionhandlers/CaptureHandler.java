package competitivecounting.interactionhandlers;

import competitivecounting.CountingBot;
import competitivecounting.CountingEmojis;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class CaptureHandler {
    private final static long MIN_TIME_BETWEEN_CAPTURES = 630000;
    private final double CAPTURE_CHANCE = 0.05;
    private final EmojiReactHandler emojiReactHandler;

    private final List<Capture> captures;
    public CaptureHandler(EmojiReactHandler emojiReactHandler) {
        this.emojiReactHandler = emojiReactHandler;
        this.captures = CountingBot.getInstance().getStorage().loadCaptures();
    }

    public boolean raisedCapture(Message message, int number, String userId, HashMap<String,Long> lastCaptureTimes, Runnable onCaptureFailed, Runnable onCaptureSucceeded, TrophyHandler trophyHandler) {
        if (!lastCaptureTimes.containsKey(userId)) {
            lastCaptureTimes.put(userId, System.currentTimeMillis());
            return false;
        }
        long lastCaptureTime = lastCaptureTimes.get(userId);
        if (System.currentTimeMillis() - lastCaptureTime < MIN_TIME_BETWEEN_CAPTURES) {
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
            Mono<Void>[] reactions = new Mono[10];
            for(int i = 0; i < 10; i ++) {
                reactions[i] = sentCaptureMessage.addReaction(CountingEmojis.ALL_NUMBER_EMOJIS[i]);
            }
            AtomicReference<EmojiReactHandler.TriFunction<Message, User, ReactionEmoji.Unicode, Boolean>> secretHandlerRef = new AtomicReference<>();
            AtomicReference<EmojiReactHandler.TriFunction<Message, User, Integer, Boolean>> numberReactRef = new AtomicReference<>();
            secretHandlerRef.set(
                    (reactedMessage, user, reactedEmoji) -> {
                if(reactedMessage.getId().equals(sentCaptureMessage.getId()) && user.getId().asString().equals(userId)) {
                    if ("\uD83C\uDDFA".equals(reactedEmoji.getRaw())) { // u emoji
                        if (emojiReactHandler.hasOnNumberReact(numberReactRef.get())) {
                            emojiReactHandler.removeOnNumberReact(numberReactRef.get());
                        }
                        trophyHandler.spawnMooseTrophy(sentCaptureMessage, capture.question, reactedEmoji);
                        CountingBot.write(reactedMessage, "Alright, for now moose don't soose to be a maloosevolent scrooster, <@!" + userId + ">. You can continue moosing now!");
                        onCaptureSucceeded.run();
                        return true;
                    }
                }
                return false;
            });
            numberReactRef.set(
                    (reactedMessage, user, reactedNumber) -> {
                if(reactedMessage.getId().equals(sentCaptureMessage.getId()) && user.getId().asString().equals(userId)) {
                    if (secret) {
                        if (emojiReactHandler.hasOnAnyReact(secretHandlerRef.get())) {
                            emojiReactHandler.removeOnAnyReact(secretHandlerRef.get());
                        }
                    }
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

            if(secret) {
                emojiReactHandler.addOnAnyReact(secretHandlerRef.get());
            }
            Mono.when(reactions).doOnSuccess((v) -> {
                emojiReactHandler.addOnNumberReact(numberReactRef.get());

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
