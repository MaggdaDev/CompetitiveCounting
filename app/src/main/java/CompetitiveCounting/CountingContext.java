package CompetitiveCounting;

public class CountingContext {

    private final Counter counter;
    private final int currentNumber;
    private final int lastNumber;
    public CountingContext(Counter counter, int currentNumber, int lastNumber) {
        this.counter = counter;
        this.currentNumber = currentNumber;
        this.lastNumber = lastNumber;

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
}
