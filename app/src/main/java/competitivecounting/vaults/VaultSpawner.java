package competitivecounting.vaults;

import competitivecounting.*;
import competitivecounting.dialogue.Dialogue;
import competitivecounting.items.equippables.Equippables;
import competitivecounting.items.equippables.VaultLocator;
import competitivecounting.vaults.trophyvault.TrophyVault;
import discord4j.core.object.entity.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VaultSpawner {
    private final static Vault[] ALL_VAULTS = {
            new VaultOfLongStrides(),
            new CommunityVault(),
            new PrimeVault(),
            new TrophyVault()
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
        String s = streak.isEmpty() ? getStaticVaultInfo(counter) : streak.get().getVaultSpawner().getStreakVaultInfo(counter);
        CountingBot.write(message, s);
    }

    private String getStreakVaultInfo(Counter counter) {
        String ret = getStaticVaultInfo(counter);
        if (previousContext == null) {
            return ret;
        }

        List<Vault> eligibleVaults = new ArrayList<>();
        for (Vault vault : vaults) {
            if (vault.canSpawn(previousContext)) {
                eligibleVaults.add(vault);
            }
        }
        ret += "\n";
        if (eligibleVaults.size() == 0) {
            ret += "The last count did not meet the requirements of any vault!";
        } else if (eligibleVaults.size() == 1) {
            ret += "The last count only met the requirements for the " + eligibleVaults.get(0).getVaultName() + ".";
        } else {
            ret += "The last count met the requirements of the following vaults: \n";
            for (int i = 0; i < eligibleVaults.size(); i++) {
                ret += eligibleVaults.get(i).getVaultName();
                if (i == eligibleVaults.size() - 2) {
                    ret += " & ";
                } else if (i <= eligibleVaults.size() - 3) {
                    ret += ", ";
                }
            }
        }
        return ret;

    }

    private static String getStaticVaultInfo(Counter counter) {
        String s = "If you have equipped a " + VaultLocator.NAME + ", you are are capable of finding rare vaults! If you meet their requirements, they will spawn at their respective spawn rate:\n";
        for (Vault vault : ALL_VAULTS) {
            int odds = (int) Math.round(1. / vault.getSpawnChance());
            int oddsWithBoni = (int) Math.round(1. / counter.getCountingBoosterManager().modifyVaultRate(vault.getSpawnChance()));
            s += "- " + vault.getVaultName() + ": " + vault.getSpawnConditionsDescription() +
                    " (1 in " + Util.valueAndValueWithBoniToString(odds, oddsWithBoni) + ")\n";
        }
        return s;
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
                activeVault = vault;
                ((VaultLocator) context.getCounter().getCollection().getEquippable(Equippables.VAULT_LOCATOR)).incrementLocatedVaults();
                new VaultDialogue(message, context, m -> {
                    vault.reset();
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

    public void dispose() {
        for (Vault vault : vaults) {
            vault.dispose();
        }
        // empty
    }


}
