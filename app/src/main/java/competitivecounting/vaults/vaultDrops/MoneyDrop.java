package competitivecounting.vaults.vaultDrops;

import competitivecounting.Counter;
import competitivecounting.CountingContext;
import competitivecounting.Price;
import competitivecounting.bank.BankCommandHandler;
import competitivecounting.dialogue.Dialogue;
import competitivecounting.items.equippables.SponsoredMonocle;
import discord4j.core.object.entity.Message;

public class MoneyDrop extends VaultDrop {

    private int lastDropAmount = 0;
    private boolean wasMonocleActiveAtLastDrop = false;
    public MoneyDrop(double weight) {
        super(weight);
    }

    @Override
    public void payout(Message message, Dialogue dialogue, Counter payoutReceiver, CountingContext contextAtVaultSpawn) {
        int money = draw_money();
        lastDropAmount = money;
        wasMonocleActiveAtLastDrop = contextAtVaultSpawn.getStreak().getCounterIdsOfActiveSponsoredMonocles().contains(payoutReceiver.getId());
        if (money < 2000) {
            //
        } else if (money < 20000 ) {
            dialogue.addNpcLine("Nice!", 1000);
        } else if (money < 70000) {
            dialogue.addNpcLine("Whoa!", 1500);
        } else if (money < 300000) {
            dialogue.addNpcLine("Is there a zero too much??", 1500);
        } else if(money < 1000000) {
            dialogue.addNpcLine("This must be a bug! How does so much money even fit into one vault??", 2500);
        } else {
            dialogue.addNpcLine("We should inform the police about this find...", 1500)
                    .addNpcLine("There is no way so much money got into this vault by legal means...", 1500);
        }
        Price price = new Price(money, Price.Unit.MONEY);
        dialogue.addNpcLine("You found " + price + "!",0);
        dialogue.addRunnable(m -> payoutReceiver.addBonusScoreFromVault(money, message, contextAtVaultSpawn));

        if (money >= BankCommandHandler.MIN_VAULT_PAYOUT_TO_UNLOCK_MONOCLE &&
                !contextAtVaultSpawn.getGuild().getBank().isMonocleUnlocked(payoutReceiver.getId())) {
            dialogue.addSleep(2)
                    .addNpcLine("\uD83D\uDC0A: Whoa!", 2500)
                    .addNpcLine("\uD83D\uDC0A: That is a respectable amount of money!", 2500)
                    .addNpcLine("\uD83D\uDC0A: You know what - you almost seem like a somewhat worthy business partner for the CrocBank Inc.!", 3500)
                    .addNpcLine("\uD83D\uDC0A: In case you are interested in the highly regarded CrocBank Inc. sponsorship - ", 3000)
                    .addNpcLine("\uD83D\uDC0A: - I might be willing to sell you an exclusive " + SponsoredMonocle.NAME + "!", 3500)
                    .addNpcLine("\uD83D\uDC0A: You'd better check it out soon using `~shop` before I reconsider your suitability for the sponsorship...", 0)
                    .addRunnable(m -> contextAtVaultSpawn.getGuild().getBank().unlockMonocleFor(payoutReceiver.getId()));

        }
    }

    protected int draw_money() {
        int shift = 12;
        int mult = 100;
        double nu = 1.1;
        int ret;
        do {
            double rng = Math.random();
            ret = mult*((int) ((shift + 1)*Math.pow(1 - rng, (-1. / nu))) - shift);
        } while (ret < 0 || Integer.MAX_VALUE/2 < ret);
        return ret;
    }

    public int getLastDropAmount() {
        return lastDropAmount;
    }

    public boolean wasMonocleActiveAtLastDrop() {
        return wasMonocleActiveAtLastDrop;
    }
}
