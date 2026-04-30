package CompetitiveCounting.bank.bankupgrades;

public class DepositLimitUpgrade extends BankUpgrade{

    private final static String UPGRADE_ID = "DEPOSIT_LIMIT";
    private final static String UNLOCK_NAME = "deposit_limit";
    private final static int[] PRICES = {0, 10000, 100000, 1000000};
    private final static int[] DEPOSIT_LIMIT_VALUES = {50000, 100000, 500000, 1000000};
    private final static String[] NAMES = {"Miniscule Deposit Limit", "Huge Deposit Limit Upgrade", "Massive Deposit Limit Upgrade", "Humongous Deposit Limit Upgrade"};
    private final static String DESCRIPTION = "Allows me to hoard more money! You should definitely buy this one.";
    private final static String UNIT = " money";
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
    int[] getPrices() {
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
    public String getUnit() {
        return UNIT;
    }
}
