package CompetitiveCounting.bank;

import CompetitiveCounting.bank.exceptions.BankTransactionException;
import CompetitiveCounting.contracts.Contract;
import CompetitiveCounting.contracts.ContractHandler;
import CompetitiveCounting.contracts.ContractOwner;
import CompetitiveCounting.items.Inventory;
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
    private HashMap<String, Integer> upgrades;
    private List<Contract> contracts;
    private transient List<Contract> incomingContracts;

    private transient ContractHandler contractHandler;

    public Bank(String guildId) {
        this.guildId = guildId;
        this.totalScore = 1050505;
        this.accounts = new HashMap<>();
        this.upgrades = new HashMap<>();
        for (String key: UPGRADE_KEYS) {
            this.upgrades.put(key, 0);
        }
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
    }

    public void donate(int amount) {
        totalScore += amount;
    }

    public void removeMoney(int amount) {
        // should ideally only be used from within the loan function. removes money because it gives it to the user.
        // i'm not using withdraw since that would subtract from the user's BankAccount
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

    public void deposit(String counterId, int amount) {
        totalScore += amount;
        accounts.get(counterId).deposit(amount - DEPOSIT_COST);
    }

    public int getTotalScore() { return this.totalScore; }

    public int getBalance(String counterId) throws BankTransactionException {
        return accounts.get(counterId).getBalance();
    }

    public void withdraw(String counterId, int withdrawAmount) {
        accounts.get(counterId).withdraw(withdrawAmount);
        totalScore -= withdrawAmount;
    }

    public void getUpgrades() {
        System.out.println(upgrades);
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
}