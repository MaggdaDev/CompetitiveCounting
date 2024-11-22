/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;

import discord4j.core.object.entity.Message;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author DavidPrivat
 */
public class ContractHandler {

    private transient Counter owner;
    private transient List<Contract> contracts;
    private transient boolean currentlyAlreadyIterating = false;

    public ContractHandler(Counter owner) {
        this.owner = owner;
        contracts = owner.getContracts();
    }

    public void addContract(Counter getter, int percentage, int limit) {
        Contract add = new Contract(getter, percentage, limit);
        add.owner = owner;
        Contract equalContract = null;
        for (Contract currContract : contracts) {
            if (currContract.toId.equals(add.toId) && ((add.limit == -1 && currContract.limit == -1) || (currContract.limit == add.limit && currContract.percentage == add.percentage))) {
                equalContract = currContract;
                break;
            }
        }
        if (equalContract == null) {
            contracts.add(add);
            getter.getIncomingContracts().add(add);
        } else {
            if (limit != -1) {
                equalContract.limit += add.limit;
            }
            equalContract.percentage += add.percentage;
        }
    }

    public void checkExpiredContracts(Message message) {
        Iterator<Contract> it = contracts.iterator();
        while (it.hasNext()) {
            Contract currContr = it.next();

            if (!currContr.isValid()) {
                it.remove();
                Counter toCounter = CountingBot.getInstance().getCounter(currContr.toId);
                toCounter.getIncomingContracts().remove(currContr);
                CountingBot.write(message, "Contract from " + currContr.owner.getPing() + " to " + toCounter.getPing() + " expired:\n" + currContr.toString());
                CountingBot.getInstance().safeCounters();
            }
        }
    }

    public void removeContract(Contract contract) {
        contracts.remove(contract);
    }

    public int getCurrentTotalPerc() {
        int all = 0;
        for (Contract curr : contracts) {
            all += curr.percentage;
        }
        return all;
    }

    public int getCurrentTotalPercExcludingTo(Counter counter) {
        int all = 0;
        for (Contract curr : contracts) {
            if (!curr.toId.equals(counter.getId())) {
                all += curr.percentage;
            }
        }
        return all;
    }

    public int getNetto(int brutto, Message message) {
        int netto = brutto;
        boolean isInRecursive;
        if (this.currentlyAlreadyIterating) {
            isInRecursive = true;
        } else {
            currentlyAlreadyIterating = true;
            isInRecursive = false;
        }
        for (Contract curr : contracts) {
            int pay = curr.getPaid(brutto);
            netto -= pay;
            CountingBot.getInstance().getCounter(curr.toId).addBonusScoreFromContract(pay, message);
        }
        if (!isInRecursive) {
            currentlyAlreadyIterating = false;
        }
        if (!currentlyAlreadyIterating) {
            checkExpiredContracts(message);
        }
        return netto;
    }

}
