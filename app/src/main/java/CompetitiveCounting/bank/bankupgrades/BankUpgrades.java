package CompetitiveCounting.bank.bankupgrades;

public class BankUpgrades {
    private final DepositLimitUpgrade depositLimitUpgrade = new DepositLimitUpgrade();
    private final LoanRateUpgrade loanRateUpgrade = new LoanRateUpgrade();
    private final LoanLimitUpgrade loanLimitUpgrade = new LoanLimitUpgrade();

    private final BankUpgrade[] allUpgrades = {depositLimitUpgrade, loanRateUpgrade, loanLimitUpgrade};
    @Override
    public String toString() {
        return "Your upgrades: \n" +
                " - depositLimitUpgrade: " + (depositLimitUpgrade.getCurrentLvl()+1)+
                "\n - loanRateUpgrade: " + (loanRateUpgrade.getCurrentLvl()+1) +
                "\n - loanLimitUpgrade: " + (loanLimitUpgrade.getCurrentLvl()+1);
    }

    public String getBuyablesString() {
        String s = "";
        int counter = 1;
        for (BankUpgrade upgrade : allUpgrades) {
            if (!upgrade.isMaxedOut()) {
                s += counter + ". '" + upgrade.getUnlockName() + "': Buy the " + upgrade.getNextName() + " for " + upgrade.howMuchIsTheNextLvl() + " money (currently: " + upgrade.getCurrentLvlOutOfMaxLvlString() + ") \n";
            }
        }
        return s;

    }

    public LoanRateUpgrade getLoanRateUpgrade() {
        return loanRateUpgrade;
    }
}