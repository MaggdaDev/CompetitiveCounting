package CompetitiveCounting.bank.bankupgrades;

public abstract class BankUpgrade {
    // irgendwanntodo secrets einbauen die dann vong die trophies her geben rum
    protected final String upgradeId;
    protected int currentLvl = 0;

    abstract String[] getNames();
    abstract int[] getPrices();
    abstract String getDescription();
    abstract String getUnlockName();
    public abstract int getCurrentValue();
    public abstract int getNextValue();
    public abstract String getAdvertisement();
    public abstract String getUnit();  // note this has a space like " money" so that "%" can not have a space
    public abstract String getBoughtFeedback();

    protected BankUpgrade(String upgradeId) {
        this.upgradeId = upgradeId;
    }

    public String getStatusString() {
        String result_string = getCurrentName() + " ";
        if (isMaxedOut()) {
            result_string += "(Maximum Level): ";
        } else {
            result_string += "(Level " + getCurrentLvl() + " of " + getMaxLvl() + "): ";
        }
        return result_string + getCurrentValue() + getUnit() + "\n-# " + getDescription() + "\n";
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
