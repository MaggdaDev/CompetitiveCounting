package competitivecounting.items.equippables;

import competitivecounting.*;
import competitivecounting.dialogue.Dialogue;
import competitivecounting.items.Consumables;
import competitivecounting.vaults.PrimeVault;
import discord4j.core.object.entity.Message;

public class CoinMiner extends Equippable {
    public static final String NAME = "Coin:pick:Miner";
    public static final String DESCRIPTION = "Equip to gain a small chance to mine a " + Consumables.PRIME_COIN.getName() + " while counting primes.";
    private int coinsMined = 0;
    private final static double spawnChance = 0.0015;
    public static final String EQUIPPED_DESCRIPTION = "On each prime number that you count, a " + Consumables.PRIME_COIN.getName() +
            " may spawn with a small probability. Anyone with a " + NAME +
            " equipped may claim it.\n-# " + Consumables.PRIME_COIN.getName() + "s mined: {0}";
    public static final long COIN_CLAIM_TIMEOUT_SECONDS = 20;

    public CoinMiner(Counter owner) {
        super(null, NAME, DESCRIPTION, owner);
    }

    @Override
    public String getCollectionDescription() {
        return EQUIPPED_DESCRIPTION.replace("{0}", "" + coinsMined);
    }

    @Override
    public Equippable createObject(Counter owner) {
        return new CoinMiner(owner);
    }

    @Override
    public void performPassiveAfterCounterReceivesMoney(Message message, CountingContext context, int scoreAdd) {
        if (context.getCounter() != owner) {
            return;
        }
        if (PrimeVault.isPrime(context.getCurrentNumber())) {
            double rand = Math.random();
            if (rand <= spawnChance) {
                // Spawn
                new Dialogue()
                        .addEmojiReaction(CountingEmojis.COIN)
                        .addWaitForEmojiReaction(CountingEmojis.COIN, (msg, emojiReactor) -> {
                            if (emojiReactor.getCollection().containsEquippable(Equippables.COIN_MINER)) {
                                emojiReactor.getInventory().addItem(Consumables.PRIME_COIN);
                                ((CoinMiner)emojiReactor.getCollection().getEquippable(Equippables.COIN_MINER)).incrementCoinsMinedStat();
                                CountingBot.write(msg, "Congratulations, you mined a " + Consumables.PRIME_COIN.getName() + ", " + emojiReactor.getName() + "!");
                                return true;
                            }
                            CountingBot.write(msg, "You need to equip a " + CoinMiner.NAME + " to mine " + Consumables.PRIME_COIN.getName() + "s, " + emojiReactor.getName() + "!");
                            return false;
                        }, COIN_CLAIM_TIMEOUT_SECONDS, m -> {
                            message.removeReactions(CountingEmojis.COIN).subscribe();
                            new Dialogue()
                                    .addSleep(3)
                                    .addNpcLine("You have forsaken the wealth of a real " + Consumables.PRIME_COIN.getName() + "...", 3000)
                                    .addNpcLine("Your temperance is beyond compare.", 3000)
                                    .addRunnable(currMsg -> context.getStreak().getTrophyHandler().spawnTrophy(message, -4))
                                    .play(message);
                            return true;
                        })
                        .play(message);
            }
        }
    }

    private void incrementCoinsMinedStat() {
        coinsMined++;
    }
}
