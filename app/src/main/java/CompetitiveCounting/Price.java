package CompetitiveCounting;

public class Price {
    private int price;
    private Unit unit;

    public Price(int price, Unit unit) {
        this.price = price;
        this.unit = unit;
    }
    public Price(int price) {
        this.price = price;
        this.unit = Unit.MONEY;
    }

    @Override
    public String toString() {
        String unitStr = "";
        switch (unit) {
            case MONEY:
                unitStr = "money";
                break;
            case PRESTIGE_POINTS:
                if (price == 1) {
                    unitStr = "prestige point";
                } else {
                    unitStr = "prestige points";
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown unit: " + unit);
        }
        return price + " " + unitStr;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public enum Unit {
        MONEY, PRESTIGE_POINTS;
    }
}
