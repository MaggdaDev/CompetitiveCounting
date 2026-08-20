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
public class RootRule extends NumberRule{

    private final int root, base;
    final static String NUMBER_RULE_TYPE = "ROOT";
    public RootRule(String owner, int nthRoot, int base) {
        super(owner, NUMBER_RULE_TYPE);
        this.root = nthRoot;
        this.base = base;
    }
    
    
    @Override
    public boolean numberAccepted(int number) {
        double rootVal = Math.pow(number, 1.0d/((double)root));
        double maxDiff = 0.00001d;
        double diff = Math.abs(rootVal - Math.round(rootVal));
        return  diff > maxDiff;
    }

    @Override
    public String toString() {
        String ord = String.valueOf(BaseSystems.decimalToSystem(root, base));
        if (ord.endsWith("1") && !ord.endsWith("11")) {
            ord += "st";
        } else if (ord.endsWith("2") && !ord.endsWith("12")) {
            ord += "nd";
        } else if (ord.endsWith("3") && !ord.endsWith("13")) {
            ord += "rd";
        } else {
            ord += "th";
        }
        return "Numbers with an integer " + ord +" root must be skipped!";
    }

    @Override
    public String getRuleTypeString() {
        return "Numbers must not have a perfect integer root for degree: ";
    }

    @Override
    public String getValueInBase() {
        return BaseSystems.decimalToSystem(root, base);
    }

}

