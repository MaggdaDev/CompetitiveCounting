package competitivecounting.bank.bankupgrades;

public class LoanRateUpgrade extends BankUpgrade{

    public static final LoanRateUpgrade EMPTY = new LoanRateUpgrade();
    private final static String UPGRADE_ID = "LOAN_RATE";
    private final static String UNLOCK_NAME = "loan_rate";
    private final static int[] PRICES = {0, 100000, 250000, 500000, 1000000};
    private final static double[] LOAN_RATE_VALUES = {100.0, 90.0, 80.0, 70.0, 60.0};
    private final static String[] NAMES = {"Outrageously High Loan Rate", "Better Loan Rate Upgrade", "Great Loan Rate Upgrade", "Fantastic Loan Rate Upgrade", "Mythic Loan Rate Upgrade"};
    private final static String DESCRIPTION = "Allows you to get better deals on your loans! Which means that you should take out more of them.";
    private final static String ADVERTISEMENT = "This upgrade multiplies interest rate on your loans with a modifier, which is lowered from {0} down to {1}.";
    private final static String ADVERTISEMENT_MAXED = "This upgrade multiplies interest rate on your loans with a modifier, which has been lowered down to {0}.";
    private final static String UNIT = "%";
    private final static String BUY_FEEDBACK = "The interest rate on your loans is now multiplied by {0} instead of {1}.";
    protected LoanRateUpgrade() {
        super(UPGRADE_ID);
    }
    private final static String BUY_RECOMMENDATION = "You can buy the *{0}* to increase this discount.";

    public double[] getLoanRateValues() {
        return LOAN_RATE_VALUES;
    }

    @Override
    public String getBoughtFeedback() {
        if (currentLvl == 0) {
            return "";  // Should be called AFTER upgrade is bought
        }
        return BUY_FEEDBACK.replace("{0}", getCurrentValue() + UNIT).replace("{1}", (int) LOAN_RATE_VALUES[currentLvl-1] + UNIT);
    }
    @Override
    public int getEmptyValue() {
        return EMPTY.getCurrentValue();
    }
    @Override
    protected String getBuyRecommendationString() {
        return BUY_RECOMMENDATION.replace("{0}", getNextName());
    }

    @Override
    String[] getNames() {
        return NAMES;
    }

    @Override
    public int[] getPrices() {
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
    public int getNextValue() {
        if (isMaxedOut()) {
            return -1;
        }
        return (int) LOAN_RATE_VALUES[currentLvl+1];
    }

    @Override
    public String getAdvertisement() { return ADVERTISEMENT; }

    @Override
    public String getAdvertisementMaxed() { return ADVERTISEMENT_MAXED; }

    @Override
    public String getUnit() {
        return UNIT;
    }
}
