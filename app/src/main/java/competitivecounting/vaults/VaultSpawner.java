package competitivecounting.vaults;

import competitivecounting.*;
import competitivecounting.items.equippables.Equippables;
import competitivecounting.items.equippables.VaultLocator;
import competitivecounting.vaults.publicgoodsvault.VaultOfPublicGoods;
import competitivecounting.vaults.trophyvault.TrophyVault;
import discord4j.core.object.entity.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VaultSpawner {
    private final static Vault[] ALL_VAULTS = {
            new VaultOfLongStrides(),
            new PrimeVault(),
            new TrophyVault(),
            new CommunityVault(),
            new VaultOfPublicGoods()
    };
    private final Vault[] vaults;
    private Vault activeVault = null;
    private CountingContext previousContext;

    public VaultSpawner() {
        vaults = new Vault[ALL_VAULTS.length];
        for (int i = 0; i < ALL_VAULTS.length; i++) {
            try {
                vaults[i] = ALL_VAULTS[i].getClass().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    public static void vaultInfo(Message message, Optional<CountingStreak> streak, Counter counter) {
        String s = streak.isEmpty() ? getStaticVaultInfo(counter, streak) : streak.get().getVaultSpawner().getStreakVaultInfo(counter, streak.get());
        CountingBot.write(message, s);
    }

    private String getStreakVaultInfo(Counter counter, CountingStreak streak) {
        StringBuilder ret = new StringBuilder(getStaticVaultInfo(counter, Optional.of(streak)));
        if (previousContext == null) {
            return ret.toString();
        }

        List<Vault> eligibleVaults = new ArrayList<>();
        for (Vault vault : vaults) {
            if (vault.canSpawn(previousContext)) {
                eligibleVaults.add(vault);
            }
        }
        ret.append("\n");
        if (eligibleVaults.isEmpty()) {
            ret.append("The last count did not meet the requirements of any vault!");
        } else if (eligibleVaults.size() == 1) {
            ret.append("The last count only met the requirements for the ").append(eligibleVaults.get(0).getVaultName()).append(".");
        } else {
            ret.append("The last count met the requirements of the following vaults: \n");
            for (int i = 0; i < eligibleVaults.size(); i++) {
                ret.append(eligibleVaults.get(i).getVaultName());
                if (i == eligibleVaults.size() - 2) {
                    ret.append(" & ");
                } else if (i <= eligibleVaults.size() - 3) {
                    ret.append(", ");
                }
            }
        }
        ret.append((hasActiveVault()) ? "\n\nThere is currently a **" + activeVault.getVaultName() + "** active!" : "");
        return ret.toString();

    }

    private static String getStaticVaultInfo(Counter counter, Optional<CountingStreak> streak) {
        StringBuilder s = new StringBuilder("If you have equipped a " + VaultLocator.NAME + ", you are are capable of finding rare vaults! If you meet their requirements, they will spawn at their respective spawn rate:\n");
        for (Vault vault : ALL_VAULTS) {
            int odds = (int) Math.round(1. / vault.getSpawnChance());
            double spawnChance = vault.getSpawnChance();
            spawnChance = counter.getCountingBoosterManager().modifyVaultRate(spawnChance);
            if (streak.isPresent()) {
                CountingContext lastContext = streak.get().getLastCountingContext();
                if (lastContext != null) {
                    spawnChance = counter.getCollection().modifyVaultRateFromEquippables(spawnChance, lastContext);
                }
            }
            int oddsWithBoni = (int) Math.round(1. / spawnChance);
            s.append("- ").append(vault.getVaultName()).append(": ").append(vault.getSpawnConditionsDescription()).append(spawnChance <= 0 ? "" : " (1 in " + Util.valueAndValueWithBoniToString(odds, oddsWithBoni) + ")\n");
        }
        return s.toString();
    }

    public Optional<Vault> maybeSpawnVault(Message message, CountingContext context) {
        previousContext = context;
        if (!context.getCounter().getCollection().containsEquippable(Equippables.VAULT_LOCATOR)) {
            return Optional.empty();
        }
        if (activeVault != null) {
            return Optional.empty();
        }
        for (Vault vault : vaults) {
            if (vault.maybeSpawn(context)) {
                if (vault instanceof CommunityVault) {
                    if (context.getStreak().getCounterIdsOfActiveSponsoredMonocles().contains(context.getCounter().getId())) {
                        if (getVaultOfPublicGoods().canSpawn(context)) {
                            vault = getVaultOfPublicGoods();
                        } else {
                            CountingBot.write(message, context.getCounter().getName() + ", even though you have an active CrocBank Inc. sponsorship, the "
                                    + VaultOfPublicGoods.NAME + " cannot spawn, as not all participating counters can afford the buy-in of " + VaultOfPublicGoods.BUY_IN + " money.");
                        }
                    }
                }
                activeVault = vault;
                ((VaultLocator) context.getCounter().getCollection().getEquippable(Equippables.VAULT_LOCATOR)).incrementLocatedVaults();
                new VaultDialogue(message, context, m -> {
                    activeVault.reset();
                    activeVault = null;
                }, vault)
                        .play(message);
                break;
            }
        }
        return Optional.ofNullable(activeVault);
    }

    public Vault getActiveVault() {
        return activeVault;
    }

    public boolean hasActiveVault() {
        return activeVault != null;
    }

    public void dispose() {
        for (Vault vault : vaults) {
            vault.dispose();
        }
        // empty
    }

    public VaultOfPublicGoods getVaultOfPublicGoods() {
        for (Vault vault : vaults) {
            if (vault instanceof VaultOfPublicGoods) {
                return (VaultOfPublicGoods) vault;
            }
        }
        return null;
    }

}
