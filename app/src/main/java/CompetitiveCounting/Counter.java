/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;

import CompetitiveCounting.bank.Bank;
import CompetitiveCounting.contracts.Contract;
import CompetitiveCounting.contracts.ContractHandler;
import CompetitiveCounting.contracts.ContractOwner;
import CompetitiveCounting.items.Inventory;
import CompetitiveCounting.items.Purchasable;
import CompetitiveCounting.items.ShopCommandHandler;
import CompetitiveCounting.items.StreakEnders;
import CompetitiveCounting.tradeoffer.TradeOffer;
import discord4j.core.object.entity.Message;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author DavidPrivat
 */
public class Counter implements ContractOwner {

    public final static int PRESTIGE_WORTH = 1000000;
    public final static double SYSTEM_OWNED_FACT = 1.5;
    public final static double MULT_PLUS_PER_PRESTIGE = 0.2;

    public final static double TROPHY_BONUS_MULT = 2;
    private final String key, name;
    private int score, prestiges, prestigePoints;

    private HashMap<String, Integer> currScoreAdds;
    private int[] unlocked;
    private int[] unlockedSystems;
    private BonusStreak[] bonusStreaks;
    private List<Contract> contracts;
    private transient List<Contract> incomingContracts;
    private ArrayList<Integer> ownedTrophies;
    private int trophyShards;
    private transient HashMap<String, TradeOffer> tradeOffers = new HashMap<String, TradeOffer>();
    private transient ContractHandler contractHandler;


    private transient String guildId;   // Will be set in initContracts method
    private Inventory inventory;

    public Counter(String guildId, String key, String name) {
        this.key = key;
        this.score = 0;
        this.name = name;
        this.unlocked = new int[]{};
        currScoreAdds = new HashMap<>();
        this.prestiges = 0;
        this.prestigePoints = 0;
        this.unlockedSystems = new int[]{};
        this.bonusStreaks = new BonusStreak[]{};
        inventory = new Inventory();
        init(guildId);
        contractHandler.initIncomingContracts(CountingBot.getInstance().getGuilds().get(guildId));
    }

    public void init(String guildId) {
        this.guildId = guildId;
        if (contracts == null) {    // MUST BE BEFORE CONTRACT HANDLER
            contracts = new ArrayList<>();
        }
        if (contractHandler == null) {  // MUST BE AFTER CONTRACTS
            contractHandler = new ContractHandler(this);
        }

        if (incomingContracts == null) {
            incomingContracts = new ArrayList<>();
        }

        if (currScoreAdds == null) {
            currScoreAdds = new HashMap<>();
        }

        if (ownedTrophies == null) {
            ownedTrophies = new ArrayList<>();
        }
        if (inventory == null) {
            inventory = new Inventory();
        }

    }



    public void unlock(Unlockable unlockable, Message message) {
        if (unlockable.ordinal() >= Unlockable.BASE_1.ordinal()) {
            if (this.isUnlocked(unlockable)) {
                CountingBot.write(message, "You have already unlocked this.");
                return;
            }
            if (prestigePoints == 0) {
                CountingBot.write(message, "You don't have enough prestige points to do that.");
                return;
            }
            this.addUnlocked(message, unlockable);

        } else {
            if (this.isUnlocked(unlockable)) {
                CountingBot.write(message, "You have already unlocked this.");
                return;
            }
            if (unlockable.getPrice() > this.score) {
                CountingBot.write(message, "You only have " + this.score + " money, but you need " + unlockable.getPrice() + "!");
                return;
            }
            this.addUnlocked(message, unlockable);
            this.score -= unlockable.getPrice();
            CountingBot.write(message, "You unlocked '" + unlockable.getName() + "' and paid " + unlockable.getPrice() + " money. You have " + this.getScore() + " money left.");
            CountingBot.getInstance().save();
        }
    }

    public void unlockRuleCostUpgrade(Message message) {
        for (Unlockable currentRuleCostUpgradeToEvaluate : Unlockable.getRuleCostUpgrades()) {
            if (!this.isUnlocked(currentRuleCostUpgradeToEvaluate)) {
                unlock(currentRuleCostUpgradeToEvaluate, message);
                return;
            }
        }
        CountingBot.write(message, "You have already unlocked all rule cost upgrades.");
    }

    private void addUnlocked(Message message, Unlockable unlockable) {
        int[] newUnlocked = new int[unlocked.length + 1];
        for (int i = 0; i < this.unlocked.length; i++) {
            newUnlocked[i] = this.unlocked[i];
        }
        newUnlocked[this.unlocked.length] = unlockable.ordinal();
        this.unlocked = newUnlocked;

        if (unlockable.ordinal() >= Unlockable.BASE_1.ordinal()) {
            switch (unlockable) {
                case BASE_1:
                    unlockBase(message, "1");
                    break;
                case BASE_16:
                    unlockBase(message, "16");
                    break;
                case BASE_2:
                    unlockBase(message, "2");
                    break;
                case BASE_3:
                    unlockBase(message, "3");
                    break;
            }
        }

    }
    public int getOwedToBank() {
        int total = 0;
        for (Contract curr : contracts) {
            if (curr.toId.equals(Bank.CONTRACT_OWNER_ID)) {
                total += curr.limit;
            }
        }
        return total;
    }

    public void unlockBase(Message message, String base) {
        if (!BaseSystems.isNumInSystem(base, 10) || Integer.parseInt(base) > BaseSystems.MAX_BASE || Integer.parseInt(base) < 1) {
            CountingBot.write(message, "Invalid base! (note: base can not exceed 72.)");
            return;
        }
        int system = Integer.parseInt(base);
        if (isBaseUnlocked(system)) {
            CountingBot.write(message, "You have already unlocked this base.");
            return;
        }
        if (prestigePoints < Unlockable.getBasePrice(base)) {
            CountingBot.write(message, "You don't have enough prestige points to do that.");
            return;
        }

        if (unlockedSystems == null) {
            unlockedSystems = new int[]{};
        }
        int[] newUnlockedSys = new int[unlockedSystems.length + 1];
        for (int i = 0; i < this.unlockedSystems.length; i++) {
            newUnlockedSys[i] = unlockedSystems[i];
        }

        newUnlockedSys[unlockedSystems.length] = system;

        this.unlockedSystems = newUnlockedSys;
        this.prestigePoints -= Unlockable.getBasePrice(base);

        CountingBot.write(message, "You unlocked the 'base-" + base + "-system' and paid " + Unlockable.getBasePrice(base) + " prestige points. Counting in this System will no longer give you reduced score, and you can start streaks with this system now.");
        CountingBot.getInstance().save();

    }

    public double getAddruleDiscountFactor() {
        for (Unlockable currUnlockable : Unlockable.getRuleCostUpgradesDescending()) {
            if (isUnlocked(currUnlockable)) {
                return Unlockable.RULE_COST_UPGRADE_TO_COST_MULTIPLIER(currUnlockable);
            }
        }
        return 1.0;
    }

    public boolean hasTrophy(int trophy) {
        return ownedTrophies.contains(trophy);
    }

    public void addTrophy(int trophy) {
        ownedTrophies.add(trophy);
        ownedTrophies.sort(Comparator.naturalOrder());
        CountingBot.getInstance().save();
    }

    public int getTrophyShards() {
        return trophyShards;
    }

    public void addTrophyShard() {
        trophyShards++;
        CountingBot.getInstance().save();
    }

    public int getTrophyAmount() {
        return ownedTrophies.size();
    }

    public double getFactFromSysAndPrestiges(int base) {
        return (1.0 + MULT_PLUS_PER_PRESTIGE * prestiges) * ((isBaseUnlocked(base) && base != 10) ? SYSTEM_OWNED_FACT : 1.0);
    }

    public boolean isUnlocked(Unlockable unlockable) {
        int ord = unlockable.ordinal();
        for (int i = 0; i < this.unlocked.length; i++) {
            if (this.unlocked[i] == ord) {
                return true;
            }
        }
        return false;
    }

    public int getAccWorth() {
        int worth = 0;
        worth += score;
        for (int j : unlocked) {
            worth += Unlockable.values()[j].getPrice();
        }
        if (inventory.isShopUnlocked()) {
            worth += ShopCommandHandler.UNLOCK_COMMAND_USAGE_PRICE;
        }
        return worth;
    }

    public void addTradeOffer(TradeOffer tradeOffer, String key) { //trading start
        if (tradeOffers == null) {
            tradeOffers = new HashMap<>();
        }
        tradeOffers.put(key, tradeOffer);
    }

    public String buttonClick(String customId, Message message) {
        if (tradeOffers == null) {
            tradeOffers = new HashMap<>();
        }
        if (customId.startsWith("-")) {  // DECLINE
//            System.out.println("Tradeoffer declined!");
            String newId = customId.substring(1);
            if (tradeOffers.containsKey(newId)) {
                tradeOffers.remove(newId);
                return "Offer declined!";
            } else {
                return "This offer doesn't match any of yours";
            }
        } else if (customId.startsWith(Contract.ACCEPT_REMOVE_CONTRACT_PREFIX)) {
            String requestedRemoveId = customId.split(":")[1];
            String contractId = customId.split(":")[2];
            if (!Objects.equals(getId(), requestedRemoveId)) {
                return "Only " + CountingBot.getInstance().getCounter(guildId, requestedRemoveId).getName() + " can accept the removal!";
            }
            // iterate through both incoming contracts and contracts at once
            ArrayList<Contract> contractsToIterate = new ArrayList<>(contracts);
            contractsToIterate.addAll(incomingContracts);
            for (Contract currContract : contractsToIterate) {
                if (Objects.equals(currContract.getContractId(), contractId)) {
                    contractHandler.removeContract(currContract);
                    return "Contract removed successfully!";
                }
            }
            return "This contract doesn't match any of yours";
        } else if (customId.startsWith(Contract.DECLINE_REMOVE_CONTRACT_PREFIX)) {
            String requestedRemoveId = customId.split(":")[1];
            String contractId = customId.split(":")[2];
            if (!Objects.equals(getId(), requestedRemoveId)) {
                return "Only " + CountingBot.getInstance().getCounter(guildId, requestedRemoveId).getName() + " can decline the removal!";
            }
            ArrayList<Contract> contractsToIterate = new ArrayList<>(contracts);
            contractsToIterate.addAll(incomingContracts);
            for (Contract currContract : contractsToIterate) {
                if (Objects.equals(currContract.getContractId(), contractId)) {
                    return "Contract removal declined!";
                }
            }
            return "This contract doesn't match any of yours";
        } else if (tradeOffers.containsKey(customId)) {
            TradeOffer offer = tradeOffers.get(customId);
            String answ = offer.isTradeOfferValid();
            if (answ.toUpperCase().equals("VALID")) {
                offer.fullfill(message);
                tradeOffers.remove(customId);
                return "Accepted!";
            } else {
                return answ;
            }
        } else {
            return "This offer doesn't match any of yours";
        }
    }

    public void transferTo(Counter to, int amount, Message message) {
        to.addBonusScore(amount, message);
        subtractScore(amount);
        CountingBot.getInstance().save();

    }//trading end

    public boolean prestige(Message message) {
        if (getAccWorth() >= PRESTIGE_WORTH) {
            prestiges++;
            prestigePoints++;
            score = 0;
            unlocked = new int[]{};
            setShopUnlocked(false);
            CountingBot.getInstance().save();
            return true;
        } else {
            CountingBot.write(message, "Reset all your progress with ~prestige and acquire a global boost of 25%, as well as 1 prestige point. \n Your net worth (wallet + unlocks) has to be " + PRESTIGE_WORTH + " or more before you can do this. You are still missing " + (PRESTIGE_WORTH - getAccWorth()) + " money.");
            return false;
        }
    }

    public int getPrestiges() {
        return prestiges;
    }

    public BonusStreak[] getBonusStreaks() {
        return bonusStreaks;
    }

    public void bonus(Message message, BonusStreak.BonusCountType type, int count) {
        boolean exists = false;
        BonusStreak streak = null;
        for (BonusStreak currStreak : this.bonusStreaks) {
            if (currStreak.getType().equals(type)) {
                exists = true;
                streak = currStreak;
                break;
            }
        }
        if (!exists) {
            streak = generateBonusStreak(type);
        }
        streak.count(this, message, count);
    }

    public BonusStreak generateBonusStreak(BonusStreak.BonusCountType type) {
        BonusStreak streak = new BonusStreak(type);
        BonusStreak[] newStreaks = new BonusStreak[this.bonusStreaks.length + 1];
        for (int i = 0; i < this.bonusStreaks.length; i++) {
            newStreaks[i] = this.bonusStreaks[i];
        }
        newStreaks[newStreaks.length - 1] = streak;
        this.bonusStreaks = newStreaks;
        return streak;
    }

    public void contractInfo(Message message) {
        String mess = "**Your contracts:**\n\n";
        if (contracts.size() == 0 && incomingContracts.size() == 0) {
            CountingBot.write(message, "You don't have any active contracts!");
            return;
        }
        if (contracts.size() == 1) {
            mess += "Outgoing:\n" + contracts.get(0).toString();
        } else if (contracts.size() > 1) {
            mess += "You pay " + contractHandler.getCurrentTotalPerc() + "% of your income to";
            for (Contract curr : contracts) {
                mess += "\n" + curr.toString();
            }
        }
        if (incomingContracts.size() > 0) {
            mess += "\n\nIncoming:";
            for (Contract curr : incomingContracts) {
                mess += "\n" + curr;
            }
        }
        CountingBot.write(message, mess);
    }

    public void cancelContractsTo(Counter to) {
        Iterator<Contract> it = contracts.iterator();
        while (it.hasNext()) {
            Contract next = it.next();
            if (to.getId().equals(next.toId)) {
                it.remove();
            }
        }
        it = to.getIncomingContracts().iterator();
        while (it.hasNext()) {
            Contract next = it.next();
            if (getId().equals(next.owner.getId())) {
                it.remove();
            }
        }
    }

    public void notifyCount(int number, CountingStreak streak) {
        int scoreAdd = (int) Math.round(number * getBonusFact(streak) * getTrophyBonus(number));
//        System.out.println("ScoreAdd: " + scoreAdd + " for number " + number);
        if (this.currScoreAdds.containsKey(streak.getKey())) {
            this.currScoreAdds.replace(streak.getKey(), this.currScoreAdds.get(streak.getKey()) + scoreAdd);
        } else {
            this.currScoreAdds.put(streak.getKey(), scoreAdd);
        }
    }

    private double getTrophyBonus(int number) {
        if (hasTrophy(number)) {
            return TROPHY_BONUS_MULT;
        }
        return 1.0;
    }

    public void use(Purchasable item, Message message, Optional<CountingStreak> optionalStreak) {
        if (optionalStreak.isPresent()) {
            if (!currScoreAdds.containsKey(optionalStreak.get().getKey())){
                optionalStreak.get().addCounter(this, message);
            }
        }
        switch (item) {
            case FAKE_HAND_BAG: case HAND_BAG:
                writeUseMessage(Purchasable.FAKE_HAND_BAG, message);
                CountingBot.getInstance().requestHandBagRefundViaItem(message);
                inventory.removeItem(item);
                break;
            case WHITE_STREAK_ENDER:
                if (optionalStreak.isEmpty()) {
                    CountingBot.write(message, "You can only use streak-enders in an active streak!");
                    return;
                }
                optionalStreak.get().getStreakEnders().whiteUsed(message, this);
                break;
            default:
                throw new UnsupportedOperationException("Using item " + item.getName() + " is not implemented yet!");
        }
        CountingBot.getInstance().save();
    }

    private void writeUseMessage(Purchasable item, Message message) {
        CountingBot.write(message, "You used a " + item.getName() + "...");
    }

    public Integer[] getOwnedTrophies() {
        return ownedTrophies.toArray(new Integer[0]);
    }

    public double getBonusFact(CountingStreak streak) {
        return getFactFromSysAndPrestiges(streak.getBase()) * streak.getCurrentBonusFactor();
    }

    public void notifyWin(int win, int base, Message message) {
        addBonusScore(win, message);
    }

    public Stream<Contract> streamIncomingAndOutgoingContracts() {
        return Stream.concat(contracts.stream(), incomingContracts.stream());
    }

    public void succeed(CountingStreak streak, Message message) {
        addBonusScore(this.currScoreAdds.get(streak.getKey()), message);
        this.currScoreAdds.remove(streak.getKey());
    }

    public int getPendingStreakScore(CountingStreak streak) {
        if (this.currScoreAdds.containsKey(streak.getKey())) {
            return this.currScoreAdds.get(streak.getKey());
        }
        return 0;
    }

    public String getId() {
        return key;
    }

    public String getName() {
        return name;
    }

    public int fail(Message message, CountingStreak streak) {
        int couldHaveBeenPossible = getPossibleTotalInStreak(streak);
        int currScoreAdd = getStreakScoreAdd(streak);
        currScoreAdd /= 3.0d;
        score = (int) ((2.0d * (double) score / 3.0d));
        addBonusScore(currScoreAdd, message);
        this.currScoreAdds.remove(streak.getKey());
        return couldHaveBeenPossible - getScore();
    }

    public int failFromOwn(Message message, CountingStreak streak) {
        int couldHaveBeenPossible = getPossibleTotalInStreak(streak); // possible from total streak
        int currScoreAdd = this.currScoreAdds.get(streak.getKey()); // possible for losing user for streak
        currScoreAdd /= 2.0d; // gain is halved
        int scoreLose = (int) ((double) score / 4.0d);
        score -= scoreLose;
        addBonusScore(currScoreAdd, message);
        this.currScoreAdds.remove(streak.getKey());
        return couldHaveBeenPossible - getScore();
    }

    public void addBonusScore(int score, Message message) {
        this.score += contractHandler.getNetto(score, message);
    }

    public void addBonusScoreFromContract(int score, Message message) {
        int taxed = (score / 2);
        if (taxed != 0) {
            addBonusScore(taxed, message);
        }
        this.score += score - taxed;

    }

    public int getScore() {
        return score;
    }

    public boolean canAfford(int price) {
        return price <= score;
    }

    public int getPossibleTotalInStreak(CountingStreak streak) {
        return score + getStreakScoreAdd(streak);
    }

    private int getStreakScoreAdd(CountingStreak streak) {
        if (! this.currScoreAdds.containsKey(streak.getKey())) {
            System.err.println("Trying to calculate streak score add for a streak which thinks I am in the streak, but I dont know this streak from currScoreAdds!");
            return 0;
        }
        return currScoreAdds.get(streak.getKey());
    }


    public int getPossibleTotal() {

        return score + getCurrentScoreAdd();
    }

    public int getCurrentScoreAdd() {
        int currScoreAdd = 0;
        for (Integer currInt : this.currScoreAdds.values()) {
            currScoreAdd += currInt;
        }
        return currScoreAdd;
    }

    public void subtractScore(int sub) {
        score -= sub;
    }

    public boolean isBaseUnlocked(int base) {
        if (base == 10) {
            return true;
        }

        if (unlockedSystems == null) {
            unlockedSystems = new int[]{};
            return false;
        }

        for (int i = 0; i < unlockedSystems.length; i++) {
            int currSys = unlockedSystems[i];
            if (currSys == base) {
                return true;
            }
        }
        return false;
    }

    public int[] getUnlockedBases() {
        return unlockedSystems;
    }

    public int getPrestigePoints() {
        return prestigePoints;
    }

    public String getPing() {
        return "<@!" + getId() + ">";
    }

    public ContractHandler getContractHandler() {
        return contractHandler;
    }

    public List<Contract> getContracts() {
        return contracts;
    }

    public List<Contract> getIncomingContracts() {
        return incomingContracts;
    }

    public void addStreakToCurrAdd(CountingStreak streak) {
        if (!this.currScoreAdds.containsKey(streak.getKey())) {
            this.currScoreAdds.put(streak.getKey(), 0);
        }
    }

    public String getGuildId() {
        return guildId;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public boolean isShopUnlocked() {
        return getInventory().isShopUnlocked();
    }

    private void setShopUnlocked(boolean unlocked) {
        getInventory().setShopUnlocked(unlocked);
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }




}