package CompetitiveCounting.rules;

import CompetitiveCounting.BaseSystems;

public class DividerRule extends NumberRule {
    private final int divider, base;
    final static String NUMBER_RULE_TYPE = "DIVIDER";
    private final String ruleTypeString = "Numbers must not be divisible by: ";
    public DividerRule(String ownerId, int divider, int base) {
        super(ownerId, NUMBER_RULE_TYPE);
        this.divider = divider;
        this.base = base;
    }

    @Override
    public boolean numberAccepted(int number) {
        return (number % divider) != 0;
    }

    @Override
    public String toString() {
        return ruleTypeString + BaseSystems.decimalToSystem(divider, base);
    }

    @Override
    public String getRuleTypeString() {
        return ruleTypeString;
    }

    @Override
    public String getValueInBase() {
        return BaseSystems.decimalToSystem(divider, base);
    }

}