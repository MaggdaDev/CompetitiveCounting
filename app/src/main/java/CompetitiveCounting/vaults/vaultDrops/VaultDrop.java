package CompetitiveCounting.vaults.vaultDrops;

import CompetitiveCounting.Counter;
import CompetitiveCounting.dialogue.Dialogue;
import discord4j.core.object.entity.Message;

public abstract class VaultDrop {
    private final double weight;
    public VaultDrop(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public abstract void payout(Message message, Dialogue dialogue, Counter counter);
}

