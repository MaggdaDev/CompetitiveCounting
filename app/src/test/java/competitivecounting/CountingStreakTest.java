package competitivecounting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CountingStreakTest extends CountingTest{
    @BeforeEach
    void setup() {
        super.setUp();
    }

    @Test
    void canAffordNumberRulesWithOneType() {
        CountingStreak c = new CountingStreak("your mom", 10, "your dad");

        assertFalse(c.canAffordNumberRulesWithOneType(message, counter, "div", 6));
        counter.addBonusScore(500, message);
        assertTrue(c.canAffordNumberRulesWithOneType(message, counter, "div", 4));
    }
}