package CompetitiveCounting;

public class CountingContext {

    private final Counter counter;
    private final int currentNumber;
    private final int lastNumber;
    private final CountingStreak streak;
    public CountingContext(Counter counter, int currentNumber, int lastNumber, CountingStreak streak) {
        this.counter = counter;
        this.currentNumber = currentNumber;
        this.lastNumber = lastNumber;
        this.streak = streak;
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
}
