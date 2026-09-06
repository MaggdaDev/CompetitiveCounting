package competitivecounting.bank.bankupgrades;

public abstract class BankUpgrade {
    // irgendwanntodo secrets einbauen die dann vong die trophies her geben rum
    protected final String upgradeId;
    protected int currentLvl = 0;

    abstract String[] getNames();
    public abstract int[] getPrices();
    abstract String getDescription();
    abstract String getUnlockName();
    public abstract int getCurrentValue();
    public abstract int getNextValue();
    public abstract String getAdvertisement();
    public abstract String getAdvertisementMaxed();
    public abstract String getUnit();  // note this has a space like " money" so that "%" can not have a space
    public abstract String getBoughtFeedback();
    public abstract int getEmptyValue();
    protected abstract String getBuyRecommendationString();
    public int getPriceOfNextLvl() {
        if (isMaxedOut()) {
            return -1;
        }
        return getPrices()[currentLvl+1];
    }

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

    /**
     * @return "~~emptyValue~~ currentValue unit" if the upgrade is not at lvl 0, otherwise just "currentValue unit"
     */
    public String getCurrentValueStringPotentiallyIndicatingEmptyValue() {
        String str = "";
        if (getCurrentLvl() > 0) {
            str += "~~" + getEmptyValue() + "~~ ";
        }
        str += getCurrentValue() + getUnit();
        return str;
    }

    public String getBuyRecommendationStringIfNotMaxedOut() {
        if (isMaxedOut()) {
            return "";
        }
        return getBuyRecommendationString();
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

    public int amountSpentOnThisUpgrade() {
        if (currentLvl == 0) return 0;
        int amountSpent = 0;
        for (int i = 1; i <= getCurrentLvl(); i++) {
            amountSpent += getPrices()[i];
        }
        return amountSpent;
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
