package CompetitiveCounting.bank;

import CompetitiveCounting.CountingBot;
import CompetitiveCounting.Counter;
import CompetitiveCounting.bank.exceptions.BankLoanException;
import discord4j.core.object.entity.Message;

public class BankLoanHandler {
    private final static CountingBot bot = CountingBot.getInstance();

    public static void giveLoan(String guildId, String userId, int loanAmount, int loanRate, Message message) throws BankLoanException {
        Counter initCounter = bot.getCounter(guildId, userId);
        Bank bank = bot.getGuilds().get(guildId).getBank();

        int userContractPerc = initCounter.getContractHandler().getCurrentTotalPerc();
        if (userContractPerc + loanRate > 100) {
            throw new BankLoanException("I like your energy, but you're being a bit overzealous! You can't give me more than 100% of what you earn!");
        }

        if (bank.getTotalScore() < loanAmount) {
            throw new BankLoanException("I'd really like to strike a deal with you, but unfortunately I don't have enough money for this.");
        }
        // todo check if user has contract with the bank, and if yes, deny

        bank.removeMoney(loanAmount);
        initCounter.addBonusScore(loanAmount, message);

        //initCounter.getContractHandler().addContract(derCounterDerKeinCounterIstWeilErDieBankIstLeel, loanRate, loanAmount);
        // todo fix weil die bank kein counter ist aber diese methode ein counter objekt braucht
        // priority 1
    }
}
