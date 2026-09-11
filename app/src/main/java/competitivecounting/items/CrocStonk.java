package competitivecounting.items;

import competitivecounting.Price;

public class CrocStonk extends Item {
    public final static String NAME = "Croc:chart_with_upwards_trend:Stock";
    public final static String DESCRIPTION = "Coming soon: Certifies a 5% ownership of the CrocBank Inc.";
    public final static CrocStonk instance = new CrocStonk();
    private CrocStonk() {
        super(null, NAME, DESCRIPTION);
    }
}
