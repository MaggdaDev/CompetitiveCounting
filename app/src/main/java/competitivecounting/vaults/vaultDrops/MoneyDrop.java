package competitivecounting.vaults.vaultDrops;

import competitivecounting.Counter;
import competitivecounting.Price;
import competitivecounting.dialogue.Dialogue;
import discord4j.core.object.entity.Message;

public class MoneyDrop extends VaultDrop {

    public MoneyDrop(double weight) {
        super(weight);
    }

    @Override
    public void payout(Message message, Dialogue dialogue, Counter counter) {
        int money = draw_money();
        if (money < 2000) {
            //
        } else if (money < 20000 ) {
            dialogue.addNpcLine("Nice!", 1000);
        } else if (money < 70000) {
            dialogue.addNpcLine("Whoa!", 1500);
        } else if (money < 200000) {
            dialogue.addNpcLine("Is there a zero too much??", 1500);
        } else if(money < 1000000) {
            dialogue.addNpcLine("This must be a bug! How does so much money even fit into one vault??", 2500);
        } else {
            dialogue.addNpcLine("We should inform the police about this find...", 1500)
                    .addNpcLine("There is no way so much money got into this vault by legal means...", 1500);
        }
        Price price = new Price(money, Price.Unit.MONEY);
        dialogue.addNpcLine("You found " + price + "!",0);
        dialogue.addRunnable(m -> counter.addBonusScore(money, message));
    }

    private int draw_money() {
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


}
