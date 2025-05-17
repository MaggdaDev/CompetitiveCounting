package CompetitiveCounting.bank.exceptions;

public class NotEnoughMoneyException extends Exception{

    public enum MoneyOwner {
        COUNTER,
        ACCOUNT,
        BANK
    }
    public final int moneyNeeded;
    public final int moneyAvailable;

    public final MoneyOwner owner;

    public NotEnoughMoneyException(int moneyNeeded, int moneyAvailable, MoneyOwner owner) {
        this.moneyNeeded = moneyNeeded;
        this.moneyAvailable = moneyAvailable;
        this.owner = owner;
    }
}
