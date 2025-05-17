package CompetitiveCounting.bank;

public class BankAccount {
    private String ownerId;
    private int balance;

    BankAccount(String ownerId) {
        this.ownerId = ownerId;
        this.balance = 0;
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
        if (money > 0) {
            balance += money;
        } else {
            throw new IllegalArgumentException("du spast");
        }
    }
}
