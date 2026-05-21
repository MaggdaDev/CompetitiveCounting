package CompetitiveCounting.bank.bankupgrades;

public class DepositLimitUpgrade extends BankUpgrade{
    public final static DepositLimitUpgrade EMPTY = new DepositLimitUpgrade();
    private final static String UPGRADE_ID = "DEPOSIT_LIMIT";
    private final static String UNLOCK_NAME = "deposit_limit";
    private final static int[] PRICES = {0, 10000, 100000, 1000000};
    private final static int[] DEPOSIT_LIMIT_VALUES = {50000, 250000, 1000000, 5000000};
    private final static String[] NAMES = {"Miniscule Deposit Limit", "Huge Deposit Limit Upgrade", "Massive Deposit Limit Upgrade", "Humongous Deposit Limit Upgrade"};
    private final static String DESCRIPTION = "Allows me to hoard more money! You should definitely buy this one.";
    private final static String ADVERTISEMENT = "Raise your bank account's deposit limit from {0} to {1}.";
    private final static String UNIT = " money";
    private final static String BUY_FEEDBACK = "You can now deposit a total of {0} instead of {1}! ";
    private final static String BUY_RECOMMENDATION = "You can buy the *{0}* to increase this limit.";

    @Override
    public String getBoughtFeedback() {
        if (currentLvl == 0) {
            return "";  // Should be called AFTER upgrade is bought
        }
        return BUY_FEEDBACK.replace("{0}", getCurrentValue() + UNIT).replace("{1}", DEPOSIT_LIMIT_VALUES[currentLvl-1] + UNIT);
    }
    @Override
    public int getEmptyValue() {
        return EMPTY.getCurrentValue();
    }
    @Override
    protected String getBuyRecommendationString() {
        return BUY_RECOMMENDATION.replace("{0}", getNextName());
    }

    protected DepositLimitUpgrade() {
        super(UPGRADE_ID);
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
    public int[] getPrices() {
        return PRICES;
    }

    @Override
    String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public int getCurrentValue() {
        return DEPOSIT_LIMIT_VALUES[currentLvl];
    }

    @Override
    public int getNextValue() {
        if (isMaxedOut()) {
            return -1;
        }
        return DEPOSIT_LIMIT_VALUES[currentLvl + 1];
    }

    @Override
    public String getAdvertisement() {
        return ADVERTISEMENT;
    }

    @Override
    public String getUnit() {
        return UNIT;
    }
}
