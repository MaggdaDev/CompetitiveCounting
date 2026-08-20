package competitivecounting.bank;

import competitivecounting.bank.bankupgrades.BankUpgrades;

public class BankAccount {
    private String ownerId;
    private int balance;

    private BankUpgrades bankUpgrades;

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



    public void deposit(int money) {
        if (money >= 0) {
            balance += money;
        } else {
            throw new IllegalArgumentException("du spast");
        }
    }

    public String getUpgradesInfoString() {
        return bankUpgrades.toString();
    }

    public String getUpgradesBuyableString() {
        return bankUpgrades.getBuyablesString();
    }

    public BankUpgrades getUpgrades() {
        return bankUpgrades;
    }


}
