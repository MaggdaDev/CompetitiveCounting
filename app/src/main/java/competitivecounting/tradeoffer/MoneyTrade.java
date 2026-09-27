package competitivecounting.tradeoffer;

public class MoneyTrade extends Tradable {
    private final int amount;
    public MoneyTrade(String string) {
        String cleanedString = string.replaceAll(" ", "");
        int parsedAmount;
        try {
            parsedAmount = Integer.parseInt(cleanedString);
        } catch (NumberFormatException e) {
            if (cleanedString.startsWith("-")) {
                parsedAmount = Integer.MIN_VALUE;
            } else {
                parsedAmount = Integer.MAX_VALUE;
            }
        }
        amount = parsedAmount;
    }

    public int getAmount() {
        return amount;
    }
}
