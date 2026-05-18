package CompetitiveCounting.bank;

import CompetitiveCounting.*;
import CompetitiveCounting.bank.bankupgrades.BankUpgrade;
import CompetitiveCounting.bank.exceptions.*;
import CompetitiveCounting.dialogue.Dialogue;
import discord4j.core.object.entity.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BankCommandHandler {
    private final BankTransactionsHandler transactionsHandler;
    private TreeMap<Integer, String> donationMessages;

    private final HashMap<String, CountingGuild> guilds;

    public BankCommandHandler(BankTransactionsHandler transactionsHandler) {
        this.transactionsHandler = transactionsHandler;
        this.guilds = transactionsHandler.getGuilds();
        createMessageTreeMaps();
    }

    private void createMessageTreeMaps() {
        donationMessages = new TreeMap<>();
        donationMessages.put(0, "Single digit money is absolutely abysmal. You should be ashamed of yourself. This is the same as tipping the waitress 5 cents.");
        donationMessages.put(1, "Thirty dollars? What do I need twenty dollars for? Help! My own customer is donating 10 dollars!");
        donationMessages.put(2, "That's pocket change! Honestly...");
        donationMessages.put(3, "A little bit of money never killed nobody. Well, you have a little less now, not that it matters.");
        donationMessages.put(4, "That's an entire ~unlock command and some change! Wait a second, what if I started adding rules?");
        donationMessages.put(5, "You made the right decision to donate this much to me. Maybe it makes you feel a little bit better...");
        donationMessages.put(6, "Millions of dollars back to its rightful owner. You know, we were promised this money millennia ago!");
        donationMessages.put(9, "You cheated. You can't have a billion. You're a cheater!");
    }

    /**
     * Handles CrocBank messages/commands
     *
     * @param message
     * @return whether the JSON should be saved
     */
    public boolean handleBankCommand(Message message) {
        if (message.getGuildId().isEmpty()) {
            write(message, "This command can only be used in a server.");
        }
        String guildId = message.getGuildId().get().asString();
        if (!guilds.get(guildId).getBank().isUnlocked()) {
            write(message, "Nobody came to answer your request...");
            return false;
        }
        String authorId = message.getAuthor().get().getId().asString();
        String[] splitMessage = message.getContent().split(" ");
        if (splitMessage.length < 2 || !splitMessage[0].endsWith("bank")) {
            sendCommandNotUnderstoodMessage(message);
            return false;
        }
        String commandType = splitMessage[1].toLowerCase();
        if (List.of("donate", "withdraw", "deposit", "loan").contains(commandType) && splitMessage.length < 3) { // check if argument for money is present
            bankWrite(message, "You actually need to tell me how much money we're talking about!");
            return false;
        }
        Bank bank = guilds.get(guildId).getBank();
        if (!bank.alreadyRegistered(authorId)) {
            bank.register(authorId);
        }
        boolean shouldSaveJson = false;
        try {
            switch (commandType) {
                case "donate":
                    int donationAmount = parseStringToNaturalNumberAtIndex(splitMessage, 2, message);
                    transactionsHandler.donate(guildId, authorId, donationAmount);
                    sendDonationMessage(message, donationAmount);
                    shouldSaveJson = true;
                    break;
                case "balance":
                    int balance = bank.getBalance(authorId);
                    int roundedBalance = 100 * (balance / 100);
                    if (roundedBalance == 0) {
                        bankWrite(message, "I don't remember your balance... probably too small.");
                    } else {
                        bankWrite(message, "As long as there's enough money in my stash, you can get around " + roundedBalance + " money from me.");
                    }
                    break;
                case "withdraw":
                    int withdrawAmount = parseStringToNaturalNumberAtIndex(splitMessage, 2, message);
                    int newBalance = bank.getBalance(authorId) - withdrawAmount;
                    transactionsHandler.withdraw(guildId, authorId, withdrawAmount, message);
                    shouldSaveJson = true;
                    sendWithdrawMessage(message, withdrawAmount, newBalance);
                    break;
                case "deposit":
                    int depositAmount = parseStringToNaturalNumberAtIndex(splitMessage, 2, message);
                    int newBalance2 = bank.getBalance(authorId) + depositAmount - Bank.DEPOSIT_COST;
                    transactionsHandler.deposit(guildId, authorId, depositAmount);
                    shouldSaveJson = true;
                    sendDepositMessage(message, depositAmount, newBalance2);
                    break;
                case "loan":
                    if (splitMessage.length < 4) {
                        bankWrite(message, "You have to specify how much money you want to take out and the rate in % of paying that money back!\nExample: `~bank loan 1000000 50");
                        break;
                    }
                    int loanAmount = parseStringToNaturalNumberAtIndex(splitMessage, 2, message);
                    int loanRate = parseStringToNaturalNumberAtIndex(splitMessage, 3, message);
                    BankLoanHandler.giveLoan(guildId, authorId, loanAmount, loanRate, message);
                    shouldSaveJson = true; // Contract has been added successfully if this line is reached
                    break;
                case "flex":
                    shouldSaveJson = sendFlexMessage(message, bank);
                    break;
                case "help":
                    sendHelpMessage(message);
                    break;
                case "upgrades":
                    bankWrite(message, bank.getAccount(authorId).getUpgradesInfoString());
                    break;
                case "upgrade":
                    upgrade(message, splitMessage, bank, authorId);
                    shouldSaveJson = true;
                    break;
                default:
                    sendCommandNotUnderstoodMessage(message);
                    break;
            }
        } catch (BankTransactionException e) {
            bankWrite(message, "Begone! You broke something and I don't want to see my business ruined by you.");
        } catch (BankNumberArgumentException e) {
            if (e.getMessage().isEmpty()) {
                bankWrite(message, "Give me proper numbers or I can't help you.");
            } else {
                bankWrite(message, e.getMessage());
            }
        } catch (NotEnoughMoneyException e) {
            switch (e.owner) {
                case BANK:
                    bankWrite(message, "Well in theory I owe you that money... but I don't have it anymore. I only have " + e.moneyAvailable + " left. So I can't give it to you now. Maybe come back later?");
                    break;
                default:
                    bankWrite(message, "You need " + e.moneyNeeded + " money for that, but you only have " + e.moneyAvailable + ". I would recommend loaning " + (e.moneyNeeded - e.moneyAvailable + (int) (Math.random() * 1000.0) + " money from me!"));
            }
        } catch (BankLoanException | BankUpgradeException e) {
            bankWrite(message, e.getMessage());
        }
        return shouldSaveJson;
    }

    public void upgrade(Message message, String[] splitMessage, Bank bank, String authorId) throws BankUpgradeException {
        if(splitMessage.length < 3) {
            int amountBuyableUpgrades = bank.getAccount(authorId).getUpgrades().getAmountBuyableUpgrades();
            String s;
            if(amountBuyableUpgrades == 0) {
                bankWrite(message, "You have bought all available upgrades! If you donate, there might be more soon...");
                return;
            } else if (amountBuyableUpgrades == 1) {
                s = "You can buy the following upgrade (so close to maxed out!): \n";
            } else {
                s = "There are multiple upgrades that you can buy! \n";
            }
            s += bank.getAccount(authorId).getUpgradesBuyableString();
            bankWrite(message, s);
            return;
        }
        if(splitMessage.length > 3) {
            throw new BankUpgradeException("I cannot help you if you refrain from using the correct syntax. " +
                    "Please use '~bank upgrade <upgrade name>'! \nIf you forgot the names of our Croc Bank Inc.'s magnificent upgrades, " +
                    "you can use '~bank upgrade' without any arguments.");
        }
        BankAccount bankAccount = bank.getAccount(authorId);
        String unlockName = splitMessage[2];
        BankUpgrade upgrade = bankAccount.getUpgrades().parseUpgrade(unlockName);
        if (upgrade == null) {
            throw new BankUpgradeException("This upgrade is not registered with the CrocBank Inc.. Please make sure to " +
                    "only bother me when you have learned how to spell! How difficult can it be to just copy-paste from " +
                    "the list of upgrades? You can find it using '~bank upgrade' without any arguments.");
        }
        if (upgrade.isMaxedOut()) {
            throw new BankUpgradeException("This upgrade is already maxed out, your greed is spoken about in the bible.");
        }
        String oldLevel = upgrade.getCurrentName();
        String newLevel = upgrade.getNextName();
        upgrade.incrementLvl();
        bankWrite(message, "Congratulations! You have upgraded your deplorable '" + oldLevel + "' to a superior '" + newLevel + "'.");
        // todo add some sort of message about the benefits of the new upgrade level
    }

    public void handBagBought(Message message) {
        String guildId = message.getGuildId().get().asString();
        String authorId = message.getAuthor().get().getId().asString();
        CountingGuild countingGuild = guilds.get(guildId);
        Bank bank = countingGuild.getBank();
        if (bank.isUnlocked()) {  // todo
            bankWrite(message, "You bought another gucci purse. Are you some sort of collector or what's the motive behind your actions?");
            return;
        }
        Counter counter = countingGuild.getCounter(authorId);
        if (counter.getActiveUnlockBankDialog() != null) {
            counter.getActiveUnlockBankDialog().stop();
            counter.setActiveUnlockBankDialog(null);
        }
        Dialogue dialogue = createHandBagBoughtDialogue(message, guildId, authorId, bank);
        dialogue.play(message);
        counter.setActiveUnlockBankDialog(dialogue);
    }

    public void handBagRefundRequestedViaItemUse(Message message) { // todo: What happens if bank item is used after the bank is already unlocked?
        String guildId = message.getGuildId().get().asString();
        String authorId = message.getAuthor().get().getId().asString();
        CountingGuild countingGuild = guilds.get(guildId);
        Bank bank = countingGuild.getBank();
        if (bank.isUnlocked()) {
            bankWrite(message, "You cannot refund this item. But you can visit the CrocBank by writing ~bank!");
        } else {
            Dialogue dialogue = createHandBagBoughtDialogue(message, guildId, authorId, bank);
            dialogue.playAtIndex(message, 5);
        }
    }

    private Dialogue createHandBagBoughtDialogue(Message message, String guildId, String authorId, Bank bank) {
        return new Dialogue().addNpcLine(toCrocText("Oh look, finally, a customer!"), 2000)
                .addNpcLine(toCrocText("Me? I'm the Crocodile, and I'm the salesman selling those handbags! I am very grateful for your purchase."), 4000)
                .addNpcLine(toCrocText("By the way, I am obligated to inform you of the possibility to react with the :goblin: emoji to this message if you have any complaints... But now I'm off to my next customer, see ya!"), 0)
                .addRunnable((m) -> CountingBot.getInstance().getShopCommandHandler().acquireHandBag(message, guildId, authorId))
                .addWaitForEmojiReaction(CountingEmojis.GOBLIN, false)
                .addNpcLine(toCrocText("Fake? What do you mean fake? Everything about this leather is as real as it gets! Do you not trust me? I don't think a refund is appropriate."), 6000)
                .addNpcLine(toCrocText("However, it seems like this is a thriving spot to do business. Therefore, I will create a branch of my very own *CrocBank* here."), 2000)
                .addNpcLine(toCrocText("I will consider your generous, ahem, *donation* an investment! Consider this a great financial opportunity for the future!"), 0)
                .addRunnable(m -> bank.unlock())
                .addRunnable(m -> CountingBot.getInstance().save())
                .addNpcLine("You have unlocked the bank on *" + message.getGuild().block().getName() + "*! Every member can now use the ~bank commands. For more details, run ~bank help.", 0);
    }


    private int parseStringToNaturalNumberAtIndex(String[] splitMessage, int index, Message message) throws BankNumberArgumentException {
        int number = parseStringArgToIntAtIndex(splitMessage, index, message);
        if (number < 0) {
            throw new BankNumberArgumentException("You can't trick me with negative numbers!");
        } else if (number == 0) {
            throw new BankNumberArgumentException("Without moss, nothing goes (or so we say in my country).");
        }
        return number;
    }

    private int parseStringArgToIntAtIndex(String[] splitMessage, int index, Message message) throws BankNumberArgumentException {
        try {
            return Integer.parseInt(splitMessage[index]);
        } catch (NumberFormatException e) {
            throw new BankNumberArgumentException("");
        }
    }

    private void sendCommandNotUnderstoodMessage(Message message) {
        bankWrite(message, "I didn't get that. You probably meant ~bank donate 1000000");
    }

    private void sendDonationMessage(Message message, int donationAmount) {
        int magnitude = (int) Math.log10(donationAmount);
        Map.Entry<Integer, String> entry = donationMessages.floorEntry(magnitude);

        String response = entry != null ? entry.getValue() : "entry is null, you shit your pants";

        bankWrite(message, response);
    }

    private void sendDepositMessage(Message message, int depositAmount, int newBalance) {
        if (depositAmount < 1000) {
            bankWrite(message, "You are wasting my time! I won't lift a finger for that little money.");
        } else if (depositAmount == 1000) {
            bankWrite(message, "You have hit the sweet spot of depositing exactly zero money. Good job.");
        } else {
            bankWrite(message, "Thank you for " + (depositAmount - (Bank.DEPOSIT_COST / 2)) + " money. You now have " + (depositAmount - Bank.DEPOSIT_COST) + " more money on your account. Your balance is " + newBalance + ".");
        }

    }

    private void sendWithdrawMessage(Message message, int withdrawAmount, int newBalance) {
        bankWrite(message, "You have taken " + withdrawAmount + "from the bank. You better give it back! You now have " + newBalance + " left here.");
    }

    /**
     * @return whether the json should be saved after the call
     * @throws BankTransactionException
     */
    // NIGGER
    private boolean sendFlexMessage(Message message, Bank bank) throws BankTransactionException {
        String guildId = message.getGuildId().get().asString();
        String authorId = message.getAuthor().get().getId().asString();
        CountingGuild countingGuild = guilds.get(guildId);
        Counter counter = countingGuild.getCounter(authorId);
        int bankScore = bank.getTotalScore();
        int userBalance = counter.getScore();
        if (userBalance == 0) {
            bankWrite(message, "Do you even know what money is or why don't you have any?");
        } else {
            double ratio = Math.round(10.0 * (double) bankScore / (double) userBalance) / 10.0;
            if (bankScore == userBalance) {
                bankWrite(message, "What a coincidence! We both have exactly the same amount of money: " + bankScore + ".");
            } else if (ratio == 1.0) {
                bankWrite(message, "Wow, your wealth is comparable to mine!");
            } else if (ratio > 1.0) {
                bankWrite(message, "You are but a mere mortal to my vast riches. I have " + ratio + "x more money than you.");
            } else {
                ratio = Math.round(10.0 * (double) userBalance / (double) bankScore) / 10.0;
                int donationAmount = 1000;
                if (ratio > 100 && userBalance >= donationAmount) {
                    new Dialogue().addNpcLine(toCrocText("You have so much more money than me that you surely wouldn't notice if I just..."), 2000)
                            .addNpcLine("A contribution has been donated to the bank.", 0)
                            .play(message);
                    try {
                        transactionsHandler.donate(guildId, authorId, donationAmount);
                        return true;
                    } catch (NotEnoughMoneyException e) {
                        System.err.println("Something went wrong! Could not donate " + donationAmount + " money to the bank even though user has at least " + donationAmount + " money.");
                    }
                } else {
                    bankWrite(message, "This can't be! You have " + ratio + "x more money than me.");
                }
            }
        }

        return false;
    }

    private void sendHelpMessage(Message message) {
        String helpMessage = "## Terms of Service\n" +
                "\uD83D\uDC0A: We at the CrocBank Inc. are happy that you are considering this lucrative deal! Therefore I will gladly explain our Terms of Service™ to you.\n" +
                "\n" +
                "### ~bank donate\n" +
                "\uD83D\uDC0A: The most important command, donate your money to the bank for a greater cause!\n" +
                "### ~bank deposit\n" +
                "\uD83D\uDC0A: Deposit some of your (negligible) riches into your own personal bank account, keep it safe and profit from an astounding 0% interest rate! But at least nobody's gonna steal it...\n" +
                "### ~bank withdraw\n" +
                "\uD83D\uDC0A: You broke? You need your money back? Better think twice about it, it's safe and protected at the CrocBank after all!\n" +
                "### ~bank balance\n" +
                "\uD83D\uDC0A: Accurately displays your balance at the CrocBank with absolutely no margin of error. Trust us, we have your best interests at heart.\n" +
                "### ~bank loan\n" +
                "\uD83D\uDC0A: You can take out a loan from us too! With very small and fair interest rates, you can choose how much of your income you can dedicate to paying us back. [Syntax: `~bank loan <amount> <rate in %>`]\n" +
                "### ~bank flex\n" +
                "\uD83D\uDC0A: Lets us at the CrocBank show you just how deep our pockets are. Try it out!\n" +
                "### ~bank help\n" +
                "\uD83D\uDC0A: Should be obvious...";
        write(message, helpMessage);
    }

    public static String toCrocText(String text) {
        return "\uD83D\uDC0A: " + text;
    }

    public static void bankWrite(Message message, String text) {
        write(message, toCrocText(text));
    }

    private static void write(Message message, String text) {
        CountingBot.write(message, text);
    }


}
