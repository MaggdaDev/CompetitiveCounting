package competitivecounting.vaults.vaultDrops;

import competitivecounting.Counter;
import competitivecounting.CountingContext;
import competitivecounting.Util;
import competitivecounting.bank.BankCommandHandler;
import competitivecounting.dialogue.Dialogue;
import competitivecounting.items.CrocStonk;
import discord4j.core.object.entity.Message;

public class CrocStockDrop extends ItemDrop {

    public CrocStockDrop(double weight) {
        super(weight, CrocStonk.instance);
    }

    @Override
    public void payout(Message message, Dialogue dialogue, Counter counter, CountingContext contextAtVaultSpawn) {
        super.payout(message, dialogue, counter, contextAtVaultSpawn);
        dialogue.addRunnable(m -> {
            int nowOwnedCrocStonks = counter.getInventory().getAmountOfItem(CrocStonk.instance);
            int totalCrocStonks = contextAtVaultSpawn.getGuild().getBank().getTotalCrocStocksEmitted();
            double ownershipPerStock = CrocStonk.getBankOwnershipPerCrocStock(totalCrocStonks);
            String percentOfCrocBank = Util.ratioToPercentageString(ownershipPerStock * nowOwnedCrocStonks);
            BankCommandHandler.bankWrite(m, "With that, a total of " + totalCrocStonks + CrocStonk.NAME + (totalCrocStonks>1? "s":"")
                    + " have been emitted, out of which you own " + nowOwnedCrocStonks + " (=" + percentOfCrocBank + " of the CrocBank Inc.)!");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            BankCommandHandler.bankWrite(message, "Check out `~bank stocks` for more info!");
        });
    }
}
