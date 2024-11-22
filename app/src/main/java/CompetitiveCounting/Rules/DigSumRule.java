/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting.Rules;

/**
 *
 * @author DavidPrivat
 */
public class DigSumRule extends NumberRule{

    private final int digSum;
    public DigSumRule(String owner, int digSum) {
        super(owner);
        this.digSum = digSum;        
    }
    
    
    @Override
    public boolean numberAccepted(int number) {
        int calculatedDigSum = 0;
        int restOfNumber = number;
        int counter = 1;
        while(restOfNumber > 0) {
            int dig = restOfNumber % ((int)Math.pow(10.0d, (double)counter));
            calculatedDigSum += dig / Math.pow(10.d, (double)(counter - 1));
            restOfNumber -= dig;
            
            counter++;
        }
        return calculatedDigSum != digSum;
    }

    @Override
    public String toString() {
        return "Every number with the digsum " + digSum + " has to be skipped!";
    }
    
}
