package competitivecounting.contracts;

import discord4j.core.object.entity.Message;

import java.util.List;

public interface ContractOwner {
    String getGuildId();

    String getName();

    List<Contract> getContracts();
    List<Contract> getIncomingContracts();

    String getPing();

    String getId();

    void addBonusScoreFromContract(int pay, Message message);
}
