/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting.Rules;
import CompetitiveCounting.BaseSystems;
import CompetitiveCounting.Counter;

/**
 *
 * @author DavidPrivat
 */
public abstract class NumberRule implements Rule {
    private String ownerId;
    
    public NumberRule(String ownerId) {
        this.ownerId = ownerId;
    }
    
    public abstract boolean numberAccepted(int number);
    
    public abstract String toString();
    
    @Override
    public String getOwnerId() {
        return ownerId;
    }
    
    public static class DividerRule extends NumberRule {
        private final int divider, base;
        public DividerRule(String ownerId, int divider, int base) {
            super(ownerId);
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
}
