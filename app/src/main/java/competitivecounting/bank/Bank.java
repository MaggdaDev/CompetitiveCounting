package competitivecounting.bank;

import competitivecounting.Counter;
import competitivecounting.CountingBot;
import competitivecounting.bank.exceptions.BankTransactionException;
import competitivecounting.contracts.Contract;
import competitivecounting.contracts.ContractHandler;
import competitivecounting.contracts.ContractOwner;
import competitivecounting.items.CrocStonk;
import discord4j.core.object.entity.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Bank implements ContractOwner {
    public static final String CONTRACT_ENTITY_NAME = "The CrocBank Inc.";
    public static final String CONTRACT_OWNER_ID = "-1";
    private boolean isUnlocked = false;
    public final static int DEPOSIT_COST = 1000;
    private String guildId;
    private int totalScore;
    private HashMap<String, BankAccount> accounts;

    private static final List<String> UPGRADE_KEYS = List.of("loan_formula", "max_loan", "max_balance", "placeholder_2");
    private List<Contract> contracts;
    private transient List<Contract> incomingContracts;

    private transient ContractHandler contractHandler;

    public Bank(String guildId) {
        this.guildId = guildId;
        this.totalScore = 1050505;
        this.accounts = new HashMap<>();
        init();
    }

    public void init() {
        if (contracts == null) {    // MUST BE BEFORE CONTRACT HANDLER
            contracts = new ArrayList<>();
        }
        if (contractHandler == null) {  // MUST BE AFTER CONTRACTS
            contractHandler = new ContractHandler(this);
        }
        if (incomingContracts == null) {
            incomingContracts = new ArrayList<>();
        }
        for (BankAccount account : accounts.values()) {
            account.init();
        }
    }

    public void addProfit(int forBank, Counter source, String reason, Message message) {
        int totalProfit = forBank;
        double percentPerCrocStock = CrocStonk.getBankOwnershipPerCrocStock(getTotalCrocStocksEmitted());
        for (Counter counter: CountingBot.getInstance().getGuilds().get(guildId).getCounters().values()) {
            BankAccount account = getAccount(counter.getId());
            double crocStocks = counter.getCrocStocks();
            int forUser = (int) (crocStocks * percentPerCrocStock * totalProfit);
            counter.addBonusScore(forUser, message);
            account.documentProfit(forUser, totalProfit, source, reason);
            forBank -= forUser;
        }
        System.out.println("After distributing profits to croc stock owners, " + forBank +
                " is left for the bank (initially " + totalProfit + ") for reason: " + reason +
                " from source: " + source.getName());
        addMoney(forBank);
    }

    public int getTotalCrocStocksEmitted() {
        int total = 0;
        for (Counter counter: CountingBot.getInstance().getGuilds().get(guildId).getCounters().values()) {
            total += counter.getCrocStocks();
        }
        return total;
    }

    public void addMoney(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Bank tries to add negative money: " + amount);
        }
        totalScore += amount;
    }

    public void removeMoney(int amount) {
        // should ideally only be used from within the loan function. removes money because it gives it to the user.
        // i'm not using withdraw since that would subtract from the user's BankAccount
        if (amount > totalScore) {
            throw new IllegalStateException("Bank tries to remove more money than it has: " + amount + " > " + totalScore);
        }
        totalScore -= amount;
    }

    public void register(String counterId) {
        if (!alreadyRegistered(counterId)) {
            BankAccount account = new BankAccount(counterId);
            accounts.put(counterId, account);
        }
    }

    @Override
    public String getGuildId() {
        return guildId;
    }

    @Override
    public String getName() {
        return Bank.CONTRACT_ENTITY_NAME;
    }

    @Override
    public String getId() {
        return CONTRACT_OWNER_ID;
    }

    @Override
    public void addBonusScoreFromContract(int pay, Message message) {
        totalScore += pay;
    }

    @Override
    public List<Contract> getContracts(){
        return contracts;
    }

    @Override
    public List<Contract> getIncomingContracts() {
        return incomingContracts;
    }

    @Override
    public String getPing() {
        return "CrocBank"; // scheis nich ob man das raucht
    }

    public boolean alreadyRegistered(String counterId) {
        return accounts.containsKey(counterId);
    }

    public void deposit(String counterId, int amount, Message message) {
        totalScore += amount;
        accounts.get(counterId).depositWithoutFeeOrAffectingTotalBankScore(amount);
        Counter user = CountingBot.getCounter(guildId, counterId);
        BankCommandHandler.chargeFee(user, DEPOSIT_COST, "Deposit fee for a deposit of " + amount + " money.", message);
    }

    public int getTotalScore() { return this.totalScore; }

    public int getBalance(String counterId) throws BankTransactionException {
        return accounts.get(counterId).getBalance();
    }

    public void withdraw(String counterId, int withdrawAmount) {
        accounts.get(counterId).withdraw(withdrawAmount);
        totalScore -= withdrawAmount;
    }

    public BankAccount getAccount(String counterId) {
        if (!alreadyRegistered(counterId)) {
            register(counterId);
        }
        return accounts.get(counterId);
    }


    public void unlock() {
        isUnlocked = true;
    }
    public boolean isUnlocked() {
        return isUnlocked;
    }

    public ContractHandler getContractHandler() {
        return contractHandler;
    }

    public boolean isMonocleUnlocked(String counterId) {
        if (!alreadyRegistered(counterId)) {
            return false;
        }
        return accounts.get(counterId).isMonocleUnlocked();
    }

    public void unlockMonocleFor(String id) {
        if (!alreadyRegistered(id)) {
            register(id);
        }
        accounts.get(id).setMonocleUnlocked(true);
        CountingBot.getInstance().save();
    }
}