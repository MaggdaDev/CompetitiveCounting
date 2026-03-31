package CompetitiveCounting.bank.bankupgrades;

public class DebtLimitUpgrade extends BankUpgrade{
    private final static String UPGRADE_ID = "DEBT_LIMIT";
    private final static String UNLOCK_NAME = "debt_limit";
    private final static int[] PRICES = {0, 50000, 200000, 1000000};
    private final static int[] DEBT_LIMIT_VALUES = {150000, 350000, 750000, 1500000};
    private final static String[] NAMES = {"Awful Debt Limit", "Nice Debt Limit Upgrade", "Amazing Debt Limit Upgrade", "Phenomenal Debt Limit Upgrade"};
    private final static String DESCRIPTION = "Allows you to have more total debt! I hope you bought this first...";
    protected DebtLimitUpgrade() {
        super(UPGRADE_ID);
    }

    public int[] getDebtLimitValues() {
        return DEBT_LIMIT_VALUES;
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

}
