package CompetitiveCounting.bank;

import CompetitiveCounting.CountingBot;
import CompetitiveCounting.Counter;
import CompetitiveCounting.CountingEmojis;
import CompetitiveCounting.bank.bankupgrades.DebtLimitUpgrade;
import CompetitiveCounting.bank.bankupgrades.LoanLimitUpgrade;
import CompetitiveCounting.bank.bankupgrades.LoanRateUpgrade;
import CompetitiveCounting.bank.exceptions.BankLoanException;
import CompetitiveCounting.dialogue.Dialogue;
import discord4j.core.object.entity.Message;

import java.util.Optional;

public class BankLoanHandler {
    private final static CountingBot bot = CountingBot.getInstance();

    public static void giveLoan(String guildId, String userId, int loanAmount, int percentOfIncomeRepay, Message message) throws BankLoanException {
        Counter initCounter = bot.getCounter(guildId, userId);
        Bank bank = bot.getGuilds().get(guildId).getBank();
        LoanRateUpgrade loanRateUpgrade = bank.getAccount(userId).getUpgrades().getLoanRateUpgrade();
        LoanLimitUpgrade loanLimitUpgrade = bank.getAccount(userId).getUpgrades().getLoanLimitUpgrade();
        DebtLimitUpgrade debtLimitUpgrade = bank.getAccount(userId).getUpgrades().getDebtLimitUpgrade();

        int extraPayback = calculateLoanRepayAmount(loanAmount, percentOfIncomeRepay, loanRateUpgrade);
        int extraPaybackWithoutUpgrade = calculateLoanRepayAmount(loanAmount, percentOfIncomeRepay, LoanRateUpgrade.EMPTY);

        int userContractPerc = initCounter.getContractHandler().getCurrentTotalPerc();
        if (userContractPerc + percentOfIncomeRepay > 100) {
            throw new BankLoanException("I like your energy, but you're being a bit overzealous! You can't give me more than 100% of what you earn!");
        }

        if (bank.getTotalScore() < loanAmount) {
            throw new BankLoanException("I'd really like to strike a deal with you, but unfortunately I don't have enough money for this.");
        }

        int maxLoanLimit = loanLimitUpgrade.getCurrentValue();
        int maxLoanLimitWithoutUpgrade = LoanLimitUpgrade.EMPTY.getCurrentValue();
        int maxTotalOwedToBank = debtLimitUpgrade.getCurrentValue();
        int maxTotalOwedToBankWithoutUpgrade = DebtLimitUpgrade.EMPTY.getCurrentValue();
        int crocLoanFee = 999;

        if (loanAmount < 1000) {
            throw new BankLoanException("This small amount of money is not even worth the paperwork to give a loan to you!");
        }
        if (loanAmount > maxLoanLimit) {
            String amount = maxLoanLimit + " money";
            if(loanLimitUpgrade.getCurrentLvl() > 0) {
                amount = "~~" + maxLoanLimitWithoutUpgrade + "~~" + amount;
            }
            String exception = "I will not entrust you with more than " + amount + ", for I have no faith in your ability to pay it back.";
            if (!loanLimitUpgrade.isMaxedOut()) {
                exception += " Buying the '" + loanLimitUpgrade.getNextName() + "' could, however, increase your trustworthiness.";
            }
            throw new BankLoanException(exception);
        }

        int resultingTotalOwed = initCounter.getOwedToBank() + loanAmount + extraPayback;
        if (resultingTotalOwed > maxTotalOwedToBank) {
            String s = "Do you take me for a fool? This loan would increase your total debt across all loans to " + resultingTotalOwed + " money." +
                    "The maximum money you may owe me is ~~" + maxTotalOwedToBankWithoutUpgrade + "~~ " +
                    maxTotalOwedToBank + " money.";
            if (!debtLimitUpgrade.isMaxedOut()) {
                s += "\nYou can raise this ceiling by buying the " + debtLimitUpgrade.getNextName() + ".";
            }

            // Todo: Total interest sagen
            throw new BankLoanException(s);
        }

        // Todo ~~ geld ~~ syntax nur wenn tatsächlich ein upgrade gekauft wurde!
        // Todo bei allen stellen mit ~~ überprüfen, ob das dort richtig ist
        new Dialogue().addNpcLine("Ok, I'll hand " + loanAmount + " money over to you, and you will pay me back " +
                        "~~" + (loanAmount + extraPaybackWithoutUpgrade) + "~~ " +
                (loanAmount + extraPayback) + " money by giving me " + percentOfIncomeRepay + "% of your income. Do we have a deal?", 0)
                .addEmojiReaction(CountingEmojis.HANDSHAKE)
                .addEmojiReaction(CountingEmojis.GOBLIN)
                .initializeParallelDialogElements()
                .addWaitForEmojiReaction(CountingEmojis.GOBLIN, true, m-> {
                    int consultingFee = Math.min(599, initCounter.getScore() / 10);
                    BankCommandHandler.bankWrite(message, "Even though you have decided not to move forward with this transaction, I must inform you that I have meticulously measured the administrative expenses for this consultation you have requested, and you will receive an invoice of " + consultingFee + " money.");
                    initCounter.subtractScore(consultingFee);
                }, Optional.of(userId))
                .addWaitForEmojiReaction(CountingEmojis.HANDSHAKE, false, m->{}, Optional.of(userId))
                .finishParallelDialogElementsAndAdd()
                .addRunnable(m -> {
                    // Do Contract
                    bank.removeMoney(loanAmount - crocLoanFee);
                    initCounter.addBonusScore(loanAmount - crocLoanFee, message);
                    initCounter.getContractHandler().addContract(bank, percentOfIncomeRepay, loanAmount + extraPayback);
                })
                .addNpcLine("Here, take these " + (loanAmount - crocLoanFee) + " money! With all of your loans, you now owe me a total of " +
                        resultingTotalOwed + " money. Btw, you can always check your debts with ~contracts.", 2000)
                .addNpcLine("By the way... you'd better pay me back my money soon, or else...", 3000)
                .addNpcLine("... I will tell my cousins... they already know your IP address...", 0)
                .setNpcLineConverter(BankCommandHandler::toCrocText)
                .play(message);
    }

    private static int calculateLoanInterestRate(int money, int rate, LoanRateUpgrade loanRateUpgrade) {
        return (int) ((Math.ceil(Math.sqrt(money) / 10) / (4 * rate) * loanRateUpgrade.getCurrentValue()));
    }

    private static int calculateLoanRepayAmount(int money, int rate, LoanRateUpgrade loanRateUpgrade) { // go int it down midlane
        return (int) (calculateLoanInterestRate(money, rate, loanRateUpgrade) * 0.01 * money);
    }
}
