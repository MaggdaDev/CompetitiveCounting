package competitivecounting.tradeoffer;

import competitivecounting.Counter;

public class ContractNullTrade extends Tradable {
    private Counter givingCounter, gettingCounter;

    public ContractNullTrade(Counter giver, Counter getter) {
        givingCounter = giver;
        gettingCounter = getter;
    }

    public Counter getGivingCounter() {
        return givingCounter;
    }

    public Counter getGettingCounter() {
        return gettingCounter;
    }

}
