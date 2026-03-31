package CompetitiveCounting.bank.bankupgrades;

public class DepositLimitUpgrade extends BankUpgrade{

    private final static int[] PRICES = {10000, 100000};
    private final static String[] NAMES = {"Huge Deposit Limit Upgrade", "Massive Deposit Limit Upgrade"};
    private final static String DESCRIPTION = "Allows me to hoard more money! You should definitely buy this one.";
    protected DepositLimitUpgrade() {
        super("deposit_limit", NAMES, PRICES, DESCRIPTION);
    }


}
