package CompetitiveCounting.bank.bankupgrades;

public class BankUpgrades {
    private final DepositLimitUpgrade depositLimitUpgrade = new DepositLimitUpgrade();
    private final LoanRateUpgrade loanRateUpgrade = new LoanRateUpgrade();
    private final LoanLimitUpgrade loanLimitUpgrade = new LoanLimitUpgrade();
    private final DebtLimitUpgrade debtLimitUpgrade = new DebtLimitUpgrade();

    private BankUpgrade[] getAllUpgrades() {
        return new BankUpgrade[]{depositLimitUpgrade, loanRateUpgrade, loanLimitUpgrade, debtLimitUpgrade};
    }

    @Override
    public String toString() {
        return "**Your upgrades** \n" +
                depositLimitUpgrade.getStatusString() + "\n" +
                loanRateUpgrade.getStatusString() + "\n" +
                loanLimitUpgrade.getStatusString() + "\n" +
                debtLimitUpgrade.getStatusString();
    }

    public String getBuyablesString() {
        String s = "";
        int counter = 1;
        for (BankUpgrade upgrade : getAllUpgrades()) {
            if (!upgrade.isMaxedOut()) {
                String currentValueWithUnit = upgrade.getCurrentValue() + upgrade.getUnit();
                String nextValueWithUnit = upgrade.getNextValue() + upgrade.getUnit();
                s += counter + ". '" + upgrade.getUnlockName() + "': Buy the " + upgrade.getNextName() + " for " + upgrade.howMuchIsTheNextLvl() +
                        " money (currently " + upgrade.getCurrentLvlOutOfMaxLvlString() + ").\n" +
                        "-# " + upgrade.getAdvertisement().replace("{0}", currentValueWithUnit).replace("{1}", nextValueWithUnit)
                        + "\n\n";
            }
            counter += 1;
        }
        return s;
    }

    public int getAmountBuyableUpgrades() {
        int counter = 0;
        for (BankUpgrade upgrade : getAllUpgrades()) {
            if (!upgrade.isMaxedOut()) {
                counter++;
            }
        }
        return counter;
    }

    public LoanRateUpgrade getLoanRateUpgrade() {
        return loanRateUpgrade;
    }

    public LoanLimitUpgrade getLoanLimitUpgrade() {
        return loanLimitUpgrade;
    }

    public DebtLimitUpgrade getDebtLimitUpgrade () {
        return debtLimitUpgrade;
    }

    public DepositLimitUpgrade getDepositLimitUpgrade() {
        return depositLimitUpgrade;
    }

    /**
     *
     * @param unlockName
     * @return the upgrade with the given unlock name (or null if none was found)
     */
    public BankUpgrade parseUpgrade(String unlockName) {
        for (BankUpgrade upgrade : getAllUpgrades()) {
            if (upgrade.getUnlockName().equals(unlockName)) {
                return upgrade;
            }
        }
        return null;
    }


}