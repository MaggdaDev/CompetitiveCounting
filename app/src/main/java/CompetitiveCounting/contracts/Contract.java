/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting.contracts;

import CompetitiveCounting.Counter;
import CompetitiveCounting.CountingBot;

import java.util.ArrayList;

/**
 * @author DavidPrivat
 */
public class Contract {
    public final static String ACCEPT_REMOVE_CONTRACT_PREFIX = "accept_remove_contract:";
    public final static String DECLINE_REMOVE_CONTRACT_PREFIX = "decline_remove_contract:";
    private static int currRemoveId = 0;
    public int paidBack;
    public int percentage;  //1 - 100
    public int limit;       //  limti = -1 := no limit
    public String toId;
    public transient Counter owner; // default: null

    public transient String requested_remove_id = null;
    public transient long remove_request_time = 0;

    public final static long REMOVE_REQUEST_TIMEOUT = 1000 * 60 * 60; // 1h

    public final transient ArrayList<String> expired_remove_request_ids = new ArrayList<>();

    public void requestRemove() {
        if (requested_remove_id != null) {
            expired_remove_request_ids.add(requested_remove_id);
        }
        remove_request_time = System.currentTimeMillis();
        requested_remove_id = String.valueOf(currRemoveId);
        currRemoveId++;
    }

    public boolean isRemoveRequestTimedOut() {
        if (requested_remove_id == null) {
            return false;
        }
        return System.currentTimeMillis() - remove_request_time > REMOVE_REQUEST_TIMEOUT;
    }

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

    public boolean isExpiredRemoveRequestId(String id) {
        return expired_remove_request_ids.contains(id);
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
        String countersInfo = owner.getName() + " -> " + CountingBot.getInstance().getCounter(owner.getGuildId(), toId).getName();
        if (limit == -1) {
            return countersInfo + ": " + percentage + "% of income (" + paidBack + " paid so far)";
        } else {
            return countersInfo + ": " + percentage + "% of income until " + limit + " paid (" + paidBack + " paid so far)";
        }
    }
}
