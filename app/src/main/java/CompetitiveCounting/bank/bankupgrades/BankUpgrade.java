package CompetitiveCounting.bank.bankupgrades;

public abstract class BankUpgrade {
    // todo accept decline buttons loan
    // todo functionlaity vong der ganzen upgrades her (wenigger diesmal)
    // todo info rückmeldung feedback dings wenn vong upgrade her durchgestrichen du wiest schon brudi
    // todo preis int array call dingens auf 0 anpassen weil wir ham das jetz gechanged vong der 1 auf die 0 her
    // todo bessere communication bei loan (geht nich weil loan limit, geht nich weil debt limit ects)
    // todo secrets einbauen die dann vong die trophies her geben rum
    protected final String upgradeId;
    protected int currentLvl = 0;

    abstract String[] getNames();
    abstract int[] getPrices();
    abstract String getDescription();
    abstract String getUnlockName();
    public abstract int getCurrentValue();
    public abstract String getUnit();  // note this has a space like " money" so that "%" can not have a space

    protected BankUpgrade(String upgradeId) {
        this.upgradeId = upgradeId;
    }

    public String getStatusString() {
        return getCurrentName() + " (level " + getCurrentLvl() + " of " + getMaxLvl() + "): " + getCurrentValue() + getUnit() + "\n-# " + getDescription() + "\n";
    }

    public void incrementLvl() {
        currentLvl++;
    }

    public int howMuchIsTheNextLvl() {
        if (isMaxedOut()) {
            return -1;
        }
        return getPrices()[currentLvl+1];
    }

    public int getCurrentLvl() {
        return currentLvl;
    }

    public boolean isMaxedOut() {
        return currentLvl >= getPrices().length-1;
    }

    public int getMaxLvl() {
        return getPrices().length - 1;
    }

    public String getCurrentLvlOutOfMaxLvlString() {
        return (currentLvl) + "/" + (getMaxLvl());
    }

    public String getNextName() {
        if (isMaxedOut()) {
            return "MAXED OUT";
        }
        return getNames()[currentLvl+1];
    }

    public String getCurrentName() {
        return getNames()[currentLvl];
    }
}
