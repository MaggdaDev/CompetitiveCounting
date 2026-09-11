package competitivecounting.bank;

import competitivecounting.bank.bankupgrades.BankUpgrades;

public class BankAccount {
    private String ownerId;
    private int balance;

    private BankUpgrades bankUpgrades;

    private boolean monocleUnlocked = false;

    BankAccount(String ownerId) {
        this.ownerId = ownerId;
        this.balance = 0;
        init();
    }

    public void init() {
        if (bankUpgrades == null) {
            bankUpgrades = new BankUpgrades();
        }
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
}
