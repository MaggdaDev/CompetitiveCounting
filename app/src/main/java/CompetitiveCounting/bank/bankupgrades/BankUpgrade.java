package CompetitiveCounting.bank.bankupgrades;

public class BankUpgrade {

    protected transient int[] prices;
    protected final String[] names;
    protected final String description;

    protected final String unlockName;

    protected int currentLvl = 0;

    protected BankUpgrade(String unlockName, String[] names, int[] prices, String description) {
        this.unlockName = unlockName;
        this.names = names;
        this.prices = prices;
        this.description = description;
    };

    public void incrementLvl() {
        currentLvl++;
    }

    public int howMuchIsTheNextLvl() {
        if (isMaxedOut()) {
            return -1;
        }
        return prices[currentLvl+1];
    }

    public int getCurrentLvl() {
        return currentLvl;
    }

    public boolean isMaxedOut() {
        return currentLvl >= prices.length-1;
    }

    public int getMaxLvl() {
        return prices.length - 1;
    }

    public String getCurrentLvlOutOfMaxLvlString() {
        return (currentLvl+1) + "/" + (getMaxLvl()+1);
    }

    public String getUnlockName() {
        return unlockName;
    }

    public String getNextName() {
        if (isMaxedOut()) {
            return "MAXED OUT";
        }
        return names[currentLvl+1];
    }
}
