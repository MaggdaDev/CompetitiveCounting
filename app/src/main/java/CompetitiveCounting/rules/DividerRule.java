package CompetitiveCounting.rules;

import CompetitiveCounting.BaseSystems;

public class DividerRule extends NumberRule {
    private final int divider, base;
    final static String NUMBER_RULE_TYPE = "DIVIDER";
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
        return "Numbers must not be divisible by: " + BaseSystems.decimalToSystem(divider, base);
    }

}