/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competitivecounting.rules;

import competitivecounting.CountingEmojis;
import discord4j.core.object.emoji.Emoji;
import discord4j.core.object.entity.Message;

import java.time.Instant;

/**
 *
 * @author DavidPrivat
 */
public class SlowModeRule implements Rule {

    private long epochAtStart;
    private int secondsDiff;
    private boolean lost = false, shouldStop = false, newlyAdded = true;
    private String ownerId;

    public SlowModeRule(int duration, String ownerId) {
        secondsDiff = duration;
        this.ownerId = ownerId;
    }

    public void applyTimerToMessage(Message message) {
        Thread thread = new Thread() {

            @Override
            public void run() {
                epochAtStart = Instant.now().getEpochSecond();
                int timer = secondsDiff;
                Emoji one, two, three, clock;
                one = Emoji.unicode("\u0031\u20E3");
                two = Emoji.unicode("\u0032\u20E3");
                three = Emoji.unicode("\u0033\u20E3");
                clock = Emoji.unicode("\u23F3");
                message.addReaction(clock).subscribe();
                while (timer > 0) {
                    if (shouldStop) {
                        break;
                    }
                    switch (timer) {
                        case 2:
                            message.addReaction(one).subscribe();
                            break;
                        case 4:
                            message.addReaction(two).subscribe();
                            newlyAdded = false;
                            break;
                        case 6:
                            message.addReaction(three).subscribe();
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
                    message.removeSelfReaction(clock).subscribe();
                    message.addReaction(CountingEmojis.KEKMARK).subscribe();
                    shouldStop = false;
                    return;
                }
                message.removeSelfReaction(clock).subscribe();
                message.addReaction(CountingEmojis.KEKMARK).subscribe();

            }
        };
        thread.start();
    }

    public boolean isNewlyAdded() {
        return newlyAdded;
    }
    
    public void stop() {
        shouldStop = true;
    }

    public int getDuration() {
        return secondsDiff;
    }
    
    public double getCurrentBonusFactor() {
        return Math.pow(Math.max(1.0,getDuration()), 1.0/3.0);
    }

    public boolean accepted(Message message) {
        boolean accepts = Instant.now().getEpochSecond() - epochAtStart >= secondsDiff;
        if (!accepts) {
            lost = true;
        }
        return accepts;
    }

    public boolean hasLost() {
        return lost;
    }

    @Override
    public String getOwnerId() {
        return ownerId;
    }

    @Override
    public String toString() {
        return "You must wait " + secondsDiff + "s between two counts. (=> "+Math.round(getCurrentBonusFactor()*10.0d)/10.0 + "x bonus factor after first count)";
    }
}
