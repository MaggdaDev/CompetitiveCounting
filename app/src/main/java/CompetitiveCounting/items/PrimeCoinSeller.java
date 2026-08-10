package CompetitiveCounting.items;

import CompetitiveCounting.Counter;
import CompetitiveCounting.CountingBot;
import CompetitiveCounting.CountingEmojis;
import CompetitiveCounting.dialogue.Dialogue;
import CompetitiveCounting.dialogue.DialogueElement;
import CompetitiveCounting.dialogue.ParallelDialogElementsBuilder;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicReference;

public class PrimeCoinSeller {
    private final static String url = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd";
    private final HttpClient client;
    private final HttpRequest request;
    private int currentBitCoinPrice = -1;
    private long lastPriceUpdateMillis = 0;
    private long priceRefreshCooldownMillis = 60 * 1000;

    public PrimeCoinSeller() {
        client = HttpClient.newHttpClient();
        request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
    }

    public void sellRequested(Message message, Counter counter) {
        if (System.currentTimeMillis() - lastPriceUpdateMillis > priceRefreshCooldownMillis) {
            int newPrice = getBitcoinPrice();
            if (newPrice > 0) {
                currentBitCoinPrice = newPrice;
                lastPriceUpdateMillis = System.currentTimeMillis();
            }
        }
        int localCurrentBitCoinPrice = currentBitCoinPrice;
        if (localCurrentBitCoinPrice < 0) {
            CountingBot.write(message, "Selling " + Consumables.PRIME_COIN.getName() + "s is currently unavailable, please try again later.");
            return;
        }
        new Dialogue().addNpcLine("Do you want to sell one " + Consumables.PRIME_COIN.getName() + " for " + localCurrentBitCoinPrice + " money?", 0)
                .addEmojiReaction(CountingEmojis.THUMBS_UP)
                .addEmojiReaction(CountingEmojis.THUMBS_DOWN)
                .initializeParallelDialogElements()
                .addWaitForEmojiReaction(CountingEmojis.THUMBS_UP, false, m -> {}, new AtomicReference<>(counter.getId()),
                        ParallelDialogElementsBuilder.ParallelDialogElementType.SUFFICIENT)
                .addWaitForEmojiReaction(CountingEmojis.THUMBS_DOWN, true, m -> {
                    CountingBot.write(message, Consumables.PRIME_COIN.getName() + " selling cancelled.");
                        }, new AtomicReference<>(counter.getId()),
                        ParallelDialogElementsBuilder.ParallelDialogElementType.SUFFICIENT)
                .finishParallelDialogElementsAndAdd()
                .addRunnable(m -> {
                    if (counter.getInventory().getAmountOfItem(Consumables.PRIME_COIN) < 1) {
                        CountingBot.write(m, "Can't sell " + Consumables.PRIME_COIN.getName() + ", as you know longer own one.");
                    } else {
                        counter.getInventory().removeItem(Consumables.PRIME_COIN);
                        counter.addBonusScore(localCurrentBitCoinPrice, m);
                        CountingBot.write(m, "You sold a " + Consumables.PRIME_COIN.getName() + " for - wow! - " + localCurrentBitCoinPrice + " money!");
                    }
                }).play(message);
    }

    private int getBitcoinPrice() {
        try {
            String response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            ).body();
            String price = response
                    .replaceAll(".*\"usd\":([0-9.]+).*", "$1");
            return Integer.parseInt(price);
        } catch (Exception e) {
            System.err.println("Exception while getting bitcoin price: " + e);
            return -1;
        }
    }
}
