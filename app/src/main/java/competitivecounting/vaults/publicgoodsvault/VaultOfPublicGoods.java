package competitivecounting.vaults.publicgoodsvault;

import com.google.common.base.Objects;
import competitivecounting.Counter;
import competitivecounting.CountingBot;
import competitivecounting.CountingContext;
import competitivecounting.CountingEmojis;
import competitivecounting.bank.Bank;
import competitivecounting.bank.BankCommandHandler;
import competitivecounting.dialogue.Dialogue;
import competitivecounting.dialogue.ParallelDialogElementsBuilder;
import competitivecounting.interactionhandlers.SlashCommandHandler;
import competitivecounting.items.CrocStonk;
import competitivecounting.items.equippables.GoodBadUgly;
import competitivecounting.vaults.CommunityVault;
import competitivecounting.vaults.RiddleDialogue;
import competitivecounting.vaults.Vault;
import competitivecounting.vaults.vaultDrops.BigMoneyDrop;
import competitivecounting.vaults.vaultDrops.ItemDrop;
import competitivecounting.vaults.vaultDrops.MoneyDrop;
import discord4j.core.object.entity.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class VaultOfPublicGoods extends Vault {
    public final static String NAME = "Vault of Public Goods";
    private final static String RIDDLE = "The key for this vault will be determined in {0} seconds. The initial suggestion for the key is {1}, "
            + "but everyone may suggest their own key using the command `/" + SlashCommandHandler.SUBMIT_KEY_COMMAND + "`. "
            + "In the end, the vault will be locked using the key that is closest to 2/3 of the average over all the submitted keys and the initially suggested"
            + " key. ";
    private final static int TOTAL_RIDDLE_TIME = 20;
    private final static long TIME_INTERVAL_COMMUNITY_COUNT = 20;
    public final static int BUY_IN = 10000;
    private final static int KEY_APPLICATION_FEE = 499,
            EXPLANATION_FEE = 999;

    private int currentStage = -1;
    private HashMap<String, Integer> crocCoinsPerPerson = new HashMap<>(),
            currentStageContributions = new HashMap<>();
    private final HashMap<String, PublicGoodsDocumentation> documentations = new HashMap<>();
    private int currentPot = 0;
    private final static double PUBLIC_GOODS_MULTIPLIER = 2. + 1e-9;
    public final static int CROC_COINS_PER_STAGE = 5,
            TOTAL_STAGES = 4,
            MONEY_PER_CROC_COIN = 400;
    private final static int MIN_CROC_FEE = 0, MAX_CROC_FEE = 3;
    private double THRESHOLD_PER_MAX_POT = 2. / 3.;
    private CountDownLatch latch = null;
    private int currentCrocFee = -1;
    private int threshold;
    public final static String SPONSOR_TAG = "-# Sponsored by: CrocBank Inc.";
    private Message stageMsg;
    private String stageStr;
    private Counter winner;
    private Bank bank;

    public VaultOfPublicGoods() {
        super(0, context -> {
            List<Counter> contributingCounters = getContributingCounters(context);
            if (contributingCounters.size() < 3) {
                return false;
            }
            for (Counter counter : contributingCounters) {
                if (counter.getScore() + counter.getScoreInBankAccount() < BUY_IN) {
                    return false;
                }
            }
            return true;
        });
        addLootToLootPool(new BigMoneyDrop(95));
        addLootToLootPool(new ItemDrop(5, CrocStonk.instance));

        addOnDropReceivedListener((counter, drop) -> {
            if (drop instanceof MoneyDrop) {
                if (documentations.containsKey(counter.getId())) {
                    documentations.get(counter.getId()).setVaultMoneyWin(((MoneyDrop) drop).getLastDropAmount(),
                            ((MoneyDrop) drop).wasMonocleActiveAtLastDrop());
                }
            }
        });
    }

    @Override
    public RiddleDialogue createRiddleDialogue(Message message, CountingContext context) {
        if (bank == null) {
            bank = CountingBot.getInstance().getGuilds().get(context.getCounter().getGuildId()).getBank();
        }
        RiddleDialogue riddleDialogue = new RiddleDialogue();
        Bank bank = CountingBot.getInstance().getGuilds().get(context.getCounter().getGuildId()).getBank();
        List<Counter> contributingCounters = getContributingCounters(context);
        threshold = (int) (THRESHOLD_PER_MAX_POT * contributingCounters.size() * CROC_COINS_PER_STAGE * TOTAL_STAGES);
        String byeMsg = "See you again at the next " + getVaultName() + "!";
        riddleDialogue
                .addOnCanceled(m -> cancelGame())
                .addRunnable(m -> {
                    System.out.println("Starting and resetting state");
                    resetState();
                    resetDocumentations(contributingCounters, threshold); // Do not clear documentations in reset(), as they should be available also after the vault has finished.

                })
                .addMaybeCancelRest(m -> {
                    System.out.println("Checking if bank is unlocked");
                    if (!bank.isUnlocked()) {
                        bankWrite(m, "The bank is currently locked. " + byeMsg);
                        return true;
                    }
                    return false;
                })
                .addMaybeCancelRest(m -> {
                    System.out.println("Checking if somehow not enough players are available");
                    if (contributingCounters.size() < 3) {
                        throw new IllegalStateException("Illegal state! There should be at least 3 contributing counters!");
                    }
                    return false;
                })
                .addRunnable(m -> new Dialogue()
                        .addNpcLine("Ladies and gentlemen!", 2500)
                        .addNpcLine("I am honoured to host this " + getVaultName() + " for you!\n" + SPONSOR_TAG, 2000)
                        .addNpcLine("Now, I will collect a buy-in of " + BUY_IN + " money each.", 2500)
                        .addNpcLine("Conveniently, the CrocBank Inc. provided me with direct access to your bank accounts, "
                                + "so do not worry if there is insufficient money in your purse!", 3500)
                        .setNpcLineConverter(BankCommandHandler::toCrocText)
                        .playBlocking(m))
                .addMaybeCancelRest(m -> {
                    System.out.println("Collecting money from all contributing counters");
                    if (!collectMoneyFromAllCounters(contributingCounters, bank, m)) {
                        BankCommandHandler.bankWrite(m, "I could not collect sufficient money from all contributing counters. " + byeMsg);
                        return true;
                    }
                    return false;
                }).addRunnable(m -> {
                    // READY CHECK
                    String youAreRegisteredText = getYouAreRegisteredText(contributingCounters);
                    ParallelDialogElementsBuilder parallelBuilder = new Dialogue()
                            .addNpcLine(youAreRegisteredText, 2000)
                            .addNpcLine("Please react with " +
                                    CountingEmojis.THUMBS_UP.asUnicodeEmoji().get().getRaw() + " if you are ready to play, or with" +
                                    CountingEmojis.RED_QUESTION_MARK.asUnicodeEmoji().get().getRaw() + " if you need a reminder of the rules!", 0)
                            .addEmojiReaction(CountingEmojis.THUMBS_UP)
                            .addEmojiReaction(CountingEmojis.RED_QUESTION_MARK)
                            .setNpcLineConverter(BankCommandHandler::toCrocText)
                            .initializeParallelDialogElements();
                    for (Counter counter : contributingCounters) {
                        parallelBuilder.addWaitForEmojiReaction(CountingEmojis.THUMBS_UP, false,
                                msg -> {
                                },
                                new AtomicReference<>(counter.getId()), ParallelDialogElementsBuilder.ParallelDialogElementType.NECESSARY);
                        parallelBuilder.addWaitForEmojiReaction(CountingEmojis.RED_QUESTION_MARK, false,
                                msg -> {
                                    int BASE_READ_TIME = 2500;
                                    int LONGER_READ_TIME = 3500;
                                    new Dialogue()  // explain rules
                                            .addNpcLine("Ok, " + counter.getPing() + ", here are the rules of the " + getVaultName() +
                                                    ":\n" + SPONSOR_TAG, BASE_READ_TIME)
                                            .addNpcLine("The one who has the most Croc Coins by the end of the game will receive the key to this " + getVaultName() + "!\n" + SPONSOR_TAG, LONGER_READ_TIME)
                                            .addNpcLine("Watch out though: In case of a tie, no one will receive the key.", BASE_READ_TIME)
                                            .addNpcLine("The Croc Coin (CC) is a temporary currency, which is only used in the " + getVaultName() + ".\n" + SPONSOR_TAG, BASE_READ_TIME)
                                            .addNpcLine("During the game, you can use `/croccoins` at any time to discreetly display your current score.", BASE_READ_TIME)
                                            .addNpcLine("For your buy-in of " + BUY_IN + " money, you will receive 20 CC throughout the game.", BASE_READ_TIME)
                                            .addNpcLine("With the correct decisions, huge profits are possible by the end of the game!", BASE_READ_TIME)
                                            .addNpcLine("Whatever fortune you’ve amassed by then can be exchanged back at the fair rate of "
                                                    + MONEY_PER_CROC_COIN + " money per CC -", BASE_READ_TIME)
                                            .addNpcLine(" - no matter whether you won the game or not.", BASE_READ_TIME)
                                            .addNpcLine("Now, how to actually play?", BASE_READ_TIME)
                                            .addNpcLine("The game consists of " + TOTAL_STAGES + " identical stages.", BASE_READ_TIME)
                                            .addNpcLine("In each stage, each counter receives "
                                                    + CROC_COINS_PER_STAGE + " CC.", BASE_READ_TIME)
                                            .addNpcLine("An arbitrary number of these can be kept in your private stash.", BASE_READ_TIME)
                                            .addNpcLine("There, they will be safe until the end of the game and will contribute to your final score.", LONGER_READ_TIME)
                                            .addNpcLine("Alternatively, you can contribute some of your CCs to fund a joint lawsuit against the CrocBank. Inc.!", LONGER_READ_TIME)
                                            .addNpcLine("For this purpose, `/contributecroccoins <0-" + CROC_COINS_PER_STAGE + ">` can be used once per stage.", LONGER_READ_TIME)
                                            .addNpcLine("Write `/contributecroccoins 0` if you do not wish to contribute any Croc Coins in this stage.", BASE_READ_TIME)
                                            .addNpcLine("Each individual contribution is hidden from the other players -", BASE_READ_TIME)
                                            .addNpcLine(" - but after each stage, the cumulative number of contributed CCs is revealed.", BASE_READ_TIME)
                                            .addNpcLine("If by the end of the game, at least " + threshold + " CC have been contributed, the lawsuit will succeed.", LONGER_READ_TIME)
                                            .addNpcLine("The CrocBank Inc. will then be legally obliged to reimburse the court cost and even add a compensation payment!", LONGER_READ_TIME)
                                            .addNpcLine("In concrete terms, the CrocBank Inc. will take a sum worth twice the contributed number of CCs - ", LONGER_READ_TIME)
                                            .addNpcLine(" - and distribute it equally among the counters.", BASE_READ_TIME)
                                            .addNpcLine("If, however, the funding goal of " + threshold + " CC is not reached by the end of the game - ", BASE_READ_TIME)
                                            .addNpcLine(" - all the funding will be spent for unsuccessful court trials against the excellent lawyers of the CrocBank Inc. - ", LONGER_READ_TIME)
                                            .addNpcLine(" - i.e., all the contributions will be gone.", BASE_READ_TIME)
                                            .addNpcLine("As a last hint: In the beginning of each game, I will calculate an appropriate Croc-Fee ("
                                                    + MIN_CROC_FEE + "-" + MAX_CROC_FEE + " CC) based on latest macro- and microeconomic data.", LONGER_READ_TIME)
                                            .addNpcLine("This value will be fixed over all the stages - ", BASE_READ_TIME)
                                            .addNpcLine(" - and will be taken from the funding pot in each stage before revealing the total contributions.", LONGER_READ_TIME)
                                            .addNpcLine("If anything was unclear, feel free to request this explanation again at the next " + getVaultName() + "!\n" +
                                                    SPONSOR_TAG, LONGER_READ_TIME)
                                            .setNpcLineConverter(BankCommandHandler::toCrocText)
                                            .playBlocking(msg);
                                    fee(counter, EXPLANATION_FEE, msg,
                                            "Customer consultation regarding the " + getVaultName() + ".\n" + SPONSOR_TAG);
                                }, new AtomicReference<>(counter.getId()), ParallelDialogElementsBuilder.ParallelDialogElementType.SUFFICIENT);
                    }
                    parallelBuilder.finishParallelDialogElementsAndAdd(60, msg -> {
                        new Dialogue().addNpcLine("It is with great regret that I have to inform you that the "
                                        + getVaultName() + " was canceled, as not all participants are ready to play.\n" + SPONSOR_TAG, 2500)
                                .addNpcLine("Your buy-ins of " + BUY_IN + " money will be regarded as a compensation for engaging my moderation services.", 1500)
                                .addNpcLine("See you again at the next " + getVaultName() + "!\n" + SPONSOR_TAG, 0)
                                .setNpcLineConverter(BankCommandHandler::toCrocText)
                                .playBlocking(msg);
                        riddleDialogue.cancelAllRemaining();
                        return true;
                    }).playBlocking(m);
                }).addRunnable(m -> {
                    // SETUP GAME
                    resetState();
                    winner = null;
                    currentStage = 0;
                    currentCrocFee = randomInt(MIN_CROC_FEE, MAX_CROC_FEE + 1);
                    System.out.println("Current croc fee: " + currentCrocFee);
                    new Dialogue()
                            .addNpcLine("Within " + TOTAL_STAGES + " stages, gather at least "
                                    + threshold + " Croc Coins for a joint lawsuit against the CrocBank Inc. -", 2000)
                            .addNpcLine(" - or keep all the coins for yourselves. That's up to you!", 2000)
                            .addNpcLine("In each stage, use `/contributecroccoins <0-" + CROC_COINS_PER_STAGE + ">` to specify the number of CCs to contribute.", 2000)
                            .addNpcLine("Use `/croccoins` to check your privately stashed CCs.", 2000)
                            .addNpcLine("By now, I have calculated a fair Croc-Fee for this game, which will be fixed over all the stages.", 2000)
                            .addNpcLine("That being said - may the game commence!", 2000)
                            .setNpcLineConverter(BankCommandHandler::toCrocText)
                            .playBlocking(m);
                }).addRunnable(m -> {
                    // Do game
                    currentPot = 0;
                    int lastStagePot = 0;
                    currentStage = 1;
                    while (currentStage <= TOTAL_STAGES) {
                        stageStr = "# Stage " + currentStage + "/" + TOTAL_STAGES + ":\n";
                        if (currentStage >= 2) {
                            stageStr += "Last stage's funding: **" + (currentPot - lastStagePot) + " CC**\n";
                        }
                        lastStagePot = currentPot;
                        if (currentStage >= 3) {
                            stageStr += "Total funding so far: **" + currentPot + "/" + threshold + " CC**\n";
                        }
                        stageStr += "\nYou have received " + CROC_COINS_PER_STAGE + " CC.\n" +
                                "Please decide how many CCs to contribute!\n" +
                                "-# Contributions received: {0}/" + contributingCounters.size();
                        stageMsg = CountingBot.writeBlocking(m, stageStr.replace("{0}", "0"));

                        currentStage++;
                        currentStageContributions.clear();
                        contributingCounters.forEach(c -> {
                            addCrocCoins(c.getId(), CROC_COINS_PER_STAGE);
                        });
                        latch = new CountDownLatch(contributingCounters.size());
                        try {
                            latch.await(10, TimeUnit.MINUTES);
                        } catch (InterruptedException e) {
                            System.out.println("Canceled wait for user contributions");
                            riddleDialogue.cancelAllRemaining();
                        } finally {
                            if (latch.getCount() != 0) {
                                contributingCounters.forEach(c -> {
                                    if (!currentStageContributions.containsKey(c.getId())) {
                                        contributeCrocCoins(c.getId(), CROC_COINS_PER_STAGE);
                                        bankWrite(message, "As " + c.getName() + " did not contribute in time, their full contribution of "
                                                + CROC_COINS_PER_STAGE + " Croc Coins was automatically added to the pot.");
                                    }
                                });
                            }
                        }
                        if (latch.getCount() != 0) {
                            throw new IllegalStateException("Illegal state! Even on timeout, all counters need to have contributed.");
                        }
                        System.out.println("Stage " + currentStage + " finished. Total pot: " + currentPot);

                        // Post-Stage
                        currentPot = Math.max(0, currentPot - currentCrocFee);
                        System.out.println("Pot after croc fee: " + currentPot);
                        documentations.forEach((key, docs) -> docs.addPotAfterStage(currentPot));
                    }

                }).addRunnable(m -> {
                    // Cashout
                    Dialogue d = new Dialogue()
                            .addNpcLine("Ladies and gentlemen!", 2000)
                            .addNpcLine("Another thrilling " + getVaultName() + " has come to an end!\n" + SPONSOR_TAG, 2500)
                            .addNpcLine("For funding a joint lawsuit against the CrocBank Inc., a remarkable sum of...", 3000)
                            .addNpcLine("... " + currentPot + " Croc Coins could be gathered...", 2500);
                    if (currentPot >= threshold) {
                        int addCrocCoinsPerPerson = (int) (PUBLIC_GOODS_MULTIPLIER * currentPot / contributingCounters.size());

                        if (currentPot == threshold) {
                            d.addNpcLine("... which is exactly the required sum for beating the CrocBank Inc.'s excellent legal defense!", 2500);
                        } else {
                            d.addNpcLine("... which is " + (currentPot - threshold) + " CC more than needed to beat the CrocBank Inc.'s excellent legal defense!", 2500);
                        }
                        d.addNpcLine("Therefore, the the CrocBank Inc. is legally obligued to distribute twice the total funding among the participants!", 3500)
                                .addNpcLine("In concrete terms, each participant receives " + addCrocCoinsPerPerson + " CC on top of their private stash for the final payout!", 2500)
                                .addRunnable(msg -> contributingCounters.forEach(c -> {
                                    addCrocCoins(c.getId(), addCrocCoinsPerPerson);
                                    documentations.get(c.getId()).setPotPayout(addCrocCoinsPerPerson, crocCoinsPerPerson.get(c.getId()));
                                }));
                    } else {
                        d.addNpcLine("... which is unfortunately " + (threshold - currentPot) + " CC less than needed to beat the CrocBank Inc. in court!", 2500)
                                .addNpcLine("Try to improve your cooperation if you want to profit from the next " + getVaultName() + "!\n" + SPONSOR_TAG, 2500)
                                .addRunnable(msg -> contributingCounters.forEach(c -> {
                                    documentations.get(c.getId()).setPotPayout(0, crocCoinsPerPerson.get(c.getId()));
                                }));
                    }
                    d.addNpcLine("Your final Croc Coin scores are now being converted to money at a fair rate of " + MONEY_PER_CROC_COIN + " money per CC - ", 2500)
                            .addRunnable(this::cashout)
                            .addNpcLine(" - and have been discreetly transferred to your bank accounts.", 3500)
                            .addNpcLine("Now, let's see who wins the key for this " + getVaultName() + "...\n" + SPONSOR_TAG, 2500)
                            .setNpcLineConverter(BankCommandHandler::toCrocText)
                            .playBlocking(m);
                }).addRunnable(m -> {
                    // vault
                    List<Counter> winners = new ArrayList<>();
                    winners.add(contributingCounters.get(0));
                    for (Counter counter : contributingCounters) {
                        if (counter.getId().equals(winners.get(0).getId())) {
                            continue;
                        }
                        if (crocCoinsPerPerson.get(counter.getId()) > crocCoinsPerPerson.get(winners.get(0).getId())) {
                            winners.clear();
                            winners.add(counter);
                        } else if (Objects.equal(crocCoinsPerPerson.get(counter.getId()), crocCoinsPerPerson.get(winners.get(0).getId()))) {
                            winners.add(counter);
                        }
                    }
                    if (winners.size() != 1) {
                        winner = null;
                        new Dialogue()
                                .addNpcLine("Oh no, " + winners.size() + " participants are tied for the highest Croc Coin score!", 2000)
                                .addNpcLine("Therefore, bureaucratic constraints prevent me from handing out the key to anyone - ", 2000)
                                .addNpcLine(" - and the " + getVaultName() + " will remain locked.\n" + SPONSOR_TAG, 2000)
                                .addNpcLine("Try to play better next time!", 2000)
                                .setNpcLineConverter(BankCommandHandler::toCrocText)
                                .playBlocking(m);
                        return;
                    }
                    winner = winners.get(0);
                    riddleDialogue.setRiddleSolverId(winner.getId());
                    new Dialogue()
                            .addNpcLine("Good news! The winner could be identified.", 2000)
                            .addNpcLine("If you think that the lucky winner could be you -", 2000)
                            .addNpcLine(" - send an application for the vault key to the CrocBank Inc. using `~1050505`", 4000)
                            .addRunnable(msg -> CountingBot.write(msg, "-# Note: " + KEY_APPLICATION_FEE + " money per application"))
                            .setNpcLineConverter(BankCommandHandler::toCrocText)
                            .play(m);


                })
                .addMaybeCancelRest(m -> winner == null);
        riddleDialogue.addWaitForCorrectSolutionAndSetWinningUserRef((msg, answer) -> {
            if (answer != 1050505 || msg.getAuthor().isEmpty() || msg.getGuildId().isEmpty()) {
                return false;
            }
            Counter applicant = CountingBot.getCounter(msg.getGuildId().get().asString(), msg.getAuthor().get().getId().asString());
            fee(applicant, KEY_APPLICATION_FEE, msg, "Application for the vault key of the " + getVaultName() + ".\n" + SPONSOR_TAG);
            return Objects.equal(winner.getId(), msg.getAuthor().get().getId().asString());
        }).addWaitForKeyReaction("\uD83D\uDC0A: See you again at the next " + getVaultName() + "! Don't forget to check your total profit using `/crocdocs`.\n" + SPONSOR_TAG)
                .addRunnable(m -> documentations.forEach((key, docs) -> docs.setFinished()));
        return riddleDialogue;
    }

    private void cancelGame() {
        documentations.forEach((key, docs) ->  docs.setCanceled());
    }

    private void fee(Counter counter, int fee, Message message, String reason) {
        if (fee > 0) {
            int reducedFee = BankCommandHandler.chargeFee(counter, fee, reason, message);
            if (reducedFee > 0) {
                documentations.get(counter.getId()).addFee(fee, reason);
            }
        }
    }

    private void cashout(Message message) {
        for (var entry : crocCoinsPerPerson.entrySet()) {
            String userId = entry.getKey();
            int crocCoins = entry.getValue();
            int moneyToAdd = crocCoins * MONEY_PER_CROC_COIN;
            Counter counter = CountingBot.getCounter(bank.getGuildId(), userId);
            counter.addToBankOrToScoreIfFull(moneyToAdd, bank, message);
            documentations.get(userId).setCashout(moneyToAdd);
            System.out.println("Counter " + counter.getName() + " cashed out " + crocCoins + " CC for " + moneyToAdd + " money.");
        }
    }

    private String getYouAreRegisteredText(List<Counter> contributingCounters) {
        StringBuilder b = new StringBuilder("Congratulations, ");
        for (int i = 0; i < contributingCounters.size(); i++) {
            b.append(contributingCounters.get(i).getPing());
            if (i == contributingCounters.size() - 2) {
                b.append(" and ");
            } else if (i <= contributingCounters.size() - 3) {
                b.append(", ");
            }
        }
        b.append("! You are now officially participating in this ")
                .append(getVaultName())
                .append("!\n")
                .append(SPONSOR_TAG);
        return b.toString();
    }

    private void addCrocCoins(String userId, int amount) {
        crocCoinsPerPerson.put(userId, crocCoinsPerPerson.getOrDefault(userId, 0) + amount);
    }

    private void removeCrocCoins(String userId, int amount) {
        crocCoinsPerPerson.put(userId, crocCoinsPerPerson.getOrDefault(userId, 0) - amount);
    }

    private static List<Counter> getContributingCounters(CountingContext context) {
        long contextCreationTime = context.getCreationTime() / 1000;
        List<Counter> contributingCounters = new ArrayList<>();
        contributingCounters.add(context.getCounter());
        for (var entry : context.getStreak().getLastCountingTimesPerCounter().entrySet()) {
            String counterId = entry.getKey();
            long lastCountingSecond = entry.getValue();
            if (counterId.equals(context.getCounter().getId())) {
                continue;
            }
            if (contextCreationTime - lastCountingSecond <= TIME_INTERVAL_COMMUNITY_COUNT) {
                contributingCounters.add(CountingBot.getCounter(context.getCounter().getGuildId(), counterId));
            }
        }
        return contributingCounters;
    }

    private void resetDocumentations(List<Counter> contributingCounters, int threshold) {
        documentations.clear();
        for (Counter counter : contributingCounters) {
            documentations.put(counter.getId(), new PublicGoodsDocumentation(contributingCounters.size(), threshold));
        }
        System.out.println("Documentations reset for contributing counters.");
    }

    public void contributeCrocCoins(String userId, int contributedCrocCoins) {
        currentStageContributions.put(userId, contributedCrocCoins);
        documentations.get(userId).contributeCrocCoins(currentStage, contributedCrocCoins, CROC_COINS_PER_STAGE);
        removeCrocCoins(userId, contributedCrocCoins);
        currentPot += contributedCrocCoins;
        stageMsg.edit(spec -> spec.setContent(stageStr.replace("{0}",
                String.valueOf(currentStageContributions.size())))).subscribe();
        latch.countDown();

        System.out.println("User " + userId + " contributed " + contributedCrocCoins + " croc coins.");
        System.out.println("Current pot: " + currentPot);
    }

    private boolean collectMoneyFromAllCounters(List<Counter> contributingCounters, Bank bank, Message message) {
        int totalBankPlus = 0;
        for (Counter counter : contributingCounters) {
            int totalMoney = counter.getScore() + counter.getScoreInBankAccount();
            if (totalMoney < BUY_IN) {
                return false;
            }
        }
        for (Counter counter : contributingCounters) {
            int moneyToTakeFromCounter = Math.min(BUY_IN, counter.getScore());
            counter.subtractScore(moneyToTakeFromCounter);
            totalBankPlus += moneyToTakeFromCounter;
            int moneyToTakeFromBank = BUY_IN - moneyToTakeFromCounter;
            if (moneyToTakeFromBank > 0) {
                bank.getAccount(counter.getId()).withdraw(moneyToTakeFromBank);
                totalBankPlus += moneyToTakeFromBank;
            }
            documentations.get(counter.getId()).setBuyIn(BUY_IN);
        }
        bank.addMoney(totalBankPlus);
        return true;
    }

    public boolean hasAlreadyContributed(String userId) {
        return currentStageContributions.containsKey(userId);
    }

    @Override
    public String getVaultName() {
        return NAME;
    }

    private void resetState() {
        currentStage = -1;
        crocCoinsPerPerson.clear();
        currentStageContributions.clear();
        currentPot = 0;
    }

    @Override
    public void reset() {
        super.reset();
        resetState();
    }


    @Override
    public String getSpawnConditionsDescription() {
        return "Replaces the " + CommunityVault.NAME + " if the counter has an active CrocBank Inc. sponsorship "
                + "and all the participating counters have at least " + VaultOfPublicGoods.BUY_IN +" money available.";
    }

    private static void bankWrite(Message m, String content) {
        BankCommandHandler.bankWrite(m, content);
    }

    public HashMap<String, Integer> getCrocCoinsPerPerson() {
        return crocCoinsPerPerson;
    }

    public boolean isGameActive() {
        return currentStage >= 0;
    }

    public int getCurrentStage() {
        return currentStage;
    }


    public PublicGoodsDocumentation getCrocDocsForUser(String userId) {
        return documentations.get(userId);
    }
}

