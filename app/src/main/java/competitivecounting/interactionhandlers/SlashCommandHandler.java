package competitivecounting.interactionhandlers;

import com.google.common.base.Objects;
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

public class SlashCommandHandler {
    public final static String SUBMIT_KEY_COMMAND = "guesskey";
    private final static String SUBMIT_KEY_COMMAND_DESC = "Submit a guess for a vault key.";

    private final static String SUBMIT_KEY_COMMAND_KEY_ARG_NAME = "key";
    private final static String SUBMIT_KEY_COMMAND_KEY_ARG_DESC = "The submitted key";

    private final static long COUNT_TESTING_ID = 1050060357613400144L;

    private final InteractionApplicationCommandCallbackSpec noVaultInThisChannelReplySpec;
    private final InteractionApplicationCommandCallbackSpec invalidKeyReplySpec;

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
            client.getRestClient().getApplicationService()
                    .createGlobalApplicationCommand(applicationId, vaultKeyRequest)
                    .subscribe();
            client.getRestClient().getApplicationService()
                    .createGuildApplicationCommand(applicationId, COUNT_TESTING_ID, vaultKeyRequest)
                    .subscribe();
            System.out.println("Registered global slash commands!");
        } catch (Exception e) {
            System.err.println("Failed to register global slash commands: " + e.getMessage());
            System.out.println("Continuing without slash commands. ");
        }
    }

    public Publisher handleSlashCommand(ChatInputInteractionEvent event) {
        if (!Objects.equal(event.getCommandName(), SUBMIT_KEY_COMMAND)) {
            return event.reply();
        }
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
