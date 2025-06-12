package CompetitiveCounting;

import CompetitiveCounting.bank.Bank;

import java.util.HashMap;

public class CountingGuild {
    private final HashMap<String, Counter> counters;
    private final Bank bank;
    private final String guildId;

    public CountingGuild(String guildId) {
        this.guildId = guildId;
        this.counters = new HashMap<>();
        this.bank = new Bank(guildId);
    }

    public Counter getCounter(String counterId) {
        return counters.get(counterId);
    }

    public void addNewCounter(String counterId, String counterName) {
        if (!counters.containsKey(counterId)) {
            Counter counter = new Counter(guildId, counterId, counterName);
            counters.put(counterId, counter);
        }
    }

    public boolean hasCounter(String counterId) {
        return counters.containsKey(counterId);
    }

    public HashMap<String, Counter> getCounters() {
        return counters;
    }

    public Bank getBank() {
        return bank;
    }

    public String getGuildId() {
        return guildId;
    }
}