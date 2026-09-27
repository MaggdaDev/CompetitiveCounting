/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competitivecounting.tradeoffer;


import competitivecounting.Counter;
import competitivecounting.Util;

/**
 *
 * @author DavidPrivat
 */
public abstract class Tradable {
    public final static String TRADE_SEPARATOR = "+", NAME_CONTENT_SEPARATOR = ":";

    public static Tradable[] generateTradables(String string, Counter giver, Counter getter) throws Exception { // in upper case
        if (string.isBlank()) {
            return new Tradable[]{};
        }
        if (!string.contains(TRADE_SEPARATOR)) {
            return new Tradable[]{generateTradable(string, giver, getter)};
        }
        String[] splitted = string.split("\\" + TRADE_SEPARATOR);
        Tradable[] ret = new Tradable[splitted.length];
        for (int i = 0; i < splitted.length; i++) {
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
        switch (name) {
            case "MONEY":
                return new MoneyTrade(splitted[1]);
            case "CONTRACT":
                return new ContractTrade(splitted[1], giver, getter);
            case "END_CONTRACTS":
                return new ContractNullTrade(giver, getter);
            case "ITEM": case "ITEMS":
                return new ItemTrade(splitted[1]);
            default:
                throw new Exception("Unknown tradable: " + name);
        }

    }


}
