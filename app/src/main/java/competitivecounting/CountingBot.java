/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competitivecounting;

import competitivecounting.Parser.TradeOfferParser.TradeOfferChecker;
import competitivecounting.bank.Bank;
import competitivecounting.bank.BankAccount;
import competitivecounting.bank.BankCommandHandler;
import competitivecounting.bank.BankTransactionsHandler;
import competitivecounting.bank.exceptions.BankTransactionException;
import competitivecounting.contracts.Contract;
import competitivecounting.interactionhandlers.*;
import competitivecounting.items.CollectionCommandHandler;
import competitivecounting.items.InventoryCommandHandler;
import competitivecounting.items.PrimeCoinSeller;
import competitivecounting.items.ShopCommandHandler;
import competitivecounting.storage.Storage;
import competitivecounting.tradeoffer.TradeHandler;
import competitivecounting.tradeoffer.TradeOffer;
import competitivecounting.vaults.VaultSpawner;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.MessageCreateSpec;
import org.jetbrains.annotations.NotNull;
import reactor.core.Disposable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author DavidPrivat
 */
public class CountingBot {

    private final static String commandIndicator = "~";
    private final Storage storage;
    private final HashMap<String, CountingGuild> guilds;
    private final HashMap<String, CountingStreak> streaks;

    // Command handlers
    private final BankCommandHandler bankCommandHandler;
    private final BankTransactionsHandler bankTransitionsHandler;
    private final ShopCommandHandler shopCommandHandler;
    private final InventoryCommandHandler inventoryCommandHandler;
    private final UserAnswerHandler userAnswerHandler, userDMHandler;
    private final SlashCommandHandler slashCommandHandler;
    private static CountingBot instance;
    private static int currId = 0;
    private static GatewayDiscordClient client;

    private final static boolean isDevMode = true;

    private final PrimeCoinSeller primeCoinSeller;

    public CountingBot(GatewayDiscordClient client) {
        streaks = new HashMap<>();
        storage = new Storage(streaks);
        this.client = client;
        guilds = storage.loadGuilds();
        System.out.println("Counters loaded!");
        instance = this;

        bankTransitionsHandler = new BankTransactionsHandler(guilds);
        bankCommandHandler = new BankCommandHandler(bankTransitionsHandler);
        shopCommandHandler = new ShopCommandHandler(guilds);
        inventoryCommandHandler = new InventoryCommandHandler(guilds);
        userAnswerHandler = new UserAnswerHandler();
        userDMHandler = new UserAnswerHandler();
        storage.loadStreaksIntoMapIfFilePresent();

        slashCommandHandler = new SlashCommandHandler();
        setupSlashCommandHandler();

        primeCoinSeller = new PrimeCoinSeller();
    }

    private void setupSlashCommandHandler() {
        slashCommandHandler.register(client);
        client.on(ChatInputInteractionEvent.class, slashCommandHandler::handleSlashCommand).subscribe();
    }


    public void message(Message message) {
        addGuildOrCounterIfNotYetRegistered(message);
        userAnswerHandler.handleUserMessage(message);
        checkCommands(message);
        count(message);

    }

    private void checkCommands(Message message) {
        String content = message.getContent();
        try {
            if (content.startsWith(commandIndicator)) {
                addGuildOrCounterIfNotYetRegistered(message);
                String commandWithoutIndicator = content.substring(commandIndicator.length()).trim(); // fixed 30 iterations of 'commandIndicator + "command"'
                Optional<CountingStreak> streak = Optional.ofNullable(streaks.get(message.getChannelId().asString()));
                if (commandWithoutIndicator.startsWith("help")) {
                    write(message, "tasukete kudasai!\nhttp://hyperlexus.net/old/competitivecountinghelp.html");
                } else if (commandWithoutIndicator.startsWith("scoreboard") || commandWithoutIndicator.equals("top")) {
                    write(message, scoreboard(message, "bal"));
                } else if (commandWithoutIndicator.startsWith("topnetworth")) {
                    write(message, scoreboard(message, "networth"));
                } else if (commandWithoutIndicator.startsWith("score")) {
                    this.scoreInfo(message);
                } else if (commandWithoutIndicator.startsWith("networth")) {
                    this.netWorthInfo(message);
                } else if (commandWithoutIndicator.startsWith("num")) {
                    numberInfo(message);
                } else if (commandWithoutIndicator.startsWith("addrule")) {
                    addRule(message);
                } else if (commandWithoutIndicator.startsWith("rules")) {
                    rules(message);
                } else if (commandWithoutIndicator.startsWith("unlock ") || commandWithoutIndicator.equals("unlock")) {
                    unlock(message);
                } else if (commandWithoutIndicator.equals("prestige")) {
                    prestige(message);
                } else if (commandWithoutIndicator.startsWith("daily")) {
                    daily(message);
                } else if (commandWithoutIndicator.startsWith("bases")) {
                    basesOwnedInfo(message);
                } else if (commandWithoutIndicator.startsWith("base")) {
                    baseInfo(message);
                } else if (commandWithoutIndicator.startsWith("tradeoffer")) {
                    tradeOffer(message);
                } else if (commandWithoutIndicator.startsWith("contracts")) {
                    contractInfo(message);
                } else if (commandWithoutIndicator.startsWith("removecontract")) {
                    removeContract(message);
                } else if (commandWithoutIndicator.startsWith("person") || commandWithoutIndicator.startsWith("counter") || commandWithoutIndicator.startsWith("last")) {
                    personInfo(message);
                } else if (commandWithoutIndicator.startsWith("fact") || commandWithoutIndicator.startsWith("mult")) {
                    factorInfo(message);
                } else if (commandWithoutIndicator.startsWith("streak")) {
                    streakInfo(message);
                } else if (commandWithoutIndicator.startsWith("trophies") || commandWithoutIndicator.startsWith("trophy")) {
                    trophiesInfo(message);
                } else if (commandWithoutIndicator.startsWith("shunlock")) {
                    shunlock(message);
                } else if (commandWithoutIndicator.startsWith("bank")) {
                    if (bankCommandHandler.handleBankCommand(message)) {  // Returns true iff json should be updated
                        save();
                    }
                } else if (commandWithoutIndicator.startsWith("shop") || commandWithoutIndicator.equals("unlock_shop")) {
                    shopCommandHandler.handleShopCommand(message);
                } else if (commandWithoutIndicator.startsWith("inventory ") || commandWithoutIndicator.startsWith("inv ") || commandWithoutIndicator.equals("inventory") || commandWithoutIndicator.equals("inv")) {
                    inventoryCommandHandler.handleInventoryCommand(message, streak);
                } else if (commandWithoutIndicator.startsWith(CollectionCommandHandler.COMMAND_INDICATOR)) {
                    CollectionCommandHandler.handleCollectionCommand(message, streak);
                } else if (commandWithoutIndicator.startsWith("vault")) {
                    VaultSpawner.vaultInfo(message, streak, getCounterFromMessage(message));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void daily(Message message) {
        Counter author = getCounterFromMessage(message);
        author.daily(message);
    }

    private void shunlock(Message message) {
        write(message, "Coming soon...");
    }

    private void basesOwnedInfo(Message message) {
        Counter author = getCounterFromMessage(message);
        if (author.getUnlockedBases().length == 0) {
            if (author.getPrestiges() == 0) {
                write(message, "How do you know about bases? You don't even have any prestige points yet!");
            } else {
                write(message, "You don't own any base yet. Unlock them in the ~unlock shop for some prestige points!");
            }
        } else if (author.getUnlockedBases().length == 1) {
            write(message, "You only own base " + author.getUnlockedBases()[0] + ".");
        } else {
            String msg = "You own the following bases: ";
            for (int i = 0; i < author.getUnlockedBases().length - 1; i++) {
                msg += author.getUnlockedBases()[i] + ", ";
            }
            msg += author.getUnlockedBases()[author.getUnlockedBases().length - 1] + ".";
            write(message, msg);
        }
    }

    private void trophiesInfo(Message message) {
        Counter author = getCounterFromMessage(message);
        StringBuilder msg = new StringBuilder();

        if (author.getOwnedTrophies().size() == 0) {
            msg.append("You don't own any trophies. Keep counting large numbers, and keep an eye out for numbers with a trophy emoji on them!");
        } else if (author.getOwnedTrophies().size() == 1) {
            String specialTrophyMessage = TrophyHandler.getTrophyDescription(author.getOwnedTrophies().get(0));
            if (!specialTrophyMessage.isEmpty()) {
                msg.append("You own one trophy:\n").append(specialTrophyMessage);
            } else {
                msg.append("You own the ").append(author.getOwnedTrophies().get(0)).append("-trophy.");
                msg.append("\n\nCounting this number in any base will give you twice the money.");
            }
        } else {
            // The list from author.getOwnedTrophies() is sorted in ascending order. Write the owned messages to the String builder, but summarize subsequent tropies using something like trophies 4-7
            List<Integer> ownedTrophies = author.getOwnedTrophies();
            int streakStartIndex;
            for (int i = 0; i < ownedTrophies.size(); i++) {
                // special trophies
                String specialTrophyMessage = TrophyHandler.getTrophyDescription(ownedTrophies.get(i));
                if (!specialTrophyMessage.isEmpty()) {
                    msg.append("\n").append(specialTrophyMessage);
                    continue;
                }

                // Usual trophies
                streakStartIndex = i;
                while (i < ownedTrophies.size() - 1 && ownedTrophies.get(i + 1) == ownedTrophies.get(i) + 1) {
                    i++;
                }
                if (i - streakStartIndex == 0) {
                    msg.append("\n").append(ownedTrophies.get(i)).append(" trophy");
                } else {
                    msg.append("\ntrophies ").append(ownedTrophies.get(streakStartIndex)).append(" to ").append(ownedTrophies.get(i));
                }
            }
            msg.append("\n\nCounting these numbers in any base will give you twice the money.");
        }

        if (author.getTrophyShards() == 1) {
            msg.append("\n\nAdditionally, you own 1 trophy shard.");
        } else if (author.getTrophyShards() > 1) {
            msg.append("\n\nAdditionally, you own ").append(author.getTrophyShards()).append(" trophy shards.");
        }

        write(message, msg.toString());
    }


    private void factorInfo(Message message) {
        String channelId = message.getChannelId().asString();
        Counter author = getCounterFromMessage(message);
        if (streaks.containsKey(channelId)) {
            CountingStreak streak = streaks.get(channelId);
            String msg = "Your collected score in this streak gets a " + Math.round(author.getBonusFact(streak) * 100.0) / 100.0 + "x bonus multiplier.";
            write(message, msg);
        } else {
            write(message, "There is no active streak in this channel.");
        }
    }

    private void personInfo(Message message) {
        String channelId = message.getChannelId().asString();
        Counter author = getCounterFromMessage(message);
        if (streaks.containsKey(channelId)) {
            Counter lastCounter = streaks.get(channelId).getLastCounter();
            if (lastCounter == null) {
                CountingBot.write(message, "No current streak! You can be the first person to count.");
                return;
            }
            if (lastCounter.getId().equals(author.getId())) {
                CountingBot.write(message, "Attention! You were the last person counting! Don't count now!");
            } else {
                CountingBot.write(message, lastCounter.getName() + " counted the last number. You can count safely now.");
            }
        } else {
            CountingBot.write(message, "No current streak! You can be the first person to count.");
        }
    }

    private void numberInfo(Message message) {
        String channelId = message.getChannelId().asString();
        if (!streaks.containsKey(channelId)) {
            write(message, "No current streak! You can start with 1.");
            return;
        }
        CountingStreak streak = streaks.get(channelId);
        int base = streak.getBase();
        int lastNumberCounted = streak.getLastNum();
        String lastNumberInBase = BaseSystems.decimalToSystem(lastNumberCounted, base);
        write(message, "The last number was **" + (base == 10 ? lastNumberCounted + "**." : lastNumberInBase + "** (=" + lastNumberCounted + ")."));
    }

    private void streakInfo(Message message) {
        String channelId = message.getChannelId().asString();
        if (!streaks.containsKey(channelId)) {
            CountingBot.write(message, "No current streak! You can be the first person to count.");
            return;
        }
        Counter author = getCounterFromMessage(message);
        CountingStreak streak = streaks.get(channelId);

        int lastNumberCounted = streak.getLastNum();
        int base = streak.getBase();
        double streakFactor = streak.getTimeRulesBonusFact();
        Counter lastCounter = streak.getLastCounter();
        if (lastCounter == null) {
            CountingBot.write(message, "No current streak! You can be the first person to count.");
            return;
        }

        String lastNumberInBase = BaseSystems.decimalToSystem(lastNumberCounted, base);

        String ruleInfo = streak.getCompactRulesInfo();
        String baseInfo = streak.getBaseInfoRespond(1);
        String factorInfo = "**Everyone**'s score multiplier: " + streakFactor + "x\n" +
                "**Your** score multiplier: " + Math.round(author.getBonusFact(streak) * 100.0) / 100.0 + "x";
        class PendingPayout {
            final Counter counter;
            final int amount;
            PendingPayout(Counter counter, int amount) {
                this.counter = counter;
                this.amount = amount;
            }
        }

        List<PendingPayout> pendingPayouts = new ArrayList<>();
        String guildId = streak.getGuildId();
        for (String counterId : streak.getAmountOfCountsPerCounter().keySet()) {
            Counter c = CountingBot.getCounter(guildId, counterId);
            int pendingScore = c.getPendingStreakScore(streak);

            if (pendingScore > 0) {
                pendingPayouts.add(new PendingPayout(c, pendingScore));
            }
        }
        pendingPayouts.sort((a, b) -> Integer.compare(b.amount, a.amount));

        StringBuilder pendingInfo = new StringBuilder();
        if (!pendingPayouts.isEmpty()) {
            pendingInfo.append("\n\nParticipants of this streak stand to gain:");
            int position = 1;
            for (PendingPayout p : pendingPayouts) {
                if (position > 5) {
                    int remainingAmountCounters = pendingPayouts.size() - 5;
                    pendingInfo.append("\n... and ").append(remainingAmountCounters).append(" more counters");
                    break;  // todo test this (don't have 6 accounts)
                }
                pendingInfo.append("\n").append(position).append(". ").append(p.counter.getName())
                        .append(": **").append(p.amount).append("** money");
                position++;
            }
        }

        String resultInfo = "Information about the current streak:\n\n";
        resultInfo += "**" + lastCounter.getName() + "** counted the previous number.";
        resultInfo += "\nThe last number was **" + (base == 10 ? lastNumberCounted + "**." : lastNumberInBase + "** (=" + lastNumberCounted + ").");
        resultInfo += "\n\n" + baseInfo;
        resultInfo += "\n\n" + factorInfo;
        resultInfo += "\n\n" + ruleInfo;
        resultInfo += pendingInfo.toString();
        CountingBot.write(message, resultInfo);
    }

    private void contractInfo(Message message) {
        Counter author = getCounterFromMessage(message);
        author.contractInfo(message);
    }

    private void removeContract(Message message) {
        String guildId = message.getGuildId().get().asString();
        Counter author = getCounterFromMessage(message);
        if (author.getContracts().isEmpty() && author.getIncomingContracts().isEmpty()) {
            CountingBot.write(message, "You don't have any contracts.");
            return;
        }
        if (message.getContent().split(" ").length != 2 && message.getContent().split(" ").length != 3) {
            CountingBot.write(message, "Usage: ~removecontract [@OtherCounter] _[in case of multiple contracts with that person: contract number]_");
            contractInfo(message);
            return;
        }
        int contractNumber = -1;
        if (message.getContent().split(" ").length == 3) {
            if (!Util.isNumber(message.getContent().split(" ")[2]) || Integer.parseInt(message.getContent().split(" ")[2]) < 0) {
                CountingBot.write(message, "Please provide an integer number greater or equal to 0 to specify the contract.");
                return;
            }
            contractNumber = Integer.parseInt(message.getContent().split(" ")[2]);
        }
        String otherCounterId = Util.pingToUserId(message.getContent().split(" ")[1]);
        if (author.getId().equals(otherCounterId)) {
            CountingBot.write(message, "You can't remove a contract with yourself.");
            return;
        }

        List<Contract> contractsMatchingToGivenId = author.streamIncomingAndOutgoingContracts().filter(
                contract -> contract.toId.equals(otherCounterId) || (contract.owner != null && contract.owner.getId().equals(otherCounterId))).collect(Collectors.toList());
        if (contractsMatchingToGivenId.isEmpty()) {
            CountingBot.write(message, "You don't have any contracts with this person.");
        } else if (contractsMatchingToGivenId.size() > 1) {
            if (contractNumber == -1) {
                StringBuilder content = new StringBuilder("Please specify the contract you want to remove by providing the corresponding number after the user ping. " +
                        "You have the following contracts with this person:");
                for (int i = 0; i < contractsMatchingToGivenId.size(); i++) {
                    content.append("\n").append(i).append("): ").append(contractsMatchingToGivenId.get(i).toString());
                }
                CountingBot.write(message, content.toString());
            } else if (contractNumber >= contractsMatchingToGivenId.size()) {
                CountingBot.write(message, "This number does not match a contract.");
            } else {
                initiateRemoveContractButtonInteraction(message, author, this.getCounter(guildId, otherCounterId), contractsMatchingToGivenId.get(contractNumber));
            }
        } else {
            Contract contract = contractsMatchingToGivenId.get(0);
            initiateRemoveContractButtonInteraction(message, author, this.getCounter(guildId, otherCounterId), contract);
        }

    }

    private void initiateRemoveContractButtonInteraction(Message message, Counter author, Counter requestedUser, Contract contractToRemove) {
        String requestedUserId = requestedUser.getId();
        String content = Util.userIdToPing(requestedUserId) + " do you accept to remove the following contract with " + author.getId() + "?\n"
                + contractToRemove;
        Button acceptButton = Button.success(Contract.ACCEPT_REMOVE_CONTRACT_PREFIX + requestedUserId + ":" +  contractToRemove.getContractId(), "Accept");
        Button declineButton = Button.danger(Contract.DECLINE_REMOVE_CONTRACT_PREFIX + requestedUserId + ":" + contractToRemove.getContractId(), "Decline");
        MessageCreateSpec spec = MessageCreateSpec.builder().addComponent(ActionRow.of(List.of(declineButton, acceptButton))).build().withContent(content);
        message.getChannel().block().createMessage(spec).subscribe();
    }

    private void tradeOffer(Message message) {
        String content = message.getContent().toUpperCase();
        Counter author = getCounterFromMessage(message);
        String guildId = message.getGuildId().get().asString();
        if (TradeOfferChecker.isValid(message.getContent(), message)) {
            TradeOffer tradeOffer = TradeHandler.parse(content, author);
            if (tradeOffer.getRequestedUser() == null) {
                CountingBot.write(message, "This user doesn't seem to have ever counted!");
                return;
            }
            if (tradeOffer.getRequestedUser().getId().equals(author.getId())) {
                CountingBot.write(message, "You can't trade with yourself!");
                return;
            }
            Counter requested = this.getCounter(guildId, tradeOffer.getRequestedUserId());
            if (!tradeOffer.isTradeOfferValid(message)) {
                return;
            }
            if (requested == null) {
                CountingBot.write(message, "Something went wrong, please try again.");
                System.err.println("Error: Requested counter in trade offer is null although it should exist.");
                return;
            }
            String tradeId = getNextTradeId();
            Button acceptButton = Button.success(tradeId, "Accept");
            Button declineButton = Button.danger("-" + tradeId, "Decline");
            String cont = tradeOffer.getUserPing() + " do you accept the trade offer?";
            MessageCreateSpec spec = MessageCreateSpec.builder().addComponent(ActionRow.of(List.of(declineButton, acceptButton))).build().withContent(cont);
            message.getChannel().block().createMessage(spec).subscribe();

            requested.addTradeOffer(tradeOffer, tradeId);

        }

    }

    public void handBagBought(Message message) {
        bankCommandHandler.handBagBought(message);
    }

    private void baseInfo(Message message) {
        String channelId = message.getChannelId().asString();
        if (streaks.containsKey(channelId)) {
            write(message, streaks.get(channelId).getBaseInfoRespond(0));
        } else {
            write(message, "There is no selected base yet.");
        }
    }

    private void prestige(Message message) {
        Counter author = getCounterFromMessage(message);
        if (author.prestige(message)) {
            if (author.getPrestiges() < 2) {
                CountingBot.write(message, "GG WP, you just prestiged! You get:\n "
                        + "- 1 prestige point for buying new bases (purchasable with the other ~unlock unlocks) and items in the shop!\n "
                        + "- a global boost of " + Math.round(Counter.MULT_PLUS_PER_PRESTIGE * 100.0d) + "% for counting");
            } else {
                CountingBot.write(message, "GG WP, you prestiged again! You get:\n "
                        + "- 1 additional prestige point\n "
                        + "- an upgrade to your global boost (" + Math.round((author.getPrestiges() - 1) * Counter.MULT_PLUS_PER_PRESTIGE * 100.0d) + "% => " + Math.round((author.getPrestiges()) * Counter.MULT_PLUS_PER_PRESTIGE * 100.0d) + "%)");
            }
        }
    }

    private void unlock(Message message) {
        Counter author = getCounterFromMessage(message);
        String[] splitted = message.getContent().split(" ");
        if (author.isUnlocked(Unlockable.UNLOCK_COMMAND)) {
            if (splitted.length != 2 && !(splitted.length == 3 && "base".equals(splitted[1]))) {
                this.unlockInfo(message, author);
                return;
            }
            String toUnlock = splitted[1];
            if (toUnlock.equals("base") && splitted.length == 3) {
                author.unlockBase(message, splitted[2]);
                return;
            }
            if (toUnlock.equals(Unlockable.RULE_COST_UPGRADE_1.getName())) {
                author.unlockRuleCostUpgrade(message);
                return;
            }
            for (int i = 0; i < Unlockable.values().length; i++) {
                Unlockable currUnlockable = Unlockable.values()[i];

                if (currUnlockable.getName().equals(toUnlock)) {
                    author.unlock(currUnlockable, message);
                    return;
                }
            }

            CountingBot.write(message, "Error: Invalid unlock!");
        } else {
            if (message.getContent().contains(" ")) {
                this.unlockInfo(message, author);
                return;
            }
            if (author.canAfford(Unlockable.UNLOCK_COMMAND.getPrice())) {
                author.unlock(Unlockable.UNLOCK_COMMAND, message);
            } else {
                this.unlockInfo(message, author);
            }
        }
        CountingBot.getInstance().save();
    }

    private void unlockInfo(Message message, Counter author) {
        if (author.isUnlocked(Unlockable.UNLOCK_COMMAND)) {
            String answ = "Unlock new stuff with the '~unlock' command!\nUsage: ~unlock [unlock name]\n\nYet to unlock (You have " + author.getScore() + " money):";
            boolean anyUnlockable = false;
            int currCount = 1;
            boolean ruleCostUpgradeAlreadyDisplayed = false;
            for (int i = 0; i < Unlockable.values().length; i++) {
                Unlockable currUnlockable = Unlockable.values()[i];
                if (i >= Unlockable.BASE_1.ordinal()) {
                    if (author.getPrestiges() == 0) {
                        continue;
                    }
                }
                if (currUnlockable == Unlockable.BASE_N) {
                    answ += "\n" + String.valueOf(currCount) + ".  '" + currUnlockable.getName() + "': " + currUnlockable.getDescription();
                    answ += " (" + Math.abs(currUnlockable.getPrice()) + " prestige point(s))";
                    anyUnlockable = true;
                } else if (!author.isUnlocked(currUnlockable)) {
                    if (currUnlockable.getName().equals(Unlockable.RULE_COST_UPGRADE_1.getName()) && ruleCostUpgradeAlreadyDisplayed) {
                        continue;
                    } else if (currUnlockable.getName().equals(Unlockable.RULE_COST_UPGRADE_1.getName())) {
                        ruleCostUpgradeAlreadyDisplayed = true;
                    }
                    answ += "\n" + String.valueOf(currCount) + ".  '" + currUnlockable.getName() + "': " + currUnlockable.getDescription();
                    currCount++;
                    if (currUnlockable.getPrice() > 0) {
                        answ += " (" + currUnlockable.getPrice() + " money)";
                    } else {
                        answ += " (" + Math.abs(currUnlockable.getPrice()) + " prestige point(s))";
                    }
                    anyUnlockable = true;
                }

            }
            if (anyUnlockable) {
                CountingBot.write(message, answ);
            } else {
                CountingBot.write(message, "You already own everything!");
            }
        } else {
            CountingBot.write(message, "Unlock the unlock command in order to unlock rules. You have " + (author.getScore()) + " out of the needed " + Unlockable.UNLOCK_COMMAND.getPrice() + ".");
        }
    }

    private void addRule(Message message) {
        String channelID = message.getChannelId().asString();
        if (streaks.containsKey(channelID)) {
            String content = message.getContent();
            streaks.get(channelID).addRule(message, getUserIdFromDiscordUserObject(message.getAuthor().get()));
        } else {
            write(message, "You have to start a streak before you can add rules.");
        }
    }

    private void rules(Message message) {
        String channelId = message.getChannelId().asString();
        if (streaks.containsKey(channelId)) {
            write(message, streaks.get(channelId).getRulesRespond());
        } else {
            write(message, "No rules!");
        }
    }

    private String scoreboard(Message message, String mode) {
        String guildId = message.getGuildId().get().asString();
        ArrayList<Counter> countersSorted = new ArrayList<>(guilds.get(guildId).getCounters().values());

        countersSorted.sort((a, b) -> {
            long scoreA = a.getPrestiges() * 1000000L + (mode.equals("networth") ? a.getAccWorth() : a.getPossibleTotal());
            long scoreB = b.getPrestiges() * 1000000L + (mode.equals("networth") ? b.getAccWorth() : b.getPossibleTotal());
            return Long.compare(scoreB, scoreA);
        });
        String ret = "Scoreboard: ";
        Bank bank = guilds.get(guildId).getBank();
        String bankString = "The CrocBank Inc. \uD83D\uDC0A: " + bank.getTotalScore() + " money";
        boolean bankDisplayed = !bank.isUnlocked(); // Dont show bank if not unlocked

        int position = 1;
        for (Counter counter : countersSorted) {
            if (counter.getPrestiges() == 0 && counter.getPossibleTotal() == 0) {
                continue;
            }

            long bankCompareValue = counter.getPrestiges() * 1_000_000L + (mode.equals("networth") ? counter.getAccWorth() : counter.getPossibleTotal());

            if (!bankDisplayed && (bank.getTotalScore() > bankCompareValue)) {
                ret += "\n" + position + ") " +  bankString;
                bankDisplayed = true;
                position += 1;
            }
            ret += "\n" + position + ") " + counter.getName() + ": ";
            if (mode.equals("bal")) {
                ret += counter.getPossibleTotal() + " money";
            } else if (mode.equals("networth")) {
                ret += counter.getAccWorth() + " net worth";
            }
            if (counter.getPrestiges() != 0) {
                ret += " (Amount of Prestiges: " + counter.getPrestiges() + ")";
            }
            position += 1;
        }
        if (!bankDisplayed) {
            ret += "\n" + position + ") " + bankString;
        }
        return ret;
    }

    private void count(Message message) {
        Runnable streakDeleteRunnable = () -> {
            String channelId = message.getChannelId().asString();
            disposeStreak(channelId);
        };
        User user = message.getAuthor().get();
        String channelKey = message.getChannelId().asString();
        String content = message.getContent();
        boolean deleteStreak = false;
        Counter author = getCounterFromMessage(message);
        String guildId = message.getGuildId().get().asString();
        if (streaks.containsKey(channelKey)) {
            CountingStreak streak = streaks.get(channelKey);
            checkIllegalCharacters(message, content, streak);
            if ((!BaseSystems.isNumInSystem(content, streak.getBase()))) {
                return;
            }
            deleteStreak = !streak.count(message, author, content, streakDeleteRunnable);
        } else {
            String[] splitted = content.split(" ");
            if (content.equals("1") || (splitted[0].equals("1") && splitted.length == 3 && splitted[1].equals("base") && Util.isNumber(splitted[2]))) {
                if (content.equals("1")) {
                    streaks.put(channelKey, new CountingStreak(channelKey, 10, guildId));
                } else {
                    int base = Integer.parseInt(splitted[2]);
                    if (author != null && author.isBaseUnlocked(base)) {
                        streaks.put(channelKey, new CountingStreak(channelKey, base, guildId));
                    } else {
                        CountingBot.write(message, "Unlock this base with prestige-points to start a streak.");
                        return;
                    }
                }
                deleteStreak = !streaks.get(channelKey).count(message, author, splitted[0], streakDeleteRunnable);
            }
        }
        if (deleteStreak) {
            streakDeleteRunnable.run();
        }

    }

    private void checkIllegalCharacters(Message message, String content, CountingStreak streak) {
        int amountOfNeitherDangerousCharactersNorNumbers = 0;
        int amountOfDangerousCharacters = 0;
        int amountOfNumbers = 0;
        for (char c : content.toCharArray()) {
            if (BaseSystems.isNumInSystem(String.valueOf(c), streak.getBase())) {
                amountOfNumbers++;
                continue;
            }
            if (c == 'O') {
                amountOfDangerousCharacters++;
                continue;
            }
            int cInt = (int) c;
            if (cInt < 33 || (cInt > 126 && cInt < 161) || cInt == 173 || cInt > 191) {
                amountOfDangerousCharacters++;
                continue;
            }
            amountOfNeitherDangerousCharactersNorNumbers++;
        }
        if (amountOfNumbers != 0 && amountOfNeitherDangerousCharactersNorNumbers == 0 && amountOfDangerousCharacters > 0) {
            message.addReaction(CountingEmojis.WARNING).subscribe();
            streak.getTrophyHandler().considerSpawningIllegalCharacterTrophy(message, streak.getLastNum());
        }
    }

    public void disposeStreak(String streakId) {
        if (streaks.containsKey(streakId)) {
            streaks.get(streakId).dispose();
        }
        streaks.remove(streakId);
    }

    public Optional<CountingStreak> getStreak(String channelId) {
        return Optional.ofNullable(streaks.get(channelId));
    }

    public void save() {
        storage.save();
    }

    public void saveCountersAndStreaks() {
        storage.saveStreaks();
        storage.save();
    }

    public static void write(Message message, String s, Consumer<? super Message> onMessageSent) {
        message.getChannel().block().createMessage(s).subscribe(onMessageSent);
    }

    public static Message writeBlocking(Message message, String s) {
        return message.getChannel().block().createMessage(s).block();
    }

    public static void write(Message message, String s) {
        write(message, s, (msg) -> {
        });
    }
    public static void respond(Message msg, String s) {
        msg.getChannel().block().createMessage(s).withMessageReference(msg.getId()).subscribe();
    }

    // implement me!
    public static void reactWithBlockPrevention(Message message, ReactionEmoji emoji, String emojiDisplayName) {
        message.addReaction(emoji)
                .onErrorResume(discord4j.rest.http.client.ClientException.class, error -> {
                    if (error.getStatus().code() == 403) {
                        write(message, "The bot tried to react with " + emojiDisplayName + ", but it appears to have been blocked." +
                                "Please unblock the bot to ensure a smooth experience for everyone.");
                        return reactor.core.publisher.Mono.empty();
                    }
                    return reactor.core.publisher.Mono.error(error);
                })
                .subscribe();
    }

    private void scoreInfo(Message message) {
        Counter counter = getCounterFromMessage(message);
        BankAccount potentialAccount = guilds.get(message.getGuildId().get().asString()).getBank().getAccount(message.getAuthor().get().getId().asString());
        String msg = "Your current score is " + (counter.getPossibleTotal() + (potentialAccount == null ? 0 : potentialAccount.getBalance())) + " money " +
                "(" + counter.getScore() + " in your purse + " + counter.getCurrentScoreAdd() + " possible from current streaks";
        if (!(potentialAccount == null) && !(potentialAccount.getBalance() == 0)) {
            msg += " + " + potentialAccount.getBalance() + " in your CrocBank:tm: account";
        }
        msg += ").";
        if (counter.getPrestiges() != 0) {
            msg += "\nYou have " + counter.getPrestigePoints() + " prestige points.";
        }
        CountingBot.write(message, msg);
    }

    private void netWorthInfo(Message message) throws BankTransactionException {
        Counter counter = getCounterFromMessage(message);
        String counterId = counter.getId();
        String guildId = message.getGuildId().get().asString();
        Bank bank = guilds.get(guildId).getBank();
        int netWorth = counter.getAccWorth();
        String netWorthOutput = "Your account's total net worth, including your score and all upgrades bought:\n";

        String formatting = getString(netWorth);
        netWorthOutput += formatting.replace("{0}", String.valueOf(netWorth));
        netWorthOutput += "\nThis is made up of " + counter.getScore() + " money in your purse and " + (netWorth - counter.getScore()) + " value from upgrades.";
        if (counter.getPrestiges() > 0) netWorthOutput += "\n\nAdditionally, you have " + counter.getPrestigePoints() + " prestige points.";
        if (bank.isUnlocked() && bank.alreadyRegistered(counterId)) {
            netWorthOutput += "\n\nYou have " + bank.getBalance(counterId) + " money in your CrocBank:tm: account, and you spent " + bank.getAccount(counterId).getTotalSpentOnUpgrades() + " money on bank upgrades.";
        }
        CountingBot.write(message, netWorthOutput);
    }

    @NotNull
    private static String getString(int netWorth) {
        int digits = (netWorth == 0) ? 0 : (int) Math.log10(netWorth) + 1;
        String formatting;
        switch(digits) {
            case 0:
                formatting = "-# {0}";
                break;
            case 4:
                formatting = "*{0}*";
                break;
            case 5:
                formatting = "**{0}**";
                break;
            case 6:
                formatting = "***{0}***";
                break;
            case 7:
                formatting = "## {0}";
                break;
            default:
                formatting = "{0}";
                break;
        }
        if (digits > 7) { formatting = "# {0}"; }
        return formatting;
    }

    public boolean isCounter(String guildId, String counterId) {
        if (!guilds.containsKey(guildId)) {
            return false;
        }
        return guilds.get(guildId).hasCounter(counterId);
    }

    private void addGuildOrCounterIfNotYetRegistered(Message message) {
        User user = message.getAuthor().get();
        String guildId = message.getGuildId().get().asString();
        String key = getUserIdFromDiscordUserObject(user);
        boolean shouldSave = false;
        if (!guilds.containsKey(guildId)) {
            guilds.put(guildId, new CountingGuild(guildId));
            shouldSave = true;
        }
        if (!guilds.get(guildId).hasCounter(key)) {
            guilds.get(guildId).addNewCounter(key, user.getUsername());
            shouldSave = true;
        }
        if (shouldSave) {
            storage.save();
        }

    }

    public Disposable subscribeEmojiReactHandler(EmojiReactHandler handler, String channelId) {
        return client.on(ReactionAddEvent.class).filter(
                        (reactionEvent) -> reactionEvent.getChannelId().asString().equals(channelId))
                .doOnNext(handler)
                .subscribe();
    }

    public void requestHandBagRefundViaItem(Message message) {
        String chanelId = message.getChannelId().asString();
        EmojiReactHandler emojiHandler = new EmojiReactHandler(chanelId, true);
        Disposable disposableSubscription = subscribeEmojiReactHandler(emojiHandler, chanelId);
        emojiHandler.activateWithSingleUseMode(disposableSubscription);
        TrophyHandler trophyHandler = new TrophyHandler(emojiHandler);
        bankCommandHandler.handBagRefundRequestedViaItemUse(message, trophyHandler);
    }

    public UserAnswerHandler getUserAnswerHandler() {
        return userAnswerHandler;
    }

    public void subscribeSingleUseSingleMessageEmojiReactHandlerAndActivate(EmojiReactHandler handler, String messageId) {
        Disposable subscription = client.on(ReactionAddEvent.class)
                .filter(event -> event.getMessage().block().getId().asString().equals(messageId))
                .doOnNext(handler)
                .subscribe();
        handler.activateWithSingleUseMode(subscription);
    }

    private Counter getCounterFromMessage(Message message) {
        String guildId = message.getGuildId().get().asString();
        String userId = getUserIdFromDiscordUserObject(message.getAuthor().get());
        return guilds.get(guildId).getCounter(userId);
    }

    private String getUserIdFromDiscordUserObject(User user) {
        return user.getId().asString();
    }

    public static Counter getCounter(String guildId, String id) {
        return getInstance().guilds.get(guildId).getCounter(id);
    }

    public static CountingBot getInstance() {
        return instance;
    }

    public static synchronized String getNextTradeId() {
        currId += 1;
        return String.valueOf(currId);
    }

    public HashMap<String, CountingGuild> getGuilds() {
        return guilds;
    }

    public BankCommandHandler getBankCommandHandler() {
        return bankCommandHandler;
    }

    public ShopCommandHandler getShopCommandHandler() {
        return shopCommandHandler;
    }

    public UserAnswerHandler getUserDMHandler() {
        return userDMHandler;
    }

    public Storage getStorage() {
        return storage;
    }

    public PrimeCoinSeller getPrimeCoinSeller() {
        return primeCoinSeller;
    }

    public SlashCommandHandler getSlashCommandHandler() {
        return slashCommandHandler;
    }

    public void registerMessageHandler(MessageHandler messageHandler) {
        client.on(MessageCreateEvent.class).subscribe(messageHandler);
    }
}
