package CompetitiveCounting.items.equippables;

import CompetitiveCounting.Price;
import CompetitiveCounting.items.Item;

public class VaultLocator extends Equippable {
    private int locatedVaults = 0;
    public final static String NAME = "Vault:satellite:Locator";
    public final static String DESCRIPTION = "When equipped, vaults will occasionally spawn on your counts if you meet their respective requirements.";
    public final static String COLLECTION_DESCRIPTION = "You can now locate vaults!\n-# Vaults located so far: {0}"; // todo: Use ~vaults to see the different types of vauls
    public VaultLocator() {
        super(new Price(1, Price.Unit.PRESTIGE_POINTS), NAME,DESCRIPTION);
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

    @Override
    public String getCollectionDescription() {
        return COLLECTION_DESCRIPTION.replace("{0}", String.valueOf(locatedVaults));
    }

    @Override
    public Equippable createObject() {
        return new VaultLocator();
    }
}
