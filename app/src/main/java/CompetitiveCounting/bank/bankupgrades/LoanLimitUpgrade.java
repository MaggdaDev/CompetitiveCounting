package CompetitiveCounting.bank.bankupgrades;

public class LoanLimitUpgrade extends BankUpgrade {
    public final static LoanLimitUpgrade EMPTY = new LoanLimitUpgrade();
    private final static String UPGRADE_ID = "LOAN_LIMIT";
    private final static String UNLOCK_NAME = "loan_limit";
    private final static int[] PRICES = {0, 50000, 200000, 1000000};
    private final static int[] LOAN_LIMIT_VALUES = {100000, 250000, 500000, 1000000};
    private final static String[] NAMES = {"Miserable Loan Limit", "Big Loan Limit Upgrade", "Huge Loan Limit Upgrade", "Massive Loan Limit Upgrade"};
    private final static String DESCRIPTION = "Allows me to give out bigger loans! Perfect for those with big dreams...";
    private final static String ADVERTISEMENT = "This increases the maximum amount of money for a single loan from {0} to {1}.";
    private final static String BUY_FEEDBACK = "Now you can take loans of up to {0} instead of {1}!";
    private final static String UNIT = " money";
    protected LoanLimitUpgrade() {
        super(UPGRADE_ID);
    }

    public int[] getLoanLimitValues() {
        return LOAN_LIMIT_VALUES;
    }

    @Override
    public String getBoughtFeedback() {
        if (currentLvl == 0) {
            return "";  // Should be called AFTER upgrade is bought
        }
        return BUY_FEEDBACK.replace("{0}", getCurrentValue() + UNIT).replace("{1}", LOAN_LIMIT_VALUES[currentLvl-1] + UNIT);
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

    @Override
    public int getCurrentValue() {
        return LOAN_LIMIT_VALUES[currentLvl];
    }

    @Override
    public int getNextValue() {
        if (isMaxedOut()) {
            return -1;
        }
        return LOAN_LIMIT_VALUES[currentLvl+1];
    }

    @Override
    public String getAdvertisement() { return ADVERTISEMENT; }

    @Override
    public String getUnit() {
        return UNIT;
    }
}
