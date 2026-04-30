package CompetitiveCounting.bank;

import CompetitiveCounting.CountingBot;
import CompetitiveCounting.Counter;
import CompetitiveCounting.bank.bankupgrades.DebtLimitUpgrade;
import CompetitiveCounting.bank.bankupgrades.LoanLimitUpgrade;
import CompetitiveCounting.bank.bankupgrades.LoanRateUpgrade;
import CompetitiveCounting.bank.exceptions.BankLoanException;
import CompetitiveCounting.dialogue.Dialogue;
import discord4j.core.object.entity.Message;

import java.util.List;

public class BankLoanHandler {
    private final static CountingBot bot = CountingBot.getInstance();

    public static void giveLoan(String guildId, String userId, int loanAmount, int loanRate, Message message) throws BankLoanException {
        Counter initCounter = bot.getCounter(guildId, userId);
        Bank bank = bot.getGuilds().get(guildId).getBank();
        LoanRateUpgrade loanRateUpgrade = bank.getAccount(userId).getUpgrades().getLoanRateUpgrade();
        LoanLimitUpgrade loanLimitUpgrade = bank.getAccount(userId).getUpgrades().getLoanLimitUpgrade();
        DebtLimitUpgrade debtLimitUpgrade = bank.getAccount(userId).getUpgrades().getDebtLimitUpgrade();

        int extraPayback = calculateLoanRepayAmount(loanAmount, loanRate, loanRateUpgrade);
        int extraPaybackWithoutUpgrade = calculateLoanRepayAmount(loanAmount, loanRate, LoanRateUpgrade.EMPTY);

        int userContractPerc = initCounter.getContractHandler().getCurrentTotalPerc();
        if (userContractPerc + loanRate > 100) {
            throw new BankLoanException("I like your energy, but you're being a bit overzealous! You can't give me more than 100% of what you earn!");
        }

        if (bank.getTotalScore() < loanAmount) {
            throw new BankLoanException("I'd really like to strike a deal with you, but unfortunately I don't have enough money for this.");
        }

        int maxLoanLimit = loanLimitUpgrade.getCurrentValue();
        int maxTotalOwedToBank = debtLimitUpgrade.getCurrentValue();
        int crocLoanFee = 999;
        if (loanAmount < 1000) {
            throw new BankLoanException("This small amount of money is not even worth the paperwork to give a loan to you!");
        }
        if (loanAmount > maxLoanLimit) {
            throw new BankLoanException("I will not entrust you with more than " + maxLoanLimit + " money, for I have no faith in your ability to pay it back.");
        }

        int resultingTotalOwed = initCounter.getOwedToBank() + loanAmount + extraPayback;
        if (resultingTotalOwed > maxTotalOwedToBank) {
            throw new BankLoanException(
                    "Do you take me for a fool? This loan would increase your debt to " + resultingTotalOwed
                    + " money. Pay back your debt before asking for more money! The maximum money you may owe me is " +
                            maxTotalOwedToBank + " money.");
        }

        bank.removeMoney(loanAmount - crocLoanFee);
        initCounter.addBonusScore(loanAmount - crocLoanFee, message);
        initCounter.getContractHandler().addContract(bank, loanRate, loanAmount + extraPayback);

        // System.out.println(interestRateUpgrade(loanAmount, loanRate, )); todo

        new Dialogue().addNpcLine("Here, take these " + (loanAmount - crocLoanFee) + " money! You now owe me " +
                        (loanRateUpgrade.getCurrentLvl() == 0 ? "" : "~~" + (loanAmount + extraPaybackWithoutUpgrade) + "~~ ") +
                        (loanAmount + extraPayback) + " money, which you will pay back by giving me " +
                        loanRate + "% of your income. ", 2000)
                .addNpcLine("By the way... you'd better pay me back my money soon, or else...", 3000)
                .addNpcLine("... I will tell my cousins... they already know your IP address...", 0)
                .setNpcLineConverter(BankCommandHandler::toCrocText)
                .play(message);
    }

    private static int calculateLoanInterestRate(int money, int rate, LoanRateUpgrade loanRateUpgrade) {
        return (int) ((Math.ceil(Math.sqrt(money) / 10) / (4 * rate) * loanRateUpgrade.getCurrentValue()));
    }

    private static int calculateLoanRepayAmount(int money, int rate, LoanRateUpgrade loanRateUpgrade) { // change back to int
        return (int) (calculateLoanInterestRate(money, rate, loanRateUpgrade) * 0.01 * money);
    }
}
