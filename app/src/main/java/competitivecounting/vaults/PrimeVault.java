package competitivecounting.vaults;

import competitivecounting.CountingBot;
import competitivecounting.CountingContext;
import competitivecounting.CountingEmojis;
import competitivecounting.items.equippables.CoinMiner;
import competitivecounting.vaults.vaultDrops.ItemDrop;
import competitivecounting.vaults.vaultDrops.MoneyDrop;
import discord4j.core.object.entity.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrimeVault extends Vault {
    private final static String GREEN = ":green_square:", YELLOW = ":yellow_square:", WHITE = ":white_large_square:";
    private final static double SPAWN_CHANCE = 0.02;
    private final static String RIDDLE = "Guess the correct 4-digit prime number! For each guess, you will be told which digit is " +
            "at the correct spot (" + GREEN + "), at the wrong spot (" + YELLOW + ") or wrong (" + WHITE + "). Only 4-digit primes will be accepted as guesses.";

    private final static int amountOfPrimes = 1061;
    private final static int[] primes = new int[amountOfPrimes];

    static {
        int counter = 0;
        for (int i = 1000; i <= 9999; i++) {
            if (isPrime(i)) {
                primes[counter] = i;
                counter++;
            }
        }
    }

    public PrimeVault() {
        super(SPAWN_CHANCE, context -> isPrime(context.getCurrentNumber()));
        addLootToLootPool(new MoneyDrop(95));
        addLootToLootPool(new ItemDrop(5, new CoinMiner(null)));
    }

    public static boolean isPrime(int n) {
        if (n < 2) return false;
        if (n == 2 || n == 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;

        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public RiddleDialogue createRiddleDialogue(Message message, CountingContext context) {
        int idx = randomInt(0, amountOfPrimes);
        int prime = primes[idx];
        String primeAsString = String.valueOf(prime);
        char[] chars = primeAsString.toCharArray();
        List<Character> characterList = new ArrayList<>();
        for (char c : chars) {
            characterList.add(c);
        }
        String riddle = RIDDLE;
        String wholeRiddleText = getRiddleText(riddle, context.getCounter().getName());
        RiddleDialogue riddleDialogue = new RiddleDialogue();
        riddleDialogue.addNpcLine(wholeRiddleText, 0);
        riddleDialogue.addWaitForCorrectSolutionAndSetWinningUserRef((msg, answer) -> {
            if (answer == prime && msg.getAuthor().isPresent()) {
                return true;
            }
            String submittedNumAsString = String.valueOf(answer);
            if (submittedNumAsString.length() != 4 || !isPrime(answer)) {
                msg.addReaction(CountingEmojis.X).block();
                return false;
            }
            String[] feedback = new String[]{null, null, null, null};
            char[] submittedChars = submittedNumAsString.toCharArray();
            List<Character> lettersToAward = new ArrayList<>(characterList);
            // Check green
            for (int i = 0; i < 4; i++) {
                if (characterList.get(i) == submittedChars[i]) {
                    feedback[i] = GREEN;
                    lettersToAward.remove(characterList.get(i));
                }
            }
            for (int i = 0; i < 4; i++) {
                if (feedback[i] != null) {
                    continue;
                }
                if (lettersToAward.contains(submittedChars[i])) {
                    feedback[i] = YELLOW;
                    lettersToAward.remove(lettersToAward.indexOf(submittedChars[i]));
                } else {
                    feedback[i] = WHITE;
                }
            }
            CountingBot.respond(msg, String.join("", feedback));
            // Check
            return false;
        });
        return riddleDialogue.addWaitForKeyReaction();
    }

    @Override
    protected String getVaultName() {
        return "Vault of Primes";
    }

    @Override
    public String getSpawnConditionsDescription() {
        return "Spawns on prime numbers.";
    }
}
