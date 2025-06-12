package CompetitiveCounting.bank;

import CompetitiveCounting.bank.exceptions.BankTransactionException;

import java.util.HashMap;
import java.util.List;

/**
 * TODO: gucci handbag zum öffnen
 * TODO: ~shop command
 */

public class Bank {
    private boolean isUnlocked = false;
    public final static int DEPOSIT_COST = 1000;
    private String guildId;
    private int totalScore;
    private HashMap<String, BankAccount> accounts;

    private static final List<String> UPGRADE_KEYS = List.of("loan_formula", "max_loan", "max_balance", "placeholder_2");
    private HashMap<String, Integer> upgrades;

    public Bank(String guildId) {
        this.guildId = guildId;
        this.totalScore = 0;
        this.accounts = new HashMap<>();
        this.upgrades = new HashMap<>();
        for (String key: UPGRADE_KEYS) {
            this.upgrades.put(key, 0);
        }
    }

    public void donate(int amount) {
        totalScore += amount;
    }

    public void removeMoney(int amount) {
        // should ideally only be used from within the loan function. removes money because it gives it to the user.
        // i'm not using withdraw since that would subtract from the user's BankAccount
        totalScore -= amount;
    }

    public void register(String counterId) {
        if (!alreadyRegistered(counterId)) {
            BankAccount account = new BankAccount(counterId);
            accounts.put(counterId, account);
        }
    }

    public boolean alreadyRegistered(String counterId) {
        return accounts.containsKey(counterId);
    }

    public void deposit(String counterId, int amount) throws BankTransactionException {
        totalScore += amount;
        accounts.get(counterId).deposit(amount - DEPOSIT_COST);
    }

    public int getTotalScore() { return this.totalScore; }

    public int getBalance(String counterId) throws BankTransactionException {
        return accounts.get(counterId).getBalance();
    }

    public void withdraw(String counterId, int withdrawAmount) {
        accounts.get(counterId).withdraw(withdrawAmount);
        totalScore -= withdrawAmount;
    }

    public void getUpgrades() {
        System.out.println(upgrades);
    }


    public void unlock() {
        isUnlocked = true;
    }
    public boolean isUnlocked() {
        return isUnlocked;
    }
}