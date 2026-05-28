package CompetitiveCounting.items.equippables;

import CompetitiveCounting.Price;
import CompetitiveCounting.items.Item;

public class VaultLocator extends Item {
    private int locatedVaults = 0;
    public VaultLocator() {
        super(new Price(1, Price.Unit.PRESTIGE_POINTS), "Vault:satellite:Locator",
                "When equipped, vaults will occasionally spawn on your counts if you meet their respective requirements.");
    }

    public void incrementLocatedVaults() {
        locatedVaults++;
    }

    public int getLocatedVaults() {
        return locatedVaults;
    }

    public void setLocatedVaults(int locatedVaults) {
        this.locatedVaults = locatedVaults;
    }
}
