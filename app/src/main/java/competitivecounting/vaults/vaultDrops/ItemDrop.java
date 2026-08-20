package competitivecounting.vaults.vaultDrops;

import competitivecounting.Counter;
import competitivecounting.dialogue.Dialogue;
import competitivecounting.items.Item;
import discord4j.core.object.entity.Message;

public class ItemDrop extends VaultDrop {
    private final Item item;
    public ItemDrop(double weight, Item item) {
        super(weight);
        this.item = item;
    }

    @Override
    public void payout(Message message, Dialogue dialogue, Counter counter) {
        dialogue.addNpcLine("Woah, this vault is heavy... ", 2500)
                .addNpcLine("Could it be that there is more than just money in here? ", 3500)
                .addNpcLine("Gg wp, you found a " + item.getName() + "! ", 0)
                .addRunnable((m) -> {
                    counter.getInventory().addItem(item);
                });
    }
}
