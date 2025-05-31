package CompetitiveCounting.bank;

import CompetitiveCounting.Counter;
import CompetitiveCounting.CountingBot;
import CompetitiveCounting.CountingGuild;
import CompetitiveCounting.bank.exceptions.BankNumberArgumentException;
import CompetitiveCounting.bank.exceptions.BankTransactionException;
import CompetitiveCounting.bank.exceptions.NotEnoughMoneyException;
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
                    int roundedBalance = 100 * ((int) balance / 100);
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
                        bankWrite(message, "You have to specify how much money you want to take out and the rate of paying that money back!\nExample: `~bank loan 1000000 50");
                        break;
                    }
                    int loanAmount = parseStringToNaturalNumberAtIndex(splitMessage, 2, message);
                    int loanRate = parseStringToNaturalNumberAtIndex(splitMessage, 3, message);
                    bankWrite(message, String.valueOf(calculateLoanInterestRate(loanAmount, loanRate)));
                    bankWrite(message, String.valueOf(calculateLoanInterest(loanAmount, loanRate)));
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
        }
        return shouldSaveJson;
    }

    public void handBagBought(Message message) {
        String guildId = message.getGuildId().get().asString();
        String authorId = message.getAuthor().get().getId().asString();
        CountingGuild countingGuild = guilds.get(guildId);
        Bank bank = countingGuild.getBank();
        if (bank.isUnlocked()) {
            bankWrite(message, "You bought another gucci purse. Are you some sort of collector or what's the motive behind your actions?");  // TODO
            return;
        }
        new Dialogue().addNpcLine(toCrocText("Oh look, finally, a customer!"), 2000)
                .addNpcLine(toCrocText("Me? I'm the Crocodile, and I'm the salesman selling those handbags! I am very grateful for your purchase."), 4000)
                .addNpcLine(toCrocText("By the way, I am obligated to inform you of the possibility to react with the :goblin: emoji if you have any complaints... But now I'm off to my next customer, see ya!"), 0)
                .addForAfterDialogue(() -> {
                    CountingBot.getInstance().getShopCommandHandler().acquireHandBag(message, guildId, authorId);
                })
                .play(message);

/*
        fakereveal: "Oops, someone must have switched out the crocodile leather for fake leather. Unfortunately, I can't give you any refunds."
        bankcreate: "I will however, out of the goodness of my heart, create a branch of my very own CrocBank here. I will consider your generous, ahem, donation, as an investment!"
        opportunity: "Consider this a great financial opportunity for the future!"*/

    }

    private int calculateLoanInterestRate(int money, int rate) {
        return (int) (Math.ceil(Math.sqrt(money) / 10) / (4 * rate) * 100);
    }

    private int calculateLoanInterest(int money, int rate) {
        return (money * (1 + calculateLoanInterestRate(money, rate)));
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

    private String toCrocText(String text) {
        return "\uD83D\uDC0A: " + text;
    }

    private void bankWrite(Message message, String text) {
        write(message, toCrocText(text));
    }

    private void write(Message message, String text) {
        CountingBot.write(message, text);
    }


}
