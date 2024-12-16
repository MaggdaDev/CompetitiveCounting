/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;

/**
 *
 * @author DavidPrivat
 */
public enum Unlockable {
    UNLOCK_COMMAND(10000,"Unlock command", "The ability to unlock new stuff with ~unlock"),
    DIV_RULE(20000, "div_rule", "Numbers with the selected divisor n must be skipped"),
    ROOT_RULE(25000, "root_rule", "Numbers which have an integer nth root must be skipped"),
    DIGSUM_RULE(30000, "digsum_rule", "Numbers with digsum n must be skipped"),
    
    SLOWMODE_RULE(50000, "slowmode_rule", "A certain time n has to pass between counts"),
    TIMELIMIT_RULE(100000, "timelimit_rule", "The next number must have been counted before a certain time n has passed"),

    RULE_COST_UPGRADE_1(150000, "rule_cost_upgrade", "Adding a rule to a streak costs 10% less"),
    RULE_COST_UPGRADE_2(200000, "rule_cost_upgrade", "Adding a rule to a streak costs 20% less"),
    RULE_COST_UPGRADE_3(400000, "rule_cost_upgrade", "Adding a rule to a streak costs 30% less"),
    RULE_COST_UPGRADE_4(700000, "rule_cost_upgrade", "Adding a rule to a streak costs 40% less"),
    RULE_COST_UPGRADE_5(1000000, "rule_cost_upgrade", "Adding a rule to a streak costs 50% less"),

    BASE_1(-1, "base1", "Counting in unary: 1"),
    BASE_2(-1, "base2", "Counting in binary"),
    BASE_3(-1, "base3", "Counting in ternary"),
    BASE_16(-1, "base16", "Counting in hexadecimal"),
    BASE_N(-2, "base n", "Counting in base-n");
    private final int prize;
    private String name, description;
    Unlockable(int prize, String name, String description) {
        this.prize = prize;
        this.name = name;
        this.description = description;
    }
    
    public static int getBasePrize(String base) {
        switch(base) {
            case "1": case "2": case "3": case "16":
                return Math.abs(Unlockable.BASE_1.getPrize());
            default:
                return Math.abs(Unlockable.BASE_N.getPrize());
        }
    }
    
    public int getPrize() {
        return prize;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }

    public static Unlockable[] getRuleCostUpgrades() {
        return new Unlockable[] {RULE_COST_UPGRADE_1, RULE_COST_UPGRADE_2, RULE_COST_UPGRADE_3, RULE_COST_UPGRADE_4, RULE_COST_UPGRADE_5};
    }

    public static Unlockable[] getRuleCostUpgradesDescending() {
        return new Unlockable[] {RULE_COST_UPGRADE_5, RULE_COST_UPGRADE_4, RULE_COST_UPGRADE_3, RULE_COST_UPGRADE_2, RULE_COST_UPGRADE_1};
    }

    public static double RULE_COST_UPGRADE_TO_COST_MULTIPLIER(Unlockable ruleCostUpgrade) {
        switch (ruleCostUpgrade) {
            case RULE_COST_UPGRADE_1:
                return 0.9;
            case RULE_COST_UPGRADE_2:
                return 0.8;
            case RULE_COST_UPGRADE_3:
                return 0.7;
            case RULE_COST_UPGRADE_4:
                return 0.6;
            case RULE_COST_UPGRADE_5:
                return 0.5;
            default:
                throw new IllegalArgumentException("Not a rule cost upgrade: " + ruleCostUpgrade);
        }
    }
}
