package competitivecounting.items.equippables;

import competitivecounting.CountingContext;

public interface VaultRateModifier {
    public double modifyVaultRate(double rate, CountingContext context);
}
