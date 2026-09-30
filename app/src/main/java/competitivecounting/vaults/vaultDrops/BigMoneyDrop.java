package competitivecounting.vaults.vaultDrops;

import competitivecounting.vaults.Vault;

public class BigMoneyDrop extends MoneyDrop {
    public BigMoneyDrop(double weight, Vault vault) {
        super(weight, vault);
    }

    @Override
    protected int draw_money() {
        int draw;
        do {
            draw = super.draw_money();
        } while (draw < 2000);
        return draw;
    }
}
