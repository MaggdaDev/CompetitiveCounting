package competitivecounting.items.equippables;

import com.google.common.base.Objects;
import competitivecounting.Counter;
import competitivecounting.CountingContext;
import competitivecounting.Price;
import competitivecounting.Util;
import competitivecounting.items.Item;
import org.jetbrains.annotations.Nullable;

public class VaultLocator extends Equippable implements VaultRateModifier {
    private int locatedVaults = 0;
    public final static String NAME = "Vault\uD83D\uDCE1Locator";
    public final static String DESCRIPTION = "When equipped, vaults will occasionally spawn on your counts if you meet their respective requirements.";
    public final static String COLLECTION_DESCRIPTION = "You can now locate vaults (~vaults for more info). \n-# Vaults located: {0}      {1}";
    public final static Price PRICE = new Price(1, Price.Unit.PRESTIGE_POINTS);
    public VaultLocator(Counter owner) {
        super(PRICE, NAME, DESCRIPTION, owner);
    }

    public void incrementLocatedVaults() {
        locatedVaults++;
    }

    @Override
    public String getCollectionDescription() {
        return COLLECTION_DESCRIPTION.replace("{0}", String.valueOf(locatedVaults))
                .replace("{1}", level > 1 ? "Bonus vault chance: "
                        + Util.bonusMultToAddPercString(getVaultChanceBonusMultiplier()) : "");
    }

    @Override
    public Equippable createObject(Counter owner) {
        return new VaultLocator(owner);
    }

    public double getVaultChanceBonusMultiplier() {
        return getMultiplierFromLevel();
    }

    @Override
    public double modifyVaultRate(double rate, @Nullable CountingContext context) {
        if (context != null && !Objects.equal(context.getCounter().getId(), owner.getId())) {  // Should be obsolete, as this is only called on current counter
            return rate;
        }
        return modifyProbabilityFromLevel(rate);
    }
}
