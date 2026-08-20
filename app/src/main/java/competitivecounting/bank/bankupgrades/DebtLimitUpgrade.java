package competitivecounting.bank.bankupgrades;

public class DebtLimitUpgrade extends BankUpgrade{
    public final static DebtLimitUpgrade EMPTY = new DebtLimitUpgrade();

    private final static String UPGRADE_ID = "DEBT_LIMIT";
    private final static String UNLOCK_NAME = "debt_limit";
    private final static int[] PRICES = {0, 50000, 200000, 1000000};
    private final static int[] DEBT_LIMIT_VALUES = {150000, 350000, 750000, 1500000};
    private final static String[] NAMES = {"Awful Debt Limit", "Nice Debt Limit Upgrade", "Amazing Debt Limit Upgrade", "Phenomenal Debt Limit Upgrade"};
    private final static String DESCRIPTION = "Allows you to have more total debt! I hope you bought this first...";
    private final static String ADVERTISEMENT = "This upgrades your total debt limit across all of the bank's loans, increasing it from {0} to {1}.";
    private final static String UNIT = " money";
    private final static String BUY_FEEDBACK = "You can now have a total debt of {0} instead of {1}.";
    private final static String BUY_RECOMMENDATION = "You can raise this ceiling by buying the {0}.";
    protected DebtLimitUpgrade() {
        super(UPGRADE_ID);
    }

    @Override
    public String getBoughtFeedback() {
        if (currentLvl == 0) {
            return "";  // Should be called AFTER upgrade is bought
        }
        return BUY_FEEDBACK.replace("{0}", getCurrentValue() + UNIT).replace("{1}", DEBT_LIMIT_VALUES[currentLvl-1] + UNIT);
    }

    @Override
    public int getEmptyValue() {
        return EMPTY.getCurrentValue();
    }

    @Override
    protected String getBuyRecommendationString() {
        return BUY_RECOMMENDATION.replace("{0}", getNextName());
    }

    public int[] getDebtLimitValues() {
        return DEBT_LIMIT_VALUES;
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
        return DEBT_LIMIT_VALUES[currentLvl];
    }

    @Override
    public int getNextValue() {
        if (isMaxedOut()) {
            return -1;
        }
        return DEBT_LIMIT_VALUES[currentLvl+1];
    }

    @Override
    public String getAdvertisement() { return ADVERTISEMENT; }

    @Override
    public String getUnit() {
        return UNIT;
    }

}
