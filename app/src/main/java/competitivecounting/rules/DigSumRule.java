/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competitivecounting.rules;

import competitivecounting.BaseSystems;

/**
 *
 * @author DavidPrivat
 */
public class DigSumRule extends NumberRule{

    private final int digSum;
    private final double base;
    final static String NUMBER_RULE_TYPE = "DIGSUM";
    public DigSumRule(String owner, int digSum, int base) {
        super(owner, NUMBER_RULE_TYPE);
        this.digSum = digSum;
        this.base = base;
    }
    
    
    @Override
    public boolean numberAccepted(int number) {
        int calculatedDigSum = 0;
        int restOfNumber = number;
        int counter = 1;
        while(restOfNumber > 0) {
            int dig = restOfNumber % ((int)Math.pow(base, (double)counter));
            calculatedDigSum += (int) (dig / Math.pow(base, (double)(counter - 1)));
            restOfNumber -= dig;
            
            counter++;
        }
        return calculatedDigSum != digSum;
    }

    @Override
    public String toString() {
        return "Every number with the digsum " + BaseSystems.decimalToSystem(digSum, (int)base) + " has to be skipped!";
    }

    @Override
    public String getRuleTypeString() {
        return "Numbers must not have a digsum of: ";
    }

    @Override
    public String getValueInBase() {
        return BaseSystems.decimalToSystem(digSum, (int)base);
    }

}
