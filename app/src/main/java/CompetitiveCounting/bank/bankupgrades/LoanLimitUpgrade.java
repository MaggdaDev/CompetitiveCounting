package CompetitiveCounting.bank.bankupgrades;

public class LoanLimitUpgrade extends BankUpgrade{

    private final static int[] PRICES = {50000, 200000, 1000000};
    private final static String[] NAMES = {"Big Loan Limit Upgrade", "Huge Loan Limit Upgrade", "Massive Loan Limit Upgrade"};
    private final static String DESCRIPTION = "Allows me to give out bigger loans! Perfect for those with big dreams...";
    protected LoanLimitUpgrade() {
        super("loan_limit", NAMES, PRICES, DESCRIPTION);
    }
}
