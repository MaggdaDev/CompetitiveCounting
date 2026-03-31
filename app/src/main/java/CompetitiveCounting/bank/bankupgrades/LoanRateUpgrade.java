package CompetitiveCounting.bank.bankupgrades;

public class LoanRateUpgrade extends BankUpgrade{

    private final static int[] PRICES = {100000, 250000, 500000, 2000000};
    private final static String[] NAMES = {"Better Loan Rate Upgrade", "Great Loan Rate Upgrade", "Fantastic Loan Rate Upgrade", "Mythic Loan Rate Upgrade"};
    private final static String DESCRIPTION = "Allows you to get better deals on your loans! Which means that you should take out more of them.";
    protected LoanRateUpgrade() {
        super("loan_rate", NAMES, PRICES, DESCRIPTION);
    }
}
