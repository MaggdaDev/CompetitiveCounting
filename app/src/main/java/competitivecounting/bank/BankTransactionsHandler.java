package competitivecounting.bank;

import competitivecounting.Counter;
import competitivecounting.CountingGuild;
import competitivecounting.bank.bankupgrades.DepositLimitUpgrade;
import competitivecounting.bank.exceptions.BankDepositException;
import competitivecounting.bank.exceptions.BankTransactionException;
import competitivecounting.bank.exceptions.NotEnoughMoneyException;
import discord4j.core.object.entity.Message;

import java.util.HashMap;

public class BankTransactionsHandler {
    private final HashMap<String, CountingGuild> guilds;

    public BankTransactionsHandler(HashMap<String, CountingGuild> guilds) {
        this.guilds = guilds;
    }

    public HashMap<String, CountingGuild> getGuilds() {
        return guilds;
    }

    public void donate(String guildId, String counterId, int amount) throws BankTransactionException, NotEnoughMoneyException {
        Counter counter = guilds.get(guildId).getCounter(counterId);
        Bank bank = guilds.get(guildId).getBank();
        if(bank == null || counter == null) {
            throw new BankTransactionException("Bank or Counter not found");
        }
        if(!counter.canAfford(amount)) {
            throw new NotEnoughMoneyException(amount, counter.getScore(), NotEnoughMoneyException.MoneyOwner.COUNTER);
        }
        counter.subtractScore(amount);
        bank.addMoney(amount);
    }

    public void deposit(String guildId, String authorId, int depositAmount) throws BankTransactionException, NotEnoughMoneyException {
        Counter counter = guilds.get(guildId).getCounter(authorId);
        Bank bank = guilds.get(guildId).getBank();
        if (counter == null || bank == null) {
            throw new BankTransactionException("Bank or Counter not found");
        }
        if (!counter.canAfford(depositAmount)) {
            throw new NotEnoughMoneyException(depositAmount, counter.getScore(), NotEnoughMoneyException.MoneyOwner.COUNTER);
        }
        if (depositAmount < 1000) {
            return; // Feedback is given by CommandHandler
        }
        DepositLimitUpgrade depositLimitUpgrade = bank.getAccount(authorId).getUpgrades().getDepositLimitUpgrade();
        int depositLimit = depositLimitUpgrade.getCurrentValue();
        int currentlyDepositedAmount = bank.getBalance(authorId);
        if (currentlyDepositedAmount > depositLimit) {
            int tooMuch = currentlyDepositedAmount - depositLimit;
            bank.withdraw(authorId, tooMuch);
            bank.addMoney(tooMuch);
            throw new BankDepositException("Woah! That's my fault. You seem to have more money in your account than is allowed.\n" +
                    "But don't worry, I have rounded that down for you. Your balance is now " + depositLimit + " money.");
        }
        if (depositAmount + currentlyDepositedAmount > depositLimit) {
            String startOfSentence = "No? This would increase your balance to " + (depositAmount + currentlyDepositedAmount) +
                    ", but your maximum deposit limit is only " + depositLimitUpgrade.getCurrentValueStringPotentiallyIndicatingEmptyValue()
                    + ".\n" + depositLimitUpgrade.getBuyRecommendationStringIfNotMaxedOut();
            throw new BankDepositException(startOfSentence);
        }
        counter.subtractScore(depositAmount);
        bank.deposit(authorId, depositAmount);
    }

    public void withdraw(String guildId, String counterId, int withdrawAmount, Message message) throws BankTransactionException, NotEnoughMoneyException {
        Counter counter = guilds.get(guildId).getCounter(counterId);
        Bank bank = guilds.get(guildId).getBank();
        if (counter == null || bank == null) {
            throw new BankTransactionException("Bank or Counter not found");
        }
        if (bank.getBalance(counterId) < withdrawAmount) {
            throw new NotEnoughMoneyException(withdrawAmount, bank.getBalance(counterId), NotEnoughMoneyException.MoneyOwner.ACCOUNT);
        }
        if (bank.getTotalScore() < withdrawAmount) {
            throw new NotEnoughMoneyException(withdrawAmount, bank.getTotalScore(), NotEnoughMoneyException.MoneyOwner.BANK);
        }
        bank.withdraw(counterId, withdrawAmount);
        counter.addBonusScore(withdrawAmount, message);
    }
}
