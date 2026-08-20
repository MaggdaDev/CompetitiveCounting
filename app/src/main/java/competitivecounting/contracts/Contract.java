/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competitivecounting.contracts;

import competitivecounting.CountingBot;
import competitivecounting.bank.Bank;

import java.util.Objects;
import java.util.UUID;

/**
 * @author DavidPrivat
 */
public class Contract {
    public final static String ACCEPT_REMOVE_CONTRACT_PREFIX = "accept_remove_contract:";
    public final static String DECLINE_REMOVE_CONTRACT_PREFIX = "decline_remove_contract:";
    public int paidBack;
    public int percentage;  //1 - 100
    public int limit;       //  limti = -1 := no limit
    public String toId;
    public transient ContractOwner owner; // default: null
    private String contractId = "";

    public Contract(ContractOwner getter, int percentage, int limit) {
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
        String toName;
        if (Objects.equals(toId, Bank.CONTRACT_OWNER_ID)) { // bank
            toName = Bank.CONTRACT_ENTITY_NAME;
        } else {
            toName = CountingBot.getInstance().getCounter(owner.getGuildId(), toId).getName();
        }
        String countersInfo = owner.getName() + " -> " + toName;
        if (limit == -1) {
            return countersInfo + ": " + percentage + "% of income (" + paidBack + " paid so far)";
        } else {
            return countersInfo + ": " + percentage + "% of income until " + limit + " paid (" + paidBack + " paid so far)";
        }
    }

    public String getContractId() {
        if (contractId == null || contractId.isEmpty()) {
            contractId = UUID.randomUUID().toString();
        }
        return contractId;
    }
}
