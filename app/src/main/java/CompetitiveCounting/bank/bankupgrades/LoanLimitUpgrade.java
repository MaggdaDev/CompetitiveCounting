package CompetitiveCounting.bank.bankupgrades;

public class LoanLimitUpgrade extends BankUpgrade{

    private final static String UPGRADE_ID = "LOAN_LIMIT";
    private final static String UNLOCK_NAME = "loan_limit";
    private final static int[] PRICES = {0, 50000, 200000, 1000000};
    private final static int[] LOAN_LIMIT_VALUES = {100000, 250000, 500000, 1000000};
    private final static String[] NAMES = {"Miserable Loan Limit", "Big Loan Limit Upgrade", "Huge Loan Limit Upgrade", "Massive Loan Limit Upgrade"};
    private final static String DESCRIPTION = "Allows me to give out bigger loans! Perfect for those with big dreams...";
    protected LoanLimitUpgrade() {
        super(UPGRADE_ID);
    }

    public int[] getLoanLimitValues() {
        return LOAN_LIMIT_VALUES;
    }

    @Override
    String getUnlockName() {
        return UNLOCK_NAME;
    }

    @Override
    String[] getNames() {
        return NAMES;
    }

    @Override
    int[] getPrices() {
        return PRICES;
    }

    @Override
    String getDescription() {
        return DESCRIPTION;
    }

    public int getCurrentValue() {
        return LOAN_LIMIT_VALUES[currentLvl];
    }
}
