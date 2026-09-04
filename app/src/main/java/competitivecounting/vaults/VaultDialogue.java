package competitivecounting.vaults;

import competitivecounting.Counter;
import competitivecounting.CountingBot;
import competitivecounting.CountingContext;
import competitivecounting.CountingEmojis;
import competitivecounting.dialogue.Dialogue;
import discord4j.core.object.entity.Message;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class VaultDialogue extends Dialogue {
    private final static long VAULT_CLAIM_TIMEOUT_SECONDS = 30;
    private Counter riddleSolver = null;

    public VaultDialogue(Message message, CountingContext context, Consumer<Message> cleanupCallback, Vault vault) {
        addEmojiReaction(CountingEmojis.VAULT_LOCATOR_ICON)
                .addWaitForEmojiReaction(CountingEmojis.VAULT_LOCATOR_ICON, false,
                        (reactionMessage) -> {},
                        new AtomicReference<>(context.getCounter().getId()),
                        VAULT_CLAIM_TIMEOUT_SECONDS, currMsg -> {
                            currMsg.removeReactions(CountingEmojis.VAULT_LOCATOR_ICON).subscribe();
                            cleanupCallback.accept(currMsg);
                            CountingBot.write(currMsg, "The " + vault.getVaultName() + " on number " + context.getCurrentNumber()
                                    + " disappeared as it was not activated within " + VAULT_CLAIM_TIMEOUT_SECONDS + " seconds.");
                            return true;
                        })
                .addRunnable((m) -> {
                    riddleSolver = vault.doRiddleBlockingly(message, context);
                })
                .addMaybeCancelRest(m -> {
                    boolean shouldCancel = riddleSolver == null;
                    if(shouldCancel) {  // TODO Dialogue.addFinally() for cleanup
                        cleanupCallback.accept(m);
                        riddleSolver = null;
                    }
                    return shouldCancel;
                })
                .addRunnable((m) -> vault.loot(m, riddleSolver))
                .addRunnable(cleanupCallback);
    }
}
