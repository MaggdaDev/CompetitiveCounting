package competitivecounting.items.equippables;

import competitivecounting.Counter;
import competitivecounting.CountingBot;
import competitivecounting.CountingContext;
import competitivecounting.Util;
import competitivecounting.dialogue.Dialogue;
import discord4j.core.object.entity.Message;

import java.util.Objects;

public class DowsingRod extends Equippable {
    public final static String NAME = "Wünschel:magic_wand:Rute";
    public final static double WISH_MULTIPLIER = 10.;
    public final static String COLLECTION_DESCRIPTION_EMPTY = "_Use_ to wish for a trophy that gains x" + (int)WISH_MULTIPLIER + " spawn-probability!\n-# Successful wishes: {0}";
    public final static String COLLECTION_DESCRIPTION_WISHED = "Currently wishing for the #{0} trophy (x" + (int)WISH_MULTIPLIER + " spawn rate). _Use_ to change wish!\n-# Successful wishes: {1}";
    private final static String DESCRIPTION = "When equipped, can be used to wish for a trophy!";
    private int successfulWishes = 0;
    private int currentWish = -1;
    private transient Dialogue currentDialogue = null;

    private final static String USE_MESSAGE =  "Please submit your trophy wish by writing `wish <trophy number>`.";
    private final static String YOU_CAN_ONLY_WISH_FOR_POSITIVE_INTEGERS = "You can only wish for trophies corresponding to positive integers.";

    public DowsingRod(Counter owner) {
        super(null, NAME, DESCRIPTION, owner);
    }

    @Override
    public boolean doCollectionUse(Message message, CountingContext context) {
        if (currentDialogue != null) {
            CountingBot.write(message, USE_MESSAGE);
            return true;
        }
        currentDialogue = new Dialogue()
                .addNpcLine(USE_MESSAGE, 0)
                .addWaitForUserAnswer(answer -> {
                    String answerAuthorId = answer.getAuthor().get().getId().asString();
                    if (!Objects.equals(owner.getId(), answerAuthorId)) {
                        return false;
                    }
                    String content = answer.getContent().trim().toLowerCase();
                    if (!content.startsWith("wish ")) {
                        return false;
                    }
                    String trophyNumberStr = content.substring(5).trim();
                    int trophyNumber;
                    try {
                        trophyNumber = Integer.parseInt(trophyNumberStr);
                        if (trophyNumber <= 0) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException e) {
                        CountingBot.write(message, YOU_CAN_ONLY_WISH_FOR_POSITIVE_INTEGERS);
                        return false;
                    }
                    currentWish = trophyNumber;
                    CountingBot.write(message, "The #" + currentWish + " trophy will now spawn with a x" + (int)WISH_MULTIPLIER + " probability on your counts.");
                    return true;
                })
                .addRunnable(m -> currentDialogue = null);
        currentDialogue.play(message);
        return true;
    }

    public double modifyTrophyRate(double trophyChance, int number) {
        if (number == currentWish) {
            return Util.multiplyProbabilityThreshold(trophyChance, WISH_MULTIPLIER);
        }
        return trophyChance;
    }

    @Override
    public String getCollectionDescription() {
        return currentWish == -1 ? COLLECTION_DESCRIPTION_EMPTY.replace("{0}", String.valueOf(successfulWishes)) :
                COLLECTION_DESCRIPTION_WISHED.replace("{0}", String.valueOf(currentWish)).replace("{1}", String.valueOf(successfulWishes));
    }

    @Override
    public Equippable createObject(Counter owner) {
        return new DowsingRod(owner);
    }


    public void ownSpawnedTrophyClaimed(int trophy) {
        if (trophy == currentWish) {
            successfulWishes++;
        }
    }

    public void trophySpawned(Message message, int number) {
        if (number == currentWish) {
            CountingBot.write(message, "Your trophy wish has spawned, " + owner.getPing() + "!");
        }
    }
}
