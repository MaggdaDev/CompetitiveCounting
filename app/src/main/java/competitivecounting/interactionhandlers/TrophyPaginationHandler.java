package competitivecounting.interactionhandlers;

import competitivecounting.Counter;
import competitivecounting.CountingBot;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.ArrayList;
import java.time.Duration;

public class TrophyPaginationHandler {
    private static final int ITEMS_PER_PAGE = 20;

    public static void sendTrophyMenu(Message message, Counter author) {
        List<Integer> ownedTrophies = author.getOwnedTrophies();

        if (ownedTrophies.isEmpty()) {
            CountingBot.write(message, "You don't own any trophies! Keep counting large numbers, and keep an eye out for numbers with a trophy emoji on them!");
            return;
        }

        List<String> trophyLines = buildTrophyLines(author);
        int totalPages = (int) Math.ceil((double) trophyLines.size() / ITEMS_PER_PAGE);
        String userId = message.getAuthor().map(User::getId).map(Snowflake::asString).orElse("");
        String content = buildPageContent(trophyLines, author, 0, totalPages);
        message.getChannel().flatMap(channel -> {
            if (totalPages <= 1) {
                return channel.createMessage(content);
            }

            Button prevButton = Button.primary("trophies:prev:" + userId + ":0", "⬅️");
            Button nextButton = Button.primary("trophies:next:" + userId + ":0", "➡️");

            return channel.createMessage(MessageCreateSpec.builder()
                    .content(content)
                    .addComponent(ActionRow.of(prevButton, nextButton))
                    .build())
                    .flatMap(sentMessage -> {
                        sentMessage.edit(MessageEditSpec.builder()
                                .addComponent(ActionRow.of(prevButton.disabled(), nextButton.disabled()))
                                .build())
                                .delaySubscription(Duration.ofMinutes(2))
                                .onErrorResume(e -> Mono.empty())
                                .subscribe();
                        return Mono.just(sentMessage);
                    });
        }).subscribe();
    }

    public static void onButtonClick(ButtonInteractionEvent event, CountingBot bot) {
        String customId = event.getCustomId();  // Id looks like: "trophies:<prev/next>:ownerUserId:page"
        String[] parts = customId.split(":");
        String direction = parts[1];
        String buttonTriggererId = parts[2];
        int currentPage = Integer.parseInt(parts[3]);
        User eventUserObject = event.getInteraction().getUser();

        if (!eventUserObject.getId().asString().equals(buttonTriggererId)) {
            event.reply("These are not your trophies, " + eventUserObject.getUsername() + "!").subscribe();
            return;
        }
        String guildId = event.getInteraction().getGuildId().map(Snowflake::asString).orElse(null);
        if (guildId == null) return;
        Counter counter = CountingBot.getCounter(guildId, buttonTriggererId);
        if (counter == null) return;

        List<String> trophyLines = buildTrophyLines(counter);
        int totalPages = (int) Math.ceil((double) trophyLines.size() / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        int selectedPage;
        if (direction.equals("prev")) {
            selectedPage = (currentPage - 1 + totalPages) % totalPages;
        } else {
            selectedPage = (currentPage + 1) % totalPages;
        }
        String content = buildPageContent(trophyLines, counter, selectedPage, totalPages);

        Button prevButton = Button.primary("trophies:prev:" + buttonTriggererId + ":" + selectedPage, "⬅️");
        Button nextButton = Button.primary("trophies:next:" + buttonTriggererId + ":" + selectedPage, "➡️");

        event.edit(InteractionApplicationCommandCallbackSpec.builder()
                    .content(content)
                    .addComponent(ActionRow.of(prevButton, nextButton))
                    .build()
        ).subscribe();
    }

    public static List<String> buildTrophyLines(Counter author) {
        List<Integer> ownedTrophies = author.getOwnedTrophies();
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < ownedTrophies.size(); i++) {
            int trophyNumber = ownedTrophies.get(i);
            String trophyDescription = TrophyHandler.getTrophyDescription(ownedTrophies.get(i));
            if (trophyNumber < 0) {
                lines.add(trophyDescription);
                continue;
            }

            int streakStartIndex = i;
            while (i < ownedTrophies.size() - 1 && ownedTrophies.get(i + 1) == ownedTrophies.get(i) + 1) {
                i++;
            }

            if (i - streakStartIndex == 0) {
                lines.add(trophyDescription);
            } else {
                lines.add("<:trophy_series:1546103799884681329> " + ownedTrophies.get(streakStartIndex) + "-" + ownedTrophies.get(i));
            }
        }
        return lines;
    }

    // unfortunately i have to make a class because java 11 does not yet have records
    private static class TrophyCount {
        final int normal;
        final int special;

        TrophyCount(int normal, int special) {
            this.normal = normal;
            this.special = special;
        }

        public int getNormal() { return normal; }
        public int getSpecial() { return special; }
        public int getTotal()  { return normal + special; }
    }

    private static TrophyCount getTotalTrophyAmount(Counter author) {
        List<Integer> ownedTrophies = author.getOwnedTrophies();

        int normalCount = 0;
        int specialCount = 0;

        for (int trophyNumber : ownedTrophies) {
            if (trophyNumber < 0) {
                specialCount++;
            } else if (trophyNumber > 0){
                normalCount++;
            }
        }

        return new TrophyCount(normalCount, specialCount);
    }

    public static String buildPageContent(List<String> lines, Counter author, int page, int totalPages) {
        StringBuilder output = new StringBuilder();
        int firstTrophyToDisplay = page * ITEMS_PER_PAGE;
        int lastTrophiahdiuwdwa = Math.min(firstTrophyToDisplay + ITEMS_PER_PAGE, lines.size());  // diddy
        TrophyCount totalTrophies = getTotalTrophyAmount(author);
        String specialTrophyString = totalTrophies.getSpecial() > 0 ? " (+" + totalTrophies.getSpecial() + " special)" : "";
        output.append("You own the following ").append(totalTrophies.getNormal()).append(specialTrophyString)
                .append(" trophies, doubling your money when you count these numbers in any base: ");
        for (int i = firstTrophyToDisplay; i < lastTrophiahdiuwdwa; i++) {
            output.append("\n").append(lines.get(i));
        }
        appendShardInfo(output, author);
        if (totalPages > 1) {
            output.append("\n\n-# Page ").append(page + 1).append(" of ").append(totalPages);
        }
        return output.toString();
    }

    private static void appendShardInfo(StringBuilder msg, @NotNull Counter author) {
        if (author.getTrophyShards() == 1) {
            msg.append("\n\nAdditionally, you own 1 trophy shard.");
        } else if (author.getTrophyShards() > 1) {
            msg.append("\n\nAdditionally, you own ").append(author.getTrophyShards()).append(" trophy shards.");
        }
    }
}
