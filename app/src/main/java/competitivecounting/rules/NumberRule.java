/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competitivecounting.rules;

import java.util.Objects;

/**
 *
 * @author DavidPrivat
 */
public abstract class NumberRule implements Rule {
    private final String ownerId;
    private final String numberRuleType;

    public NumberRule(String ownerId, String numberRuleType) {
        this.ownerId = ownerId;
        this.numberRuleType = numberRuleType;
    }
    
    public abstract boolean numberAccepted(int number);
    
    public abstract String toString();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumberRule that = (NumberRule) o;

        return Objects.equals(numberRuleType, that.numberRuleType) &&
                Objects.equals(getValueInBase(), that.getValueInBase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(numberRuleType, getValueInBase());
    }

    public abstract String getRuleTypeString();

    public abstract String getValueInBase();
    
    @Override
    public String getOwnerId() {
        return ownerId;
    }

    public String getNumberRuleType() {
        return numberRuleType;
    }

    public abstract int getMinimumValue();
}
