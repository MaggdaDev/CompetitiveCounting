/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting.rules;

import CompetitiveCounting.Counter;
import CompetitiveCounting.CountingBot;
import CompetitiveCounting.CountingStreak;
import CompetitiveCounting.CountingEmojis;
import discord4j.core.object.entity.Message;

/**
 *
 * @author DavidPrivat
 */
public class TimeLimitRule implements Rule {
    public final static double BONUS_FACTOR = 2.0d;
    private String ownerId;
    private CountingStreak streak;
    private int time = 10;
    private AnimationThread thread;
    private boolean hasLost = false, shouldCancel = false;

    public TimeLimitRule(String ownerId, CountingStreak streak) {
        this.ownerId = ownerId;
        this.streak = streak;

    }

    public void applyTimerToMessage(Message message, Counter loser) {
        if (thread != null) {
            thread.shouldStop = true;
        }
        thread = new AnimationThread(message, loser);
        thread.start();
    }

    public void cancel() {
        shouldCancel = true;
    }

    public boolean hasLost() {
        return hasLost;
    }

    @Override
    public String toString() {
        return "You must send each next number before " + time + " seconds have passed (=> " + Math.round(BONUS_FACTOR) + "x bonus factor)";
    }

    private class AnimationThread extends Thread {

        public boolean shouldStop = false;
        private Message message;
        private Counter loser;

        public AnimationThread(Message message, Counter loser) {
            this.message = message;
            this.loser = loser;
        }

        @Override
        public void run() {
            int timer = time + 1;
            message.addReaction(CountingEmojis.KEKMARK_BOLT).subscribe();
            while (timer >= 0) {
                if (shouldStop) {
                    return;
                } else if (shouldCancel) {
                    timer = 0;
                }
                switch (timer) {
                    case 2:
                        message.addReaction(CountingEmojis.ONE).subscribe();
                        break;
                    case 4:
                        message.addReaction(CountingEmojis.TWO).subscribe();
                        break;
                    case 6:
                        message.addReaction(CountingEmojis.THREE).subscribe();
                        break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                timer -= 1;
            }
            if (shouldStop) {
                return;
            } else if (shouldCancel) {
                shouldCancel = false;
                return;
            }
            hasLost = true;
            streak.timeLimitLost(ownerId, message, loser);
            CountingBot.getInstance().removeStreak(streak.getKey());

        }
    }

    @Override
    public String getOwnerId() {
        return ownerId;
    }

}
