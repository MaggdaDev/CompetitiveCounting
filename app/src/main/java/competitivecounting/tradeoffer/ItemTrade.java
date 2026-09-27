package competitivecounting.tradeoffer;

public class ItemTrade extends Tradable {
    private final String itemIdentifier;
    private final int amount;
    public final static String AMOUNT_MULTIPLIER = "*";

    public ItemTrade(String string) throws TradeArgumentException {
        String cleanedString = string.replaceAll(" ", "");
        if (string.contains(AMOUNT_MULTIPLIER)) {
            String[] parts = cleanedString.split("\\" + AMOUNT_MULTIPLIER);
            if (parts.length != 2) {
                throw new IllegalStateException("Invalid item trade string: " + string);
            }
            int amount;
            String itemName;
            try {
                amount = Integer.parseInt(parts[1]);
                itemName = parts[0];
                if (amount <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                try {
                    amount = Integer.parseInt(parts[0]);
                    itemName = parts[1];
                    if (amount <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    throw new TradeArgumentException("Invalid item amount! Syntax: `ITEM:<item_name>*<amount>` or `ITEM:<amount>*<item_name>`.");
                }
            }
            this.itemIdentifier = itemName;
            this.amount = amount;
        } else {
            this.itemIdentifier = cleanedString;
            this.amount = 1;
        }
    }

    public String getItemIdentifier() {
        return itemIdentifier;
    }

    public int getAmount() {
        return amount;
    }
}
