package CompetitiveCounting.vaults;

import CompetitiveCounting.CountingBot;
import CompetitiveCounting.CountingContext;
import CompetitiveCounting.CountingEmojis;
import CompetitiveCounting.CountingStreak;
import CompetitiveCounting.dialogue.Dialogue;
import CompetitiveCounting.items.equippables.Equippable;
import CompetitiveCounting.items.equippables.Equippables;
import CompetitiveCounting.items.equippables.VaultLocator;
import discord4j.core.object.entity.Message;
import org.checkerframework.checker.nullness.Opt;

import java.util.Optional;

public class VaultSpawner {
    private final Vault[] vaults = new Vault[] {
        new VaultOfLongStrides()
    };
    private Dialogue activeDialogue = null;
    private final CountingStreak streak;
    private Vault activeVault = null;
    public VaultSpawner(CountingStreak streak) {
        this.streak = streak;
    }

    public Optional<Vault> maybeSpawnVault(Message message, CountingContext context) {
        if (!context.getCounter().getCollection().containsEquippable(Equippables.VAULT_LOCATOR)) {
            return Optional.empty();
        }
        if (activeVault != null) {
            return Optional.empty();
        }

        for (Vault vault : vaults) {
            if (vault.maybeSpawn(context)) {
                activeVault = vault;
                ((VaultLocator)context.getCounter().getCollection().getEquippable(Equippables.VAULT_LOCATOR)).incrementLocatedVaults();
                activeDialogue = new Dialogue().addEmojiReaction(CountingEmojis.VAULT_LOCATOR_ICON)
                        .addWaitForEmojiReaction(CountingEmojis.VAULT_LOCATOR_ICON, true, (reactionMessage) -> {
                            if (activeVault != vault) {
                                System.err.println("No/different active vault when user reacted vault locator icon?");
                                return;
                            }
                            activeVault.doRiddle(message, context);
                        }, Optional.of(context.getCounter().getId()))
                        .addRunnable((m) ->  activeDialogue = null);

                activeDialogue.play(message);
                break;
            }
        }
        return Optional.ofNullable(activeVault);
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
