package CompetitiveCounting.bank;

import CompetitiveCounting.CountingBot;
import CompetitiveCounting.Counter;
import CompetitiveCounting.bank.exceptions.BankLoanException;
import CompetitiveCounting.dialogue.Dialogue;
import discord4j.core.object.entity.Message;

public class BankLoanHandler {
    private final static CountingBot bot = CountingBot.getInstance();

    public static void giveLoan(String guildId, String userId, int loanAmount, int loanRate, Message message) throws BankLoanException {
        Counter initCounter = bot.getCounter(guildId, userId);
        Bank bank = bot.getGuilds().get(guildId).getBank();

        int extraPayback = calculateLoanRepayAmount(loanAmount, loanRate);

        int userContractPerc = initCounter.getContractHandler().getCurrentTotalPerc();
        if (userContractPerc + loanRate > 100) {
            throw new BankLoanException("I like your energy, but you're being a bit overzealous! You can't give me more than 100% of what you earn!");
        }

        if (bank.getTotalScore() < loanAmount) {
            throw new BankLoanException("I'd really like to strike a deal with you, but unfortunately I don't have enough money for this.");
        }

        int maxLoanLimit = 100000; // todo: make dependent on upgrades
        int maxTotalOwedToBank = 100000; // todo: maybe sprite maybe lean
        int crocLoanFee = 999;
        // Todo: Upgrades, maxlim marklov
        if (loanAmount < 1000) {
            throw new BankLoanException("This small amount of money is not even worth the paperwork to give a loan to you!");
        }
        if (loanAmount > maxLoanLimit) {
            throw new BankLoanException("I will not entrust you with such a great sum of money, for I have no faith in your ability to pay it back.");
        }
        if (initCounter.getOwedToBank() + loanAmount + extraPayback > maxTotalOwedToBank) {
            throw new BankLoanException("Do you take me for a fool? Pay back your debt before asking for more money! ");
        }

        bank.removeMoney(loanAmount - crocLoanFee);
        initCounter.addBonusScore(loanAmount - crocLoanFee, message);
        initCounter.getContractHandler().addContract(bank, loanRate, loanAmount + extraPayback);

        new Dialogue().addNpcLine("Here, take these " + (loanAmount - crocLoanFee) + " money! You now owe me " + (loanAmount + extraPayback) + " money, which you will pay back by giving me " +
                        loanRate + "% of your income. ", 2000)
                .addNpcLine("By the way... you'd better pay me back my money soon, or else...", 3000)
                .addNpcLine("... I will tell my cousins... they already know your IP address...", 0)
                .play(message);
    }

    private static int calculateLoanInterestRate(int money, int rate) {
        return (int) (Math.ceil(Math.sqrt(money) / 10) / (4 * rate) * 100);
    }

    private static int calculateLoanRepayAmount(int money, int rate) { // change back to int
        return (int) (calculateLoanInterestRate(money, rate) * 0.01 * money);
    }
}
