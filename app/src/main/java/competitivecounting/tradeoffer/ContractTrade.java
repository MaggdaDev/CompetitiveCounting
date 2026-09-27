package competitivecounting.tradeoffer;

import competitivecounting.Counter;

public class ContractTrade extends Tradable {
    private final int perc;
    private int limit;      //limit=-1 := no limti
    private Counter givingCounter, gettingCounter;

    public ContractTrade(String string, Counter giver, Counter getter) {
        givingCounter = giver;
        gettingCounter = getter;
        if (string.contains(";")) {
            String[] splitted = string.split(";");
            String percString = splitted[0];
            perc = Integer.parseInt(percString.split(":")[1].replaceAll(" ", "").replaceAll("%", ""));
            limit = Integer.parseInt(splitted[1].split(":")[1].replaceAll(" ", ""));
        } else {
            String[] splitted = string.split(":");
            perc = Integer.parseInt(splitted[1].replaceAll(" ", "").replaceAll("%", ""));
            limit = -1;     // limit = -1 := no limit
        }
    }

    public Counter getGivingCounter() {
        return givingCounter;
    }

    public Counter getGettingCounter() {
        return gettingCounter;
    }

    public int getPercentage() {
        return perc;
    }

    public int getLimit() {
        return limit;
    }

}
