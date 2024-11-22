/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;

/**
 *
 * @author DavidPrivat
 */
public class Contract {

    public int paidBack;
    public int percentage;  //1 - 100
    public int limit;       //  limti = -1 := no limit
    public String toId;
    public transient Counter owner; // default: null

    public Contract(Counter getter, int percentage, int limit) {
        toId = getter.getId();
        this.percentage = percentage;
        this.limit = limit;
        paidBack = 0;
    }

    public int getPaid(int brutto) {
        int paid = (int) (((double) percentage) * 0.01d * brutto);
        paidBack += paid;
        if (paidBack > limit) {
            int sub = paidBack - limit;
            if (sub > 0 && (paid - sub) >= 0) {
                paidBack -= sub;
                paid -= sub;
            }
        }
        return paid;
    }

    public boolean isValid() {
        if (limit == -1) {
            return true;
        }
        if (limit > paidBack) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public String toString() {
        if (limit == -1) {
            return percentage + "% of income (" + paidBack + " paid so far)";
        } else {
            return percentage + "% of income until " + limit + " paid (" + paidBack + " paid so far)";
        }
    }
}
