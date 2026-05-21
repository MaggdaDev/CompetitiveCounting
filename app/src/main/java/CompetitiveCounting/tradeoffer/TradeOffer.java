/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting.tradeoffer;

import CompetitiveCounting.Counter;
import CompetitiveCounting.CountingBot;
import CompetitiveCounting.tradeoffer.Tradable.MoneyTrade;
import CompetitiveCounting.Util;
import discord4j.core.object.entity.Message;

/**
 *
 * @author DavidPrivat
 */
public class TradeOffer {

    public final static String YOU_GET = "YOU_GET:";
    public final static String I_GET = "I_GET:";
    private String userId, userPing;
    private Tradable[] youGetTrades, iGetTrades;
    private Counter initCounter, requCounter;

    public TradeOffer(Counter initBruv, Counter rehKuhCounter, Tradable[] iGetBitches, Tradable[] judeTrades, String userId, String userPing) { //in upper case
        this.initCounter = initBruv;
        this.requCounter = rehKuhCounter;
        this.iGetTrades = iGetBitches;
        this.youGetTrades = judeTrades;
        this.userId = userId;
        this.userPing = userPing;

    }

    public Counter getRequestedUser() {
        return requCounter;
    }

    public void fullfill(Message message) {
        // all first: end contracts
        for (Tradable currTr : youGetTrades) {
            if (currTr instanceof Tradable.ContractNullTrade) {
                giveTradableFromTo(initCounter, requCounter, currTr, message);
            }
        }
        for (Tradable currTr : iGetTrades) {
            if (currTr instanceof Tradable.ContractNullTrade) {
                giveTradableFromTo(requCounter, initCounter, currTr, message);
            }
        }
        //first money
        for (Tradable currTr : youGetTrades) {
            if (currTr instanceof MoneyTrade) {
                giveTradableFromTo(initCounter, requCounter, currTr, message);
            }
        }
        for (Tradable currTr : iGetTrades) {
            if (currTr instanceof MoneyTrade) {
                giveTradableFromTo(requCounter, initCounter, currTr, message);
            }
        }
        //now contracts
        for (Tradable currTr : youGetTrades) {
            if (currTr instanceof Tradable.ContractTrade) {
                giveTradableFromTo(initCounter, requCounter, currTr, message);
            }
        }
        for (Tradable currTr : iGetTrades) {
            if (currTr instanceof Tradable.ContractTrade) {
                giveTradableFromTo(requCounter, initCounter, currTr, message);
            }
        }

        CountingBot.getInstance().save();
    }

    private void giveTradableFromTo(Counter from, Counter to, Tradable tradable, Message message) {
        if (tradable instanceof Tradable.MoneyTrade) {
            from.transferTo(to, ((MoneyTrade) tradable).getAmount(), message);
            return;
        }
        if (tradable instanceof Tradable.ContractTrade) {
            Tradable.ContractTrade trade = (Tradable.ContractTrade) tradable;
            from.getContractHandler().addContract(to, trade.getPercentage(), trade.getLimit());
        }
        if (tradable instanceof Tradable.ContractNullTrade) {
            to.cancelContractsTo(from);

        }
    }

    public String getRequestedUserId() {
        return userId;
    }

    public String getUserPing() {
        return userPing;
    }

    public boolean isTradeOfferValid(Message message) {
        String isValid = this.isTradeOfferValid();
        if(isValid.equals("VALID")) {
            return true;
        } 
        CountingBot.write(message, isValid);
        return false;
    }

    private boolean containsEndContracts(Tradable[] t) {
        for (Tradable currTradable : t) {
            if (currTradable instanceof Tradable.ContractNullTrade) {
                return true;
            }
        }
        return false;
    }

    public String isTradeOfferValid() {
        //check money
        if (!requCounter.canAfford(getTotalMoneyRequirement(iGetTrades))) {
            return userPing + " doesn't have enough money in their bank!";
        }
        if (!initCounter.canAfford(getTotalMoneyRequirement(youGetTrades))) {
            return "You don't have enough money in your bank!";
        }

        // check contract < 100%
        // first: check if contains end_contracts
        if (containsEndContracts(youGetTrades)) {
            if (getTotalContractPerc(iGetTrades) + requCounter.getContractHandler().getCurrentTotalPercExcludingTo(initCounter) > 100) {
                return this.requCounter.getName() + " can't give away more than 100% of their earnings!";
            }
        } else {
            if (getTotalContractPerc(iGetTrades) + requCounter.getContractHandler().getCurrentTotalPerc() > 100) {
                return this.requCounter.getName() + " can't give away more than 100% of their earnings!";
            }
        }

        if (containsEndContracts(iGetTrades)) {
            if (getTotalContractPerc(youGetTrades) + initCounter.getContractHandler().getCurrentTotalPercExcludingTo(requCounter)> 100) {
                return "You can't give away more than 100% of your earnings!";
            }
        } else {
            if (getTotalContractPerc(youGetTrades) + initCounter.getContractHandler().getCurrentTotalPerc() > 100) {
               return "You can't give away more than 100% of your earnings!";

            }
        }
        
    

        return "VALID";
    }

    private int getTotalMoneyRequirement(Tradable[] tradables) {
        int money = 0;
        for (Tradable trad : tradables) {
            if (trad instanceof Tradable.MoneyTrade) {
                money += ((Tradable.MoneyTrade) trad).getAmount();
            }
        }
        return money;
    }

    private int getTotalContractPerc(Tradable[] tradables) {
        int tot = 0;
        for (Tradable trad : tradables) {
            if (trad instanceof Tradable.ContractTrade) {
                tot += ((Tradable.ContractTrade) trad).getPercentage();
            }
        }
        return tot;
    }
}
