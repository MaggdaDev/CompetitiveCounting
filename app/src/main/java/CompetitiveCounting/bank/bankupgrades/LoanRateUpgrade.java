package CompetitiveCounting.bank.bankupgrades;

public class LoanRateUpgrade extends BankUpgrade{

    public static final LoanRateUpgrade EMPTY = new LoanRateUpgrade();
    private final static String UPGRADE_ID = "LOAN_RATE";
    private final static String UNLOCK_NAME = "loan_rate";
    private final static int[] PRICES = {0, 100000, 250000, 500000, 2000000};
    private final static double[] LOAN_RATE_VALUES = {100.0, 85.0, 75.0, 70.0, 60.0};
    private final static String[] NAMES = {"Outrageously High Loan Rate", "Better Loan Rate Upgrade", "Great Loan Rate Upgrade", "Fantastic Loan Rate Upgrade", "Mythic Loan Rate Upgrade"};
    private final static String DESCRIPTION = "Allows you to get better deals on your loans! Which means that you should take out more of them.";
    private final static String UNIT = "%";
    protected LoanRateUpgrade() {
        super(UPGRADE_ID);
    }

    public double[] getLoanRateValues() {
        return LOAN_RATE_VALUES;
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
    String getUnlockName() {
        return UNLOCK_NAME;
    }

    @Override
    public int getCurrentValue() {
        return (int) LOAN_RATE_VALUES[currentLvl];
    }

    @Override
    public String getUnit() {
        return UNIT;
    }
}
