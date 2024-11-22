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
public abstract class Tradable {
    public final static String TRADE_SEPARATOR = "+", NAME_CONTENT_SEPARATOR = ":";
    public static Tradable[] generateTradables(String string, Counter giver, Counter getter) throws Exception { // in upper case
        if(string.isBlank()) {
            return new Tradable[] {};
        }
        if(!string.contains(TRADE_SEPARATOR)) {
            return new Tradable[] {generateTradable(string, giver, getter)};
        }
        String[] splitted = string.split("\\"+TRADE_SEPARATOR);
        Tradable[] ret = new Tradable[splitted.length];
        for(int i = 0; i < splitted.length; i++) {
            try {
                ret[i] = generateTradable(splitted[i], giver, getter);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return ret;
    }
    
    private static Tradable generateTradable(String string, Counter giver, Counter getter) throws Exception {   //in upper case
        String[] splitted = Util.splitAtFirst(string, NAME_CONTENT_SEPARATOR);
        String name = splitted[0].replaceAll(" ", "");
        switch(name) {
            case "MONEY":
                return new MoneyTrade(splitted[1]);
            case "CONTRACT":
                return new ContractTrade(splitted[1], giver, getter);
            case "END_CONTRACTS":
                return new ContractNullTrade(giver, getter);
            default: 
                throw new Exception("Unknown tradable: " + name);
        }       
        
    }
    public static class MoneyTrade extends Tradable {
        private final int amount;
        public MoneyTrade(String string) {
            amount = Integer.parseInt(string.replaceAll(" ", ""));
        }
        
        public int getAmount() {
            return amount;
        }
    }
    
    public static class ContractTrade extends Tradable {
        private final int perc;
        private int limit;      //limit=-1 := no limti
        private Counter givingCounter, gettingCounter;
        public ContractTrade(String string, Counter giver, Counter getter) {
            givingCounter = giver;
            gettingCounter = getter;
            if(string.contains(";")) {
                String[] splitted = string.split(";");
                String percString = splitted[0];
                perc = Integer.parseInt(percString.split(":")[1].replaceAll(" ", "").replaceAll("%", ""));
                limit = Integer.parseInt(splitted[1].split(":")[1].replaceAll(" ", ""));
            } else {
                String[] splitted = string.split(":");
                perc = Integer.parseInt(splitted[1].replaceAll(" ", "").replaceAll("%", ""));
                limit = -1;     // limit = -1 := no limit
            }
        }
        
        public Counter getGivingCounter() {
            return givingCounter;
        }
        
        public Counter getGettingCounter() {
            return gettingCounter;
        }
        
        public int getPercentage() {
            return perc;
        }
        
        public int getLimit() {
            return limit;
        }
        
    }
    
    public static class ContractNullTrade extends Tradable {
        private Counter givingCounter, gettingCounter;
        
        public ContractNullTrade(Counter giver, Counter getter) {
            givingCounter = giver;
            gettingCounter = getter;
        }
        
        public Counter getGivingCounter() {
            return givingCounter;
        }
        
        public Counter getGettingCounter() {
            return gettingCounter;
        }
        
    }
    
    
    
}
