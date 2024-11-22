/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting.Rules;

import CompetitiveCounting.Emojis;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import java.time.Duration;
import java.time.Instant;

/**
 *
 * @author DavidPrivat
 */
public class SlowModeRule implements Rule {

    private Instant lastTime;
    private int secondsDiff;
    private boolean accepts = true, lost = false, shouldStop = false, newlyAdded = true;
    private String ownerId;

    public SlowModeRule(int duration, String ownerId) {
        secondsDiff = duration;
        this.ownerId = ownerId;
    }

    public void applyTimerToMessage(Message message) {
        Thread thread = new Thread() {

            @Override
            public void run() {
                accepts = false;
                int timer = secondsDiff;
                ReactionEmoji one, two, three, clock;
                one = ReactionEmoji.unicode("\u0031\u20E3");
                two = ReactionEmoji.unicode("\u0032\u20E3");
                three = ReactionEmoji.unicode("\u0033\u20E3");
                clock = ReactionEmoji.unicode("\u23F3");
                message.addReaction(clock).subscribe();
                while (timer > 0) {
                    if (shouldStop) {
                        break;
                    }
                    switch (timer) {
                        case 1:
                            message.removeSelfReaction(two).subscribe();
                            message.addReaction(one).subscribe();
                            break;
                        case 2:
                            message.removeSelfReaction(three).subscribe();
                            message.addReaction(two).subscribe();
                            newlyAdded = false;
                            break;
                        case 3:
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
                    message.removeSelfReaction(three).subscribe();
                    message.removeSelfReaction(two).subscribe();
                    message.removeSelfReaction(one).subscribe();
                    message.removeSelfReaction(clock).subscribe();
                    message.addReaction(Emojis.KEKMARK).subscribe();
                    accepts = true;
                    shouldStop = false;
                    return;
                }
                message.removeSelfReaction(one).subscribe();
                message.removeSelfReaction(clock).subscribe();
                message.addReaction(Emojis.KEKMARK).subscribe();
                accepts = true;

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
        if (accepts == false) {
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
