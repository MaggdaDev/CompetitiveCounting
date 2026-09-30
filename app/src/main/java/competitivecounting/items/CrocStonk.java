package competitivecounting.items;

import competitivecounting.Price;
import competitivecounting.Util;

public class CrocStonk extends Item {   // TODO auch für loans
    public final static String NAME = "Croc\uD83D\uDCC8Stock";
    public final static String DESCRIPTION = "While in your inventory, grants you " + Util.ratioToPercentageString(1) + " of the CrocBank Inc.'s profit. Use `~bank stocks` "
            +  "for more info.";
    public final static CrocStonk instance = new CrocStonk();
    public final static int CROC_STOCKS_FOR_HALF_BANK = 50;

    private CrocStonk() {
        super(null, NAME, DESCRIPTION);
    }

    public static double getBankOwnershipPerCrocStock(int crocStocksEmitted) {
        return Math.atan(crocStocksEmitted / (double) CROC_STOCKS_FOR_HALF_BANK * Math.PI / 2.) / (Math.PI / 2) / (double)crocStocksEmitted;
    }
}
