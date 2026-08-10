package CompetitiveCounting.vaults;

import CompetitiveCounting.*;
import CompetitiveCounting.dialogue.Dialogue;
import CompetitiveCounting.items.equippables.Equippables;
import CompetitiveCounting.items.equippables.VaultLocator;
import discord4j.core.object.entity.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class VaultSpawner {
    private final static Vault[] ALL_VAULTS = {
            new VaultOfLongStrides(),
            new CommunityVault(),
            new PrimeVault()
    };
    private final Vault[] vaults;
    private Dialogue activeDialogue = null;
    private final CountingStreak streak;
    private Vault activeVault = null;
    private Counter riddleSolver = null;
    private CountingContext previousContext;

    public VaultSpawner(CountingStreak streak) {
        this.streak = streak;
        vaults = new Vault[ALL_VAULTS.length];
        for (int i = 0; i < ALL_VAULTS.length; i++) {
            try {
                vaults[i] = ALL_VAULTS[i].getClass().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    public static void vaultInfo(Message message, Optional<CountingStreak> streak) {
        String s = streak.isEmpty() ? getStaticVaultInfo() : streak.get().getVaultSpawner().getStreakVaultInfo();
        CountingBot.write(message, s);
    }

    private String getStreakVaultInfo() {
        String ret = getStaticVaultInfo();
        if (previousContext == null) {
            return ret;
        }

        List<Vault> eligibleVaults = new ArrayList<>();
        for (Vault vault: vaults) {
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
            for (int i = 0; i < eligibleVaults.size(); i ++) {
                ret += eligibleVaults.get(i).getVaultName();
                if(i == eligibleVaults.size() - 2) {
                    ret += " & ";
                } else if(i <= eligibleVaults.size() - 3) {
                    ret += ", ";
                }
            }
        }
        return ret;

    }

    private static String getStaticVaultInfo() {
        String s = "If you have equipped a " + VaultLocator.NAME + ", you are are capable of finding rare vaults! If you meet their requirements, they will spawn at their respective spawn rate:\n";
        for (Vault vault : ALL_VAULTS) {
            s += "- " + vault.getVaultName() + ": " + vault.getSpawnConditionsDescription() + " (1 in " + Math.round(1 / vault.getSpawnChance()) + ")\n";
        }
        return s;
    }

    public Optional<Vault> maybeSpawnVault(Message message, CountingContext context) {
        if (!context.getCounter().getCollection().containsEquippable(Equippables.VAULT_LOCATOR)) {
            return Optional.empty();
        }
        if (activeVault != null) {
            return Optional.empty();
        }
        previousContext = context;
        for (Vault vault : vaults) {
            if (vault.maybeSpawn(context)) {
                activeVault = vault;
                ((VaultLocator) context.getCounter().getCollection().getEquippable(Equippables.VAULT_LOCATOR)).incrementLocatedVaults();
                activeDialogue = new Dialogue().addEmojiReaction(CountingEmojis.VAULT_LOCATOR_ICON)
                        .addWaitForEmojiReaction(CountingEmojis.VAULT_LOCATOR_ICON, false, (reactionMessage) -> {
                            if (activeVault != vault) {
                                System.err.println("No/different active vault when user reacted vault locator icon?");
                                return;
                            }
                            riddleSolver = activeVault.doRiddleBlockingly(message, context);
                        }, new AtomicReference<>(context.getCounter().getId()))
                        .addRunnable((m) -> activeVault.loot(m, riddleSolver))
                        .addRunnable((m) -> {
                            activeVault.reset();
                            activeVault = null;
                            activeDialogue = null;
                            riddleSolver = null;
                            System.out.println("Reset!");
                        });

                activeDialogue.play(message);
                break;
            }
        }
        return Optional.ofNullable(activeVault);
    }

    public Vault getActiveVault() {
        return activeVault;
    }

    public void dispose() {
        if (activeDialogue != null) {
            activeDialogue.stop();
        }
        for (Vault vault : vaults) {
            vault.dispose();
        }
        // empty
    }


}
