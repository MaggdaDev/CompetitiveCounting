package CompetitiveCounting;

import CompetitiveCounting.bank.Bank;

import java.util.HashMap;

public class CountingGuild {
    private HashMap<String, Counter> counters;
    private Bank bank;
    private final String guildId;

    public CountingGuild(String guildId) {
        this.guildId = guildId;
        init();
    }
    public void init() {
        if (this.bank == null) {
            this.bank = new Bank(guildId);
        } else {
            bank.init();
        }
        if (this.counters == null) {
            this.counters = new HashMap<>();
        }
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