package competitivecounting.vaults.vaultDrops;

import java.util.ArrayList;

public class VaultLootPool {
    private final ArrayList<VaultDrop> drops = new ArrayList<>();
    private double totalWeight = 0;
    public void addDrop(VaultDrop drop) {
        drops.add(drop);
        totalWeight += drop.getWeight();
    }

    public VaultDrop drawDrop() {
        if (drops.isEmpty()) {
            throw new RuntimeException("Vault pool is empty!");
        }
        double rng = Math.random() * totalWeight;
        double currThresh = 0;
        for(VaultDrop drop: drops) {
            if (rng < currThresh + drop.getWeight()) {
                return drop;
            }
            currThresh += drop.getWeight();
        }
        throw new RuntimeException("Total weight does not add up!");
    }
}
