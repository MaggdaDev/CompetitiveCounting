package competitivecounting.bank;

import competitivecounting.CountingBot;
import competitivecounting.CountingEmojis;
import competitivecounting.CountingTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BankCommandHandlerTest extends CountingTest {

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void testWithdrawGivesFullMoney() throws InterruptedException{
        Bank bank = CountingBot.getInstance().getGuilds().get(counter.getGuildId()).getBank();
        int baseScore = 1050505;
        bank.unlock();

        counter.addBonusScore(11000, message);
        assertEquals(baseScore, bank.getTotalScore());
        String messageId = "56789";
        simulateMessageCreation("~bank deposit 11000", messageId, getMockedUser(COUNTER_ID));
        Thread.sleep(100);
        assertEquals(baseScore + 11000, bank.getTotalScore());
        counter.getContractHandler().addContract(bank, 50, 10000);
        simulateMessageCreation("~bank withdraw 10000", messageId, getMockedUser(COUNTER_ID));
        Thread.sleep(100);
        assertEquals(10000, counter.getScore());
        System.out.println(output);
    }

}