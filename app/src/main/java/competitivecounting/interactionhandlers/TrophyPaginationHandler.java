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
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class TrophyPaginationHandler {
    private static final int ITEMS_PER_PAGE = 20;
    private static final int ITEMS_PER_GRID = 100;
    private static final String[] GRID_ROW_EMOJIS = {
            ":zero::zero:", ":one::zero:", ":two::zero:", ":three::zero:", ":four::zero:",
            ":five::zero:", ":six::zero:", ":seven::zero:", ":eight::zero:", ":nine::zero:"
    };
    private static final String[] GRID_COLUMN_EMOJIS = {
            ":one:", ":two:", ":three:", ":four:", ":five:",
            ":six:", ":seven:", ":eight:", ":nine:", ":zero:"
    };


    public static void sendTrophyMenu(Message message, Counter author) {
        sendTrophyMenu(message, author, 0);
    }

    public static void sendTrophyMenu(Message message, Counter author, int mode) {
        final int selectedMode = (mode == 1) ? 1 : 0;
        String userId = message.getAuthor().map(User::getId).map(Snowflake::asString).orElse("");
        final String content;
        final ActionRow components;
        final int totalPages;

        if (selectedMode == 1) {
            List<Integer> gridStarts = getNonEmptyGridStarts(author);  // gridStarts = 1, 101, 201, etc
            totalPages = gridStarts.size();
            if (gridStarts.isEmpty()) {
                content = "You don't own any normal trophies to display in a grid. "
                        + "Your special trophies are still available in list view.";
                components = buildModeOnlyButton(userId, 0, false);
            } else {
                content = buildGridPageContent(author, gridStarts, 0);
                components = buildTrophyButtons(
                        userId, 0, 1, totalPages, false
                );
            }
        } else {
            if (author.getOwnedTrophies().isEmpty()) {
                CountingBot.write(message, "You don't own any trophies! Keep counting large numbers, and keep an eye out for numbers with a trophy emoji on them!");
                return;
            }
            List<String> trophyLines = buildTrophyLines(author);
            totalPages = Math.max(
                    1,
                    (int) Math.ceil((double) trophyLines.size() / ITEMS_PER_PAGE)
            );
            content = buildPageContent(trophyLines, author, 0, totalPages);
            components = buildTrophyButtons(
                    userId, 0, 0, totalPages, false
            );
        }

        message.getChannel()
                .flatMap(channel -> channel.createMessage(
                        MessageCreateSpec.builder()
                                .content(content)
                                .addComponent(components)
                                .build()
                ))
                .flatMap(sentMessage -> {
                    // timeout after 2 minutes (maybe takes 2.5 minutes?? not sure)
                    ActionRow disabledComponents;

                    if (selectedMode == 1 && totalPages == 0) {
                        disabledComponents = buildModeOnlyButton(
                                userId, 0, true
                        );
                    } else {
                        disabledComponents = buildTrophyButtons(
                                userId, 0, selectedMode, totalPages, true
                        );
                    }

                    sentMessage.edit(
                                    MessageEditSpec.builder()
                                            .addComponent(disabledComponents)
                                            .build()
                            )
                            .delaySubscription(Duration.ofMinutes(2))
                            .onErrorResume(e -> Mono.empty())
                            .subscribe();

                    return Mono.just(sentMessage);
                })
                .subscribe();
    }

    public static void onButtonClick(
            ButtonInteractionEvent event,
            CountingBot bot
    ) {
        String[] parts = event.getCustomId().split(":");

        if (parts.length < 4) return;

        String action = parts[1];
        String buttonTriggererId = parts[2];

        int currentPage;
        int mode = 0;

        try {
            currentPage = Integer.parseInt(parts[3]);

            if (parts.length >= 5) mode = Integer.parseInt(parts[4]);
        } catch (NumberFormatException e) {
            event.reply("This trophy menu is invalid.").subscribe();
            return;
        }

        if (mode != 1) mode = 0;

        if ("mode".equals(action)) currentPage = 0;

        User eventUser = event.getInteraction().getUser();

        if (!eventUser.getId().asString().equals(buttonTriggererId)) {
            event.reply(
                    "These are not your trophies, " + eventUser.getUsername() + "!"
            ).subscribe();
            return;
        }

        String guildId = event.getInteraction()
                .getGuildId()
                .map(Snowflake::asString)
                .orElse(null);

        if (guildId == null) {
            event.reply("this is serverside only. you check it?").subscribe();
            return;
        }

        Counter counter = CountingBot.getCounter(guildId, buttonTriggererId);

        if (counter == null) {
            event.reply("no counter data!").subscribe();
            return;
        }

        final String content;
        final ActionRow components;

        if (mode == 1) {  // same shit again
            List<Integer> gridStarts = getNonEmptyGridStarts(counter);

            if (gridStarts.isEmpty()) {
                content = "You don't own any normal trophies to display in a grid. Your special trophies are still available in list view.";

                components = buildModeOnlyButton(buttonTriggererId, 0, false);
            } else {
                int totalPages = gridStarts.size();

                int selectedPage = selectPage(action, currentPage, totalPages);

                content = buildGridPageContent(counter, gridStarts, selectedPage);

                components = buildTrophyButtons(
                        buttonTriggererId,
                        selectedPage,
                        1,
                        totalPages,
                        false
                );
            }
        } else {
            // have original line view with trophies stacked downward
            List<String> trophyLines = buildTrophyLines(counter);

            int totalPages = Math.max(
                    1,
                    (int) Math.ceil(
                            (double) trophyLines.size() / ITEMS_PER_PAGE
                    )
            );

            int selectedPage = selectPage(
                    action, currentPage, totalPages
            );

            content = buildPageContent(
                    trophyLines, counter, selectedPage, totalPages
            );

            components = buildTrophyButtons(
                    buttonTriggererId,
                    selectedPage,
                    0,
                    totalPages,
                    false
            );
        }

        event.edit(
                InteractionApplicationCommandCallbackSpec.builder()
                        .content(content)
                        .addComponent(components)
                        .build()
        ).subscribe();
    }

    private static List<Integer> getNonEmptyGridStarts(Counter author) {
        Set<Integer> starts = new TreeSet<>();  // weil sortieren und so

        for (int trophyNumber : author.getOwnedTrophies()) {
            // ignore special trophies for grid mode (-1050505 etc)
            if (trophyNumber > 0) {
                int gridStart =
                        ((trophyNumber - 1) / ITEMS_PER_GRID) * ITEMS_PER_GRID + 1;

                starts.add(gridStart);
            }
        }
        return new ArrayList<>(starts);
    }

    private static String buildGridPageContent(Counter author, List<Integer> gridStarts, int page) {
        int rangeStart = gridStarts.get(page);
        Set<Integer> owned = new HashSet<>();

        for (int trophyNumber : author.getOwnedTrophies()) {
            if (trophyNumber > 0) {
                owned.add(trophyNumber);
            }
        }

        StringBuilder output = new StringBuilder();

        output.append(rangeStart)
                .append("-")
                .append((long) rangeStart + ITEMS_PER_GRID - 1);
        output.append("\n:man_in_motorized_wheelchair_facing_right::skin-tone-1::woman_in_motorized_wheelchair::skin-tone-1:");  // 🔥

        for (String label : GRID_COLUMN_EMOJIS) {
            output.append(label);
        }

        // 10x10 griido
        for (int row = 0; row < 10; row++) {
            output.append("\n").append(GRID_ROW_EMOJIS[row]);

            for (int column = 0; column < 10; column++) {
                int number = rangeStart + row * 10 + column;

                output.append(owned.contains(number) ? "🏆" : "❌");
            }
        }

        appendShardInfo(output, author);

        if (gridStarts.size() > 1) {
            output.append("\n\n-# Page ")
                    .append(page + 1)
                    .append(" of ")
                    .append(gridStarts.size());
        }

        return output.toString();
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

    private static ActionRow buildTrophyButtons(
            String userId,
            int page,
            int mode,
            int totalPages,
            boolean disabled
    ) {
        Button prevButton = Button.primary(
                "trophies:prev:" + userId + ":" + page + ":" + mode,
                "⬅️"
        );

        Button nextButton = Button.primary(
                "trophies:next:" + userId + ":" + page + ":" + mode,
                "➡️"
        );

        Button modeButton = Button.secondary(
                "trophies:mode:" + userId + ":0:" + (mode == 0 ? 1 : 0),
                mode == 0 ? "Grid view" : "List view"
        );

        if (disabled || totalPages <= 1) {
            prevButton = prevButton.disabled();
            nextButton = nextButton.disabled();
        }

        if (disabled) {
            modeButton = modeButton.disabled();
        }

        return ActionRow.of(prevButton, nextButton, modeButton);
    }

    private static ActionRow buildModeOnlyButton(String userId, int targetMode, boolean disabled) {  // targetMode = wo man hinswitchen will
        Button button = Button.secondary(
                "trophies:mode:" + userId + ":0:" + targetMode,
                targetMode == 0 ? "List view" : "Grid view"
        );

        if (disabled) {
            button = button.disabled();
        }

        return ActionRow.of(button);
    }

    private static int selectPage(
            String action,
            int currentPage,
            int totalPages
    ) {
        if ("mode".equals(action) || totalPages <= 1) {
            return 0;
        }

        if ("prev".equals(action)) {
            return Math.floorMod(currentPage - 1, totalPages);
        }

        return (currentPage + 1) % totalPages;
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
