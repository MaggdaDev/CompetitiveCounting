package competitivecounting.vaults.vaultDrops;

public class BigMoneyDrop extends MoneyDrop {
    public BigMoneyDrop(double weight) {
        super(weight);
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
