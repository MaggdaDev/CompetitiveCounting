package competitivecounting.items.equippables;

import competitivecounting.CountingContext;
import org.jetbrains.annotations.Nullable;

public interface VaultRateModifier {
    double modifyVaultRate(double rate, @Nullable CountingContext context);
}
