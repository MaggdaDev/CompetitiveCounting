package CompetitiveCounting.tradeoffer;

import CompetitiveCounting.Counter;
import CompetitiveCounting.CountingBot;
import CompetitiveCounting.Util;

public class TradeHandler {
    public final static String YOU_GET = "YOU_GET:";
    public final static String I_GET = "I_GET:";
    private static String userId, userPing;
    private static Tradable[] youGetTrades, iGetTrades;
    private static Counter initCounter, requCounter;


    public static TradeOffer parse(String content, Counter initCounter) {
        String[] splitted;
        boolean containsYouGet = true;
        if (content.contains(YOU_GET)) {
            splitted = content.split(YOU_GET);
        } else {
            splitted = content.split(I_GET);
            containsYouGet = false;
        }
        String init = splitted[0];
        String offers = splitted[1];

        String youGet, iGet;
        if (containsYouGet) {
            splitted = offers.split(I_GET);
            youGet = splitted[0];
            if (splitted.length > 1) {
                iGet = splitted[1];
            } else {
                iGet = "";
            }
        } else {
            youGet = "";
            iGet = offers;
        }
        splitted = init.split(" ");
        userPing = splitted[1].replaceAll(" ", "");
        userId = Util.pingToUserId(userPing);
        requCounter = CountingBot.getInstance().getCounter(initCounter.getGuildId(), userId);

        try {
            youGetTrades = Tradable.generateTradables(youGet, initCounter, requCounter);
            iGetTrades = Tradable.generateTradables(iGet, requCounter, initCounter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new TradeOffer(initCounter, requCounter, iGetTrades, youGetTrades);
    }
}
