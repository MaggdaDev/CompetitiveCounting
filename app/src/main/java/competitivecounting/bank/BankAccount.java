package competitivecounting.bank;

import competitivecounting.Counter;
import competitivecounting.bank.bankupgrades.BankUpgrades;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BankAccount {
    private String ownerId;
    private int balance;
    private BankUpgrades bankUpgrades;
    private boolean monocleUnlocked = false;

    private int totalCrocStockProfit = 0;
    private transient Deque<String> recentStockProfits = new ArrayDeque<>();

    private final static DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    BankAccount(String ownerId) {
        this.ownerId = ownerId;
        this.balance = 0;
        init();
    }

    public void init() {
        if (bankUpgrades == null) {
            bankUpgrades = new BankUpgrades();
        }
        if (recentStockProfits == null) {
            recentStockProfits = new ArrayDeque<>();
        }
    }

    public String getRecentStockProfitsString() {
        if (recentStockProfits.isEmpty()) {
            return "*No recent transactions.*";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = recentStockProfits.descendingIterator();
        while (it.hasNext()) {
            String profit = it.next();
            sb.append("- ").append(profit).append("\n");
        }
        return sb.toString();
    }

    public void documentProfit(int forUser, int totalTransaction, @Nullable Counter source, String reason) {
        increaseCrocStockProfit(forUser);
        String timestamp = LocalDateTime.now().format(formatter);
        StringBuilder s = new StringBuilder("+")
                .append(forUser)
                .append(" money at ")
                .append(timestamp)
                .append(" (");
        if (source != null) {
            s.append(source.getName())
                    .append(" paid ");
        }
        s.append(totalTransaction)
                .append(" money for: ")
                .append(reason)
                .append(")");
        if (recentStockProfits.size() >= 10) {
            recentStockProfits.poll();
        }
        recentStockProfits.offer(s.toString());
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public int getBalance() {
        return balance;
    }

    public void withdraw(int money) {
        balance -= money;
    }


    public void depositWithoutFeeOrAffectingTotalBankScore(int money) {
        if (money >= 0) {
            balance += money;
        } else {
            throw new IllegalArgumentException("du spast");
        }
    }

    public String getUpgradesInfoString() {
        return bankUpgrades.toString();
    }

    public int getTotalSpentOnUpgrades() {
        return bankUpgrades.getTotalSpentOnUpgrades();
    }

    public String getUpgradesBuyableString() {
        return bankUpgrades.getBuyablesString();
    }

    public String getUpgradesMaxedOutString() {
        return bankUpgrades.getMaxedOutUpgradesString();
    }

    public BankUpgrades getUpgrades() {
        return bankUpgrades;
    }

    public boolean isMonocleUnlocked() {
        return monocleUnlocked;
    }

    public void setMonocleUnlocked(boolean monocleUnlocked) {
        this.monocleUnlocked = monocleUnlocked;
    }


    public int getTotalCrocStockProfit() {
        return totalCrocStockProfit;
    }

    public void increaseCrocStockProfit(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot increase total money from croc stocks by a negative amount");
        }
        totalCrocStockProfit += amount;
    }


}
