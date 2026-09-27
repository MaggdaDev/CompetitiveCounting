/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competitivecounting.tradeoffer;

import competitivecounting.Counter;
import competitivecounting.CountingBot;
import competitivecounting.items.Item;
import discord4j.core.object.entity.Message;

import java.util.HashMap;

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
            if (currTr instanceof ContractNullTrade) {
                giveTradableFromTo(initCounter, requCounter, currTr, message);
            }
        }
        for (Tradable currTr : iGetTrades) {
            if (currTr instanceof ContractNullTrade) {
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
            if (currTr instanceof ContractTrade) {
                giveTradableFromTo(initCounter, requCounter, currTr, message);
            }
        }
        for (Tradable currTr : iGetTrades) {
            if (currTr instanceof ContractTrade) {
                giveTradableFromTo(requCounter, initCounter, currTr, message);
            }
        }
        // now items
        for (Tradable currTr : youGetTrades) {
            if (currTr instanceof ItemTrade) {
                giveTradableFromTo(initCounter, requCounter, currTr, message);
            }
        }
        for (Tradable currTr : iGetTrades) {
            if (currTr instanceof ItemTrade) {
                giveTradableFromTo(requCounter, initCounter, currTr, message);
            }
        }

        CountingBot.getInstance().save();
    }

    private void giveTradableFromTo(Counter from, Counter to, Tradable tradable, Message message) {
        if (tradable instanceof MoneyTrade) {
            from.transferTo(to, ((MoneyTrade) tradable).getAmount(), message);
            return;
        }
        if (tradable instanceof ContractTrade) {
            ContractTrade trade = (ContractTrade) tradable;
            from.getContractHandler().addContract(to, trade.getPercentage(), trade.getLimit());
        }
        if (tradable instanceof ContractNullTrade) {
            to.cancelContractsTo(from);
        }
        if (tradable instanceof ItemTrade) {
            ItemTrade trade = (ItemTrade) tradable;
            String itemName = trade.getItemIdentifier();
            int itemAmount = trade.getAmount();
            Item item = Item.getItemByName(itemName);
            from.getInventory().removeItems(item, itemAmount);
            to.getInventory().addItems(item, itemAmount);
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
            if (currTradable instanceof ContractNullTrade) {
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

        // check items
        String itemsResult = checkItemTradesValid();
        if (!"VALID".equals(itemsResult)) {
            return itemsResult;
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

    private String checkItemTradesValid() {
        HashMap<String, Integer> youGetItemsAmountMap = extractItemAmountHashMap(iGetTrades);
        for (String itemName : youGetItemsAmountMap.keySet()) {
            int amount = youGetItemsAmountMap.get(itemName);
            Item item = Item.getItemByName(itemName);
            if (item == null) {
                return "Unknown item: '" + itemName + "'. Please provide the full name of the item you want to trade!\n-# The emoji may be omitted.";
            }
            if (amount <= 0) {
                return "Invalid amount of item: '" + item.getName() + "'. Please provide a positive amount of the item you want to trade!";
            }
            if (requCounter.getInventory().getAmountOfItem(item) < amount) {
                return requCounter.getName() + " doesn't have enough " + item.getName() + "s!";
            }
        }
        HashMap<String, Integer> iGetItemsAmountMap = extractItemAmountHashMap(youGetTrades);
        for (String itemName : iGetItemsAmountMap.keySet()) {
            int amount = iGetItemsAmountMap.get(itemName);
            Item item = Item.getItemByName(itemName);
            if (item == null) {
                return "Unknown item: '" + itemName + "'. Please provide the full name of the item you want to trade!\n-# The emoji may be omitted.";
            }
            if (amount <= 0) {
                return "Invalid amount of item: '" + item.getName() + "'. Please provide a positive amount of the item you want to trade!";
            }
            if (initCounter.getInventory().getAmountOfItem(item) < amount) {
                return "You don't have enough " + item.getName() + "s!";
            }
        }
        return "VALID";
    }

    private HashMap<String, Integer> extractItemAmountHashMap(Tradable[] tradables) {
        HashMap<String, Integer> itemAmountMap = new HashMap<>();
        for (Tradable trad : tradables) {
            if (trad instanceof ItemTrade) {
                String itemName = Item.removeEmojis(((ItemTrade) trad).getItemIdentifier()).toLowerCase().trim();
                int amount = ((ItemTrade) trad).getAmount();
                if (itemAmountMap.containsKey(itemName)) {
                    itemAmountMap.put(itemName, itemAmountMap.get(itemName) + amount);
                } else {
                    itemAmountMap.put(itemName, amount);
                }
            }
        }
        return itemAmountMap;
    }

    private int getTotalMoneyRequirement(Tradable[] tradables) {
        int money = 0;
        for (Tradable trad : tradables) {
            if (trad instanceof MoneyTrade) {
                money += ((MoneyTrade) trad).getAmount();
            }
        }
        return money;
    }

    private int getTotalContractPerc(Tradable[] tradables) {
        int tot = 0;
        for (Tradable trad : tradables) {
            if (trad instanceof ContractTrade) {
                tot += ((ContractTrade) trad).getPercentage();
            }
        }
        return tot;
    }
}
