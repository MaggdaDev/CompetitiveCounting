/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting.Rules;

import CompetitiveCounting.BaseSystems;

/**
 *
 * @author DavidPrivat
 */
public class RootRule extends NumberRule{

    private final int root, base;
    public RootRule(String owner, int nthRoot, int base) {
        super(owner);
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
        if (ord.endsWith("1")) {
            ord += "st";
        } else if (ord.endsWith("2")) {
            ord += "nd";
        } else if (ord.endsWith("3")) {
            ord += "rd";
        } else {
            ord += "th";
        }
        return "Numbers with an integer " + ord +" root must be skipped!";
    }
    
}

