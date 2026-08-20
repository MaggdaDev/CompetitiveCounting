/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competitivecounting.contracts;

import competitivecounting.Counter;
import competitivecounting.CountingBot;
import competitivecounting.CountingGuild;
import competitivecounting.bank.Bank;
import discord4j.core.object.entity.Message;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author DavidPrivat
 */
public class ContractHandler {

    private transient ContractOwner owner;
    private transient List<Contract> contracts;
    private transient boolean currentlyAlreadyIterating = false;

    public ContractHandler(ContractOwner owner) {
        this.owner = owner;
        contracts = owner.getContracts();
    }

    public void initIncomingContracts(CountingGuild activeGuild) {
        owner.getGuildId();
        if (owner.getIncomingContracts().isEmpty()) {
            activeGuild.getCounters().forEach((String currId, Counter counter) -> {
                for (Contract currContract : counter.getContracts()) {
                    if (currContract.toId.equals(owner.getId())) {
                        currContract.owner = counter;
                        owner.getIncomingContracts().add(currContract);
                    }
                }
            });

        }
    }

    public void addContract(ContractOwner getter, int percentage, int limit) {
        Contract add = new Contract(getter, percentage, limit);
        add.owner = owner;
        Contract equalContract = null;
        for (Contract currContract : contracts) {
            if (currContract.toId.equals(add.toId) && (add.limit == currContract.limit)) {
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
        String guildId = owner.getGuildId();
        while (it.hasNext()) {
            Contract currContr = it.next();

            if (!currContr.isValid()) {
                it.remove();
                ContractOwner toOwner = findContractOwner(guildId, currContr.toId);
                toOwner.getIncomingContracts().remove(currContr);
                CountingBot.write(message, "Contract from " + currContr.owner.getPing() + " to " + toOwner.getPing() + " expired:\n" + currContr.toString());
                CountingBot.getInstance().save();
            }
        }
    }

    public void removeContract(Contract contract) {
        contract.owner.getContracts().remove(contract);
        findContractOwner(owner.getGuildId(), contract.toId).getIncomingContracts().remove(contract);
        CountingBot.getInstance().save();
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
            findContractOwner(owner.getGuildId(), curr.toId).addBonusScoreFromContract(pay, message);
        }
        if (!isInRecursive) {
            currentlyAlreadyIterating = false;
        }
        if (!currentlyAlreadyIterating) {
            checkExpiredContracts(message);
        }
        return netto;
    }

        public static ContractOwner findContractOwner(String guildId, String ownerId) {
            if (ownerId.equals(Bank.CONTRACT_OWNER_ID)) {
                return CountingBot.getInstance().getGuilds().get(guildId).getBank();
            } else {
                return CountingBot.getInstance().getCounter(guildId, ownerId);
            }
        }
    }