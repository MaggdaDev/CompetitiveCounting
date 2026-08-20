package competitivecounting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DailyStreakTest {

    private DailyStreak dailyStreak;

    @BeforeEach
    void setUp() {
        dailyStreak = new DailyStreak();
    }

    @Test
    void incrementStreak() {
        dailyStreak.increment();
        assertEquals(1, dailyStreak.getCurrentCount());
    }

}