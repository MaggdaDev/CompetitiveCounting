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
}
