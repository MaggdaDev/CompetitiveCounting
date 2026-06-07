/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting.rules;
import CompetitiveCounting.BaseSystems;

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

    public abstract String getRuleTypeString();

    public abstract String getValueInBase();
    
    @Override
    public String getOwnerId() {
        return ownerId;
    }

    public String getNumberRuleType() {
        return numberRuleType;
    }
}
