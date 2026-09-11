package competitivecounting.interactionhandlers;

import com.google.common.base.Objects;
import competitivecounting.CountingBot;
import competitivecounting.CountingStreak;
import competitivecounting.vaults.Vault;
import competitivecounting.vaults.publicgoodsvault.PublicGoodsDocumentation;
import competitivecounting.vaults.publicgoodsvault.VaultOfPublicGoods;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.reactivestreams.Publisher;

import java.util.HashMap;
import java.util.Optional;

public class SlashCommandHandler {
    public final static String SUBMIT_KEY_COMMAND = "guesskey";
    private final static String SUBMIT_KEY_COMMAND_DESC = "Submit a guess for a vault key.";
    private final static String SUBMIT_KEY_COMMAND_KEY_ARG_NAME = "key";
    private final static String SUBMIT_KEY_COMMAND_KEY_ARG_DESC = "The submitted key";

    private final static String CROC_COINS_COMMAND = "croccoins";
    private final static String CROC_COINS_COMMAND_DESC = "Check your Croc Coins balance.";

    private final static String CONTRIBUTE_COMMAND = "contributecroccoins";
    private final static String CONTRIBUTE_COMMAND_DESC = "Contribute Croc Coins to the current " + VaultOfPublicGoods.NAME + ".";
    private final static String CONTRIBUTE_COMMAND_AMOUNT_ARG_NAME = "amount";
    private final static String CONTRIBUTE_COMMAND_AMOUNT_ARG_DESC = "The amount of Croc Coins to contribute.";

    private final static String CROCDOCS_COMMAND = "crocdocs";
    private final static String CROCDOCS_COMMAND_DESC = "Get a detailed documentation of your last " + VaultOfPublicGoods.NAME
            + ", if you played one recently.";

    private final static long COUNT_TESTING_ID = 1050060357613400144L;

    private final InteractionApplicationCommandCallbackSpec noVaultInThisChannelReplySpec,
            invalidKeyReplySpec,
            noRecentVaultOfPublicGoodsReplySpec;

    private final HashMap<String, KeySubmissionListener> vaultKeyConsumersByChannelId = new HashMap<>();

    public interface KeySubmissionListener {
        InteractionApplicationCommandCallbackSpec onKeySubmitted(String userId, long key);
    }

    public SlashCommandHandler() {
        noVaultInThisChannelReplySpec = InteractionApplicationCommandCallbackSpec.builder()
                .content("There is no active vault in this channel.")
                .ephemeral(true)
                .build();
        invalidKeyReplySpec = InteractionApplicationCommandCallbackSpec.builder()
                .content("Only non-negative integers are valid keys.")
                .ephemeral(true)
                .build();
        noRecentVaultOfPublicGoodsReplySpec = InteractionApplicationCommandCallbackSpec.builder()
                .content("You have not played a " + VaultOfPublicGoods.NAME + " in this channel recently.")
                .ephemeral(true)
                .build();
    }

    public void register(GatewayDiscordClient client) {
        try {
            long applicationId = client.getRestClient().getApplicationId().block();
            ApplicationCommandRequest vaultKeyRequest = ApplicationCommandRequest.builder()
                    .name(SUBMIT_KEY_COMMAND)
                    .description(SUBMIT_KEY_COMMAND_DESC)
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(SUBMIT_KEY_COMMAND_KEY_ARG_NAME)
                            .description(SUBMIT_KEY_COMMAND_KEY_ARG_DESC)
                            .type(ApplicationCommandOption.Type.INTEGER.getValue())
                            .required(true)
                            .build()
                    ).build();

            ApplicationCommandRequest crocCoinsRequest = ApplicationCommandRequest.builder()
                    .name(CROC_COINS_COMMAND)
                    .description(CROC_COINS_COMMAND_DESC)
                    .build();

            ApplicationCommandRequest contributeCrocCoinsRequest = ApplicationCommandRequest.builder()
                    .name(CONTRIBUTE_COMMAND)
                    .description(CONTRIBUTE_COMMAND_DESC)
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(CONTRIBUTE_COMMAND_AMOUNT_ARG_NAME)
                            .description(CONTRIBUTE_COMMAND_AMOUNT_ARG_DESC)
                            .type(ApplicationCommandOption.Type.INTEGER.getValue())
                            .required(true)
                            .build()
                    ).build();

            ApplicationCommandRequest crocDocsRequest = ApplicationCommandRequest.builder()
                    .name(CROCDOCS_COMMAND)
                    .description(CROCDOCS_COMMAND_DESC)
                    .build();

            client.getRestClient().getApplicationService()
                    .createGlobalApplicationCommand(applicationId, vaultKeyRequest)
                    .subscribe();
            client.getRestClient().getApplicationService()
                    .createGlobalApplicationCommand(applicationId, crocCoinsRequest)
                    .subscribe();
            client.getRestClient().getApplicationService()
                    .createGlobalApplicationCommand(applicationId, contributeCrocCoinsRequest)
                    .subscribe();
            client.getRestClient().getApplicationService()
                    .createGlobalApplicationCommand(applicationId, crocDocsRequest)
                    .subscribe();

             /*
            client.getRestClient().getApplicationService()
                    .createGuildApplicationCommand(applicationId, COUNT_TESTING_ID, vaultKeyRequest)
                    .subscribe();
            client.getRestClient().getApplicationService()
                    .createGuildApplicationCommand(applicationId, COUNT_TESTING_ID, crocCoinsRequest)
                    .subscribe();
            client.getRestClient().getApplicationService()
                    .createGuildApplicationCommand(applicationId, COUNT_TESTING_ID, contributeCrocCoinsRequest)
                    .subscribe();
            client.getRestClient().getApplicationService()
                    .createGuildApplicationCommand(applicationId, COUNT_TESTING_ID, crocDocsRequest)
                    .subscribe();
             */
            // For testing TODO comment out
            System.out.println("Registered global slash commands!");
        } catch (Exception e) {
            System.err.println("Failed to register global slash commands: " + e.getMessage());
            System.out.println("Continuing without slash commands. ");
        }
    }

    public Publisher handleSlashCommand(ChatInputInteractionEvent event) {
        if (Objects.equal(event.getCommandName(), SUBMIT_KEY_COMMAND)) {
            String channelId = event.getInteraction().getChannelId().asString();
            if (vaultKeyConsumersByChannelId.containsKey(channelId)) {

                try {
                    long vaultKeySubmission = event.getOption(SUBMIT_KEY_COMMAND_KEY_ARG_NAME)
                            .flatMap(ApplicationCommandInteractionOption::getValue)
                            .map(ApplicationCommandInteractionOptionValue::asLong)
                            .get();
                    if (vaultKeySubmission < 0) {
                        throw new NumberFormatException();
                    }
                    String userId = event.getInteraction().getUser().getId().asString();
                    return event.reply(vaultKeyConsumersByChannelId.get(channelId).onKeySubmitted(userId, vaultKeySubmission));
                } catch (Exception e) {
                    return event.reply(invalidKeyReplySpec);
                }
            }
            return event.reply(noVaultInThisChannelReplySpec);
        } else if (Objects.equal(event.getCommandName(), CROC_COINS_COMMAND) || Objects.equal(event.getCommandName(), CONTRIBUTE_COMMAND)) {
            String reply = getCrocCoinsReply(event);
            InteractionApplicationCommandCallbackSpec crocCoinsReply = InteractionApplicationCommandCallbackSpec.builder()
                    .content(reply)
                    .ephemeral(true)
                    .build();
            return event.reply(crocCoinsReply);
        } else if (Objects.equal(event.getCommandName(), CROCDOCS_COMMAND)) {
            String userId = event.getInteraction().getUser().getId().asString();
            String channelId = event.getInteraction().getChannelId().asString();
            Optional<CountingStreak> streakOpt = CountingBot.getInstance().getStreak(channelId);
            if (streakOpt.isEmpty()) {
                return event.reply(noRecentVaultOfPublicGoodsReplySpec);
            }
            VaultOfPublicGoods vault = streakOpt.get().getVaultSpawner().getVaultOfPublicGoods();
            if (vault == null) {
                return event.reply(noRecentVaultOfPublicGoodsReplySpec);
            }
            PublicGoodsDocumentation docs = vault.getCrocDocsForUser(userId);
            if (docs == null) {
                return event.reply(noRecentVaultOfPublicGoodsReplySpec);
            }
            return event.reply(InteractionApplicationCommandCallbackSpec.builder()
                    .content(docs.toString())
                    .ephemeral(true)
                    .build());
        }

        return event.reply();
    }

    private String getCrocCoinsReply(ChatInputInteractionEvent event) {
        String userId = event.getInteraction().getUser().getId().asString();
        String channelId = event.getInteraction().getChannelId().asString();
        Optional<CountingStreak> streakOpt = CountingBot.getInstance().getStreak(channelId);
        if (streakOpt.isEmpty()) {
            return "No streak in this channel.";
        }
        Vault vault = streakOpt.get().getVaultSpawner().getActiveVault();
        if (!(vault instanceof VaultOfPublicGoods) || ((VaultOfPublicGoods) vault).getCurrentStage() < 0) {
            return "No active " + VaultOfPublicGoods.NAME + " in this channel.";
        }
        VaultOfPublicGoods vaultOfPublicGoods = (VaultOfPublicGoods) vault;
        if (!vaultOfPublicGoods.getCrocCoinsPerPerson().containsKey(userId)) {
            return "You are not participating in the current " + VaultOfPublicGoods.NAME + ".";
        }
        if (Objects.equal(event.getCommandName(), CROC_COINS_COMMAND)) {
            return "You have " + vaultOfPublicGoods.getCrocCoinsPerPerson().get(userId) + " Croc Coins stashed away.";
        } else {
            try {
                long contributedCrocCoins = event.getOption(CONTRIBUTE_COMMAND_AMOUNT_ARG_NAME)
                        .flatMap(ApplicationCommandInteractionOption::getValue)
                        .map(ApplicationCommandInteractionOptionValue::asLong)
                        .get();
                if (vaultOfPublicGoods.hasAlreadyContributed(userId)) {
                    return "You have already contributed Croc Coins to the current stage of the " + VaultOfPublicGoods.NAME + ".";
                }
                if (contributedCrocCoins < 0 || contributedCrocCoins > VaultOfPublicGoods.CROC_COINS_PER_STAGE) {
                    return "Please contribute 0-" + VaultOfPublicGoods.CROC_COINS_PER_STAGE + " Croc Coins!";
                }
                vaultOfPublicGoods.contributeCrocCoins(userId, (int) contributedCrocCoins);
                return "You have contributed " + contributedCrocCoins + " Croc Coins to the funding of a joint lawsuit against the CrocBank Inc.\nNow you have "
                        + vaultOfPublicGoods.getCrocCoinsPerPerson().get(userId) + " Croc Coins stashed away.";
            } catch (Exception e) {
                return "Invalid number of Croc Coins!";
            }
        }
    }

    public void addKeySubmissionConsumer(String channelId, KeySubmissionListener keyConsumer) {
        if (vaultKeyConsumersByChannelId.containsKey(channelId)) {
            throw new RuntimeException("Trying to add a key submission consumer for channel " + channelId + " even though one is already present!");
        }
        vaultKeyConsumersByChannelId.put(channelId, keyConsumer);
    }

    public void removeKeySubmissionConsumer(String channelId, KeySubmissionListener keyListener) {
        if (!vaultKeyConsumersByChannelId.containsKey(channelId)) {
            return;
        }
        if (!vaultKeyConsumersByChannelId.remove(channelId, keyListener)) {
            throw new RuntimeException("Removing wrong key consumer in channel " + channelId + "!");
        }
    }
}
