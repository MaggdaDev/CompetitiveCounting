package competitivecounting.vaults.publicgoodsvault;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PublicGoodsDocumentation {
    private boolean finished = false;
    private boolean canceled = false;
    private final List<Fee> fees = new ArrayList<>();
    private final List<StageContribution> stageContributions = new ArrayList<>();
    private final List<PotData> potData = new ArrayList<>();

    private int buyIn = -1;
    private int cashout = -1;
    private int crocCoinScoreAfterPotPayout = -1;
    private int potPayout = -1;
    private int threshold = -1;
    private int playersParticipating = -1;
    private int vaultMoneyWin = -1;
    private boolean monocleActive = false;

    public PublicGoodsDocumentation(int playersParticipating, int threshold) {
        this.playersParticipating = playersParticipating;
        this.threshold = threshold;
    }

    private static class Fee {
        int amount;
        String reason;

        public Fee(int amount, String reason) {
            this.amount = amount;
            this.reason = reason;
        }
    }

    private static class PotData {
        int potAmount;

        public PotData(int potAmount) {
            this.potAmount = potAmount;
        }
    }

    private static class StageContribution {
        int stageNumber;
        int crocCoinsContributed;
        int crocCoinsPerStage;

        public StageContribution(int stageNumber, int crocCoinsContributed, int crocCoinsPerStage) {
            this.stageNumber = stageNumber;
            this.crocCoinsContributed = crocCoinsContributed;
            this.crocCoinsPerStage = crocCoinsPerStage;
        }
    }

    public void contributeCrocCoins(int stageNumber, int contributedCrocCoins, int crocCoinsPerStage) {
        stageContributions.add(new StageContribution(stageNumber, contributedCrocCoins, crocCoinsPerStage));
    }

    public void addPotAfterStage(int currentPot) {
        potData.add(new PotData(currentPot));
    }

    public void addFee(int fee, String reason) {
        fees.add(new Fee(fee, reason));
    }

    public void setBuyIn(int buyIn) {
        this.buyIn = buyIn;
    }

    public void setCashout(int moneyToAdd) {
        cashout = moneyToAdd;
    }

    public void setPotPayout(int crocCoinsPerPerson, int crocCoinScoreAfterPotPayout) {
        potPayout = crocCoinsPerPerson;
        this.crocCoinScoreAfterPotPayout = crocCoinScoreAfterPotPayout;
    }


    public void setFinished() {
        finished = true;
    }


    @Override
    public String toString() {
        if (canceled) {
            return "The last " + VaultOfPublicGoods.NAME +
                    " has been canceled!";
        }
        StringBuilder s = new StringBuilder();
        if (finished) {
            s.append("# CrocDocs of your last " + VaultOfPublicGoods.NAME + "\n");
        } else {
            s.append("# CrocDocs of the current " + VaultOfPublicGoods.NAME + "\n");
        }
        s.append("Players participating: ").append(playersParticipating).append("\n");
        s.append("Funding goal: ").append(threshold).append("\n");
        if (buyIn >= 0) {
            s.append("Buy-in: ").append(buyIn).append(" money\n\n");
        }
        if (!stageContributions.isEmpty()) {
            s.append("## Stages:\n");
            for (int i = 0; i < stageContributions.size(); i++) {
                StageContribution contribution = stageContributions.get(i);
                s.append("- ").append(contribution.stageNumber - 1).append(": ")
                        .append(contribution.crocCoinsContributed)
                        .append(" CC contributed");
                if (potData.size() > i) {
                    PotData pot = potData.get(i);
                    s.append(", total funding after stage: ").append(pot.potAmount).append(" CC");
                }
                s.append("\n");
            }
        }
        if (!fees.isEmpty()) {
            s.append("## Fees:\n");
            for (Fee fee : fees) {
                s.append("- ").append(fee.amount).append(" money: ").append(fee.reason).append("\n");
            }
            s.append("\n");
        }

        if (potPayout > -1) {
            s.append("### Joint Lawsuit: ");
            if (potPayout == 0) {
                s.append("Failed! ");
            } else {
                s.append("Success!\nPayout: ")
                        .append(potPayout).append(" CC per person.\n");
            }
            s.append( "Total funding in the end: ")
                    .append(potData.get(potData.size()-1).potAmount)
                    .append("/").append(threshold).append(".\n");
            s.append("### Your final score: ").append(crocCoinScoreAfterPotPayout).append(" CC\n");
            s.append("=> Cashout: ").append(cashout).append(" money\n");
        }

        if (vaultMoneyWin > -1) {
            if (monocleActive) {
                s.append("### Money from vault loot: ~~").append(vaultMoneyWin).append("~~ 0 money (active CrocBank Inc. sponsorship)\n");
            }
        }

        int totalBalance = 0;
        if (buyIn > -1) {
            totalBalance -= buyIn;
        }
        for (Fee fee : fees) {
            totalBalance -= fee.amount;
        }
        if (cashout > -1) {
            totalBalance += cashout;
        }
        if (vaultMoneyWin > -1 && !monocleActive) {
            totalBalance += vaultMoneyWin;
        }
        if (totalBalance < 0) {
            s.append("\n## Total loss: ").append(-totalBalance).append(" money");
        } else {
            s.append("\n## Total profit: ").append(totalBalance).append(" money");
        }

        return s.toString();
    }

    public void setCanceled() {
        canceled = true;
    }

    public void setVaultMoneyWin(int vaultMoneyWin, boolean monocleActive) {
        this.vaultMoneyWin = vaultMoneyWin;
        this.monocleActive = monocleActive;
    }
}
