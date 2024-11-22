/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;

import CompetitiveCounting.Tradable.MoneyTrade;
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

    public TradeOffer(String content, Counter initC) { //in upper case
        initCounter = initC;
        String[] splitted;
        boolean containsYouGet = true;
        if (content.contains(YOU_GET)) {
            splitted = content.split(YOU_GET);
        } else {
            splitted = content.split(I_GET);
            containsYouGet = false;
        }
        String init = splitted[0];
        String offers = splitted[1];

        String youGet, iGet;
        if (containsYouGet) {
            splitted = offers.split(I_GET);
            youGet = splitted[0];
            if (splitted.length > 1) {
                iGet = splitted[1];
            } else {
                iGet = "";
            }
        } else {
            youGet = "";
            iGet = offers;
        }
        splitted = init.split(" ");
        userPing = splitted[1].replaceAll(" ", "");
        userId = userPing.substring(2, userPing.length() - 1);
        requCounter = CountingBot.getInstance().getCounter(userId);

        try {
            youGetTrades = Tradable.generateTradables(youGet, initCounter, requCounter);
            iGetTrades = Tradable.generateTradables(iGet, requCounter, initCounter);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        CountingBot.getInstance().safeCounters();
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
        return true;
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
        if (getTotalMoneyRequirement(iGetTrades) > requCounter.getScore()) {
            return userPing + " doesn't have enough money in their bank!";
        }
        if (getTotalMoneyRequirement(youGetTrades) > initCounter.getScore()) {
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
