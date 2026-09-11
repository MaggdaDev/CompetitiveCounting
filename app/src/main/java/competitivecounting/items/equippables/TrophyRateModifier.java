package competitivecounting.items.equippables;

import competitivecounting.CountingContext;

public interface TrophyRateModifier {
    public double modifyTrophyRate(double rate, CountingContext context);
}
