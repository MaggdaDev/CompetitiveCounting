package competitivecounting;

public class CountingContext {

    private final Counter counter;
    private final int currentNumber;
    private final int lastNumber;
    private final CountingStreak streak;
    private final int lastScoreAdd;
    private final String lastCounterId;
    public CountingContext(Counter counter, int currentNumber, int lastNumber, CountingStreak streak, int lastScoreAdd, String lastCounterId) {
        this.counter = counter;
        this.currentNumber = currentNumber;
        this.lastNumber = lastNumber;
        this.streak = streak;
        this.lastScoreAdd = lastScoreAdd;
        this.lastCounterId = lastCounterId;
    }

    public Counter getCounter() {
        return counter;
    }

    public int getCurrentNumber() {
        return currentNumber;
    }

    public int getLastNumber() {
        return lastNumber;
    }

    public CountingStreak getStreak() {
        return streak;
    }

    public int getLastScoreAdd() {
        return lastScoreAdd;
    }

    /**
     *
     * @return the last counter id, or "" if it is the first counter
     */
    public String getLastCounterId() {
        return lastCounterId;
    }
}
