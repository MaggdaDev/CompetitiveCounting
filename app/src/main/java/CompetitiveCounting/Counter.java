/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;

import discord4j.core.object.entity.Message;

import java.util.*;

/**
 *
 * @author DavidPrivat
 */
public class Counter {

    public final static int PRESTIGE_WORTH = 1000000;
    public final static double SYSTEM_NOT_OWNED_FACT = 0.5;
    public final static double MULT_PLUS_PER_PRESTIGE = 0.25;

    public final static double TROPHY_BONUS_MULT = 2;
    private final String key, name;
    private int score, prestiges, prestigePoints;

    private transient HashMap<String, Integer> currScoreAdds;
    private int[] unlocked;
    private int[] unlockedSystems;
    private BonusStreak[] bonusStreaks;
    private List<Contract> contracts;
    private List<Integer> ownedTrophies;
    private transient List<Contract> incomingContracts;
    private transient HashMap<String, TradeOffer> tradeOffers = new HashMap<String, TradeOffer>();
    private transient ContractHandler contractHandler;

    public Counter(String key, String name, int score, int prestiges, int prestigePoints, int[] unlocked, int[] unlockedSystems, BonusStreak[] bonusStreaks) {
        this.key = key;
        this.score = score;
        this.name = name;
        this.unlocked = unlocked;
        currScoreAdds = new HashMap<>();
        this.prestiges = prestiges;
        this.prestigePoints = prestigePoints;
        this.unlockedSystems = unlockedSystems;
        this.bonusStreaks = bonusStreaks;
        init();
        initIncomingContracts(CountingBot.getInstance().getCounters());
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
        
        if (currScoreAdds == null) {
            currScoreAdds = new HashMap<>();
        }

        if (ownedTrophies == null) {
            ownedTrophies = new ArrayList<>();
        }

    }

    public void initIncomingContracts(HashMap<String, Counter> counters) {
        if (incomingContracts.isEmpty()) {
            counters.forEach((String currId, Counter counter) -> {
                for (Contract currContract : counter.getContracts()) {
                    if (currContract.toId.equals(this.getId())) {
                        currContract.owner = counter;
                        incomingContracts.add(currContract);
                    }
                }
            });
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
            if (unlockable.getPrize() > this.score) {
                CountingBot.write(message, "You only have " + this.score + " money, but you need " + unlockable.getPrize() + "!");
                return;
            }
            this.addUnlocked(message, unlockable);
            this.score -= unlockable.getPrize();
            CountingBot.write(message, "You unlocked '" + unlockable.getName() + "' and paid " + unlockable.getPrize() + " money. You have " + this.getScore() + " money left.");
            CountingBot.getInstance().safeCounters();
        }
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

    public void unlockBase(Message message, String base) {
        if (!BaseSystems.isNumInSystem(base, 10) || Integer.parseInt(base) > 1000 || Integer.parseInt(base) < 1) {
            CountingBot.write(message, "Invalid base! (note: base can not exceed 1000.)");
            return;
        }
        int system = Integer.parseInt(base);
        if (isBaseUnlocked(system)) {
            CountingBot.write(message, "You have already unlocked this base.");
            return;
        }
        if (prestigePoints < Unlockable.getBasePrize(base)) {
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
        this.prestigePoints -= Unlockable.getBasePrize(base);

        CountingBot.write(message, "You unlocked the 'base-" + base + "-system' and paid " + Unlockable.getBasePrize(base) + " prestige points. Counting in this System will no longer give you reduced score, and you can start streaks with this system now.");
        CountingBot.getInstance().safeCounters();

    }

    public boolean hasTrophy(int trophy) {
        return ownedTrophies.contains(trophy);
    }

    public void addTrophy(int trophy) {
        ownedTrophies.add(trophy);
        CountingBot.getInstance().safeCounters();
    }

    public int getTrophyAmount() {
        return ownedTrophies.size();
    }

    public double getFactFromSys(int base) {
        if (isBaseUnlocked(base)) {
            return 1.0 + MULT_PLUS_PER_PRESTIGE * prestiges;
        } else {
            return SYSTEM_NOT_OWNED_FACT;
        }
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
        for (int i = 0; i < unlocked.length; i++) {
            worth += Unlockable.values()[this.unlocked[i]].getPrize();
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
            System.out.println("Tradeoffer declined!");
            String newId = customId.substring(1);
            if (tradeOffers.containsKey(newId)) {
                tradeOffers.remove(newId);
                return "Offer declined!";
            } else {
                return "This offer doesn't match any of yours";
            }
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
        CountingBot.getInstance().safeCounters();

    }//trading end

    public boolean prestige(Message message) {
        if (getAccWorth() >= PRESTIGE_WORTH) {
            prestiges++;
            prestigePoints++;
            score = 0;
            unlocked = new int[]{};
            CountingBot.getInstance().safeCounters();
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
        String mess = "";
        if (contracts.size() == 0 && incomingContracts.size() == 0) {
            CountingBot.write(message, "You don't have any active contracts!");
            return;
        }
        if (contracts.size() == 1) {
            mess += "You pay to\n" + CountingBot.getInstance().getCounter(contracts.get(0).toId).getName() + ": " + contracts.get(0).toString();
        } else if (contracts.size() > 1) {
            mess += "You pay " + contractHandler.getCurrentTotalPerc() + "% of your income to";
            for (Contract curr : contracts) {
                mess += "\n" + CountingBot.getInstance().getCounter(curr.toId).getName() + ": " + curr.toString();
            }
        }
        if (incomingContracts.size() > 0) {
            mess += "\n\nYou get from";
            for (Contract curr : incomingContracts) {
                mess += "\n" + curr.owner.getName() + ": " + curr.toString();
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
        System.out.println("ScoreAdd: " + scoreAdd + " for number " + number);
        if (this.currScoreAdds.containsKey(streak.getKey())) {
            this.currScoreAdds.replace(streak.getKey(), this.currScoreAdds.get(streak.getKey()) + scoreAdd);
        } else {
            this.currScoreAdds.put(streak.getKey(), scoreAdd);
        }
    }

    private double getTrophyBonus(int number) {
        if(hasTrophy(number)) {
            return TROPHY_BONUS_MULT;
        }
        return 1.0;
    }

    public double getBonusFact(CountingStreak streak) {
        return getFactFromSys(streak.getBase()) * streak.getCurrentBonusFactor();
    }

    public void notifyWin(int win, int base, Message message) {
        addBonusScore(win, message);
    }

    public void succeed(CountingStreak streak, Message message) {
        addBonusScore(this.currScoreAdds.get(streak.getKey()), message);
        this.currScoreAdds.replace(streak.getKey(), 0);
    }

    public String getId() {
        return key;
    }

    public String getName() {
        return name;
    }

    public int fail(Message message, CountingStreak streak) {
        int couldHaveBeenPossible = getPossibleTotalInStreak(streak);
        int currScoreAdd = this.currScoreAdds.get(streak.getKey());
        currScoreAdd /= 3.0d;
        score = (int) ((2.0d * (double) score / 3.0d));
        addBonusScore(currScoreAdd, message);
        this.currScoreAdds.replace(streak.getKey(), 0);
        return couldHaveBeenPossible - getScore();
    }

    public int failFromOwn(Message message, CountingStreak streak) {
        int couldHaveBeenPossible = getPossibleTotalInStreak(streak);
        int currScoreAdd = this.currScoreAdds.get(streak.getKey());
        currScoreAdd /= 4.0d;
        score = (int) ((2.0d * (double) score / 3.0d));
        addBonusScore(currScoreAdd, message);
        this.currScoreAdds.replace(streak.getKey(), 0);
        return couldHaveBeenPossible - getScore();
    }

    public void addBonusScore(int score, Message message) {
        this.score += contractHandler.getNetto(score, message);
    }

    public void addBonusScoreFromContract(int score, Message message) {
        int taxed = (int) (score / 2);
        if (taxed != 0) {
            addBonusScore(taxed, message);
        }
        this.score += score - taxed;

    }

    public int getScore() {
        return score;
    }
    
    public int getPossibleTotalInStreak(CountingStreak streak) {
        return score + currScoreAdds.get(streak.getKey());
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
        if(!this.currScoreAdds.containsKey(streak.getKey())) {
            this.currScoreAdds.put(streak.getKey(), 0);
        }
    }


}
