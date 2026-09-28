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

    public static void vaultInfo(Message message, Optional<CountingStreak> streak, Counter commandIssuer) {
        StringBuilder s = new StringBuilder();
        commandIssuer.getCollection().getEquippable(Equippables.VAULT_LOCATOR).ifPresentOrElse(
                eq -> {
                    s.append("Your ")
                            .append(eq.getName())
                            .append(" allows you to locate locked vaults");
                    if (eq.getLevel() > 1) {
                        s.append(" with ")
                                .append(Util.bonusMultToAddPercString(((VaultLocator) eq).getVaultChanceBonusMultiplier()))
                                .append(" bonus chance!\n");
                    } else {
                        s.append("!\n");
                    }
                },
                () -> s.append("If you add a " + VaultLocator.NAME + " to your collection, you will be able to locate locked "
                        + "vaults containing potentially enormous sums of money and rare items.\n")
        );
        CountingContext lastContext = streak.map(CountingStreak::getLastCountingContext).orElse(null);
        int amountOfEligibleVaults = 0;
        boolean lastCounterMissingVaultLocator = false;
        if (lastContext == null) {
            s.append("If you meet their requirements, you will spawn them at their respective spawn rate:\n");
        } else {
            for (Vault vault : ALL_VAULTS) {
                if (vault.canSpawn(lastContext)) {
                    amountOfEligibleVaults++;
                }
            }
            lastCounterMissingVaultLocator = !lastContext.getCounter().getCollection().containsEquippable(Equippables.VAULT_LOCATOR);
            if (lastCounterMissingVaultLocator) {
                s.append("On the last count, no vault could have been located, as ")
                        .append(lastContext.getCounter().getName())
                        .append(" does not have a ")
                        .append(VaultLocator.NAME)
                        .append(" in their collection.\n");
            } else if (amountOfEligibleVaults == 0) {
                s.append("The last count did not meet the requirements of any vault!\n");
            } else if (amountOfEligibleVaults == 1) {
                s.append("On the last count, one vault could potentially be located with the following chance:\n");
            } else {
                s.append("On the last count, multiple vaults could potentially be located with the following chances:\n");
            }
        }
        for (Vault vault : ALL_VAULTS) {
            double spawnChance = vault.getSpawnChance();
            spawnChance = commandIssuer.getCountingBoosterManager().modifyVaultRate(spawnChance);
            if (lastContext != null) {
                if (lastCounterMissingVaultLocator || !vault.canSpawn(lastContext)) {
                    continue;
                }
                spawnChance = lastContext.getCounter().getCollection().modifyVaultRateFromEquippables(spawnChance, lastContext);
            } else {
                spawnChance = commandIssuer.getCollection().modifyVaultRateFromEquippables(spawnChance, null);
            }
            s.append("- ")
                    .append(vault.getVaultName())
                    .append(": ")
                    .append(vault.getSpawnConditionsDescription())
                    .append(spawnChance <= 0 ? "" : " (" + Util.oddsStringFromProbAndModifiedProb(vault.getSpawnChance(), spawnChance) + ")\n");
        }
        CountingBot.write(message, s.toString());
    }

    public void maybeSpawnVault(Message message, CountingContext context) {
        previousContext = context;
        if (!context.getCounter().getCollection().containsEquippable(Equippables.VAULT_LOCATOR)) {
            return;
        }
        if (activeVault != null) {
            return;
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
                context.getCounter().getCollection().getEquippable(Equippables.VAULT_LOCATOR)
                        .ifPresent(e -> ((VaultLocator) e).incrementLocatedVaults());
                new VaultDialogue(message, context, m -> {
                    activeVault.reset();
                    activeVault = null;
                }, vault)
                        .play(message);
                break;
            }
        }
    }

    public Vault getActiveVault() {
        return activeVault;
    }

    public boolean hasRunningVault() {
        if (activeVault == null) {
            return false;
        }
        return activeVault.isRunning();
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
