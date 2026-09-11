package competitivecounting.items.equippables;

import competitivecounting.*;
import competitivecounting.bank.BankCommandHandler;
import competitivecounting.dialogue.Dialogue;
import competitivecounting.vaults.CommunityVault;
import competitivecounting.vaults.publicgoodsvault.VaultOfPublicGoods;
import discord4j.core.object.entity.Message;

import java.util.Objects;

public class SponsoredMonocle extends Equippable implements VaultRateModifier, TrophyRateModifier {
    public final static String NAME = "Sponsored<:monocle:1548071169553338411>Monocle";
    private final static String DESCRIPTION = "When equipped, can be used to activate a sponsorship by the CrocBank Inc. for selected streaks!";
    private final static String COLLECTION_DESCRIPTION = "_Use_ to activate a sponsorship by the CrocBank Inc. for this streak.\n"
            + "-# Money paid to CrocBank Inc.: {0}         Current bonus trophy/vault rate: {2}";
    private double moneyToCrocBankInc = 0;
    public final static double BASE_TROPHY_AND_VAULT_RATE_MULTIPLIER = 1.2; // TODO auto-upgrading

    public SponsoredMonocle(Counter owner) {
        super(new Price(705050), NAME, DESCRIPTION, owner);
    }

    @Override
    public String getCollectionDescription() {
        return COLLECTION_DESCRIPTION.replace("{0}", String.valueOf((int) moneyToCrocBankInc))
                .replace("{2}", Util.bonusMultToAddPercString(getTrophyVaultRateMultiplier()));
    }

    @Override
    public Equippable createObject(Counter owner) {
        return new SponsoredMonocle(owner);
    }

    @Override
    public double modifyTrophyRate(double rate, CountingContext context) {
        return modifyRate(rate, context);
    }

    @Override
    public boolean doCollectionUse(Message message, CountingContext context) {
        if (context.getStreak().getCounterIdsOfActiveSponsoredMonocles().contains(owner.getId())) {
            CountingBot.write(message, "Your CrocBank Inc. sponsorship is already active for this streak!");
            return true;
        }
        context.getStreak().getCounterIdsOfActiveSponsoredMonocles().add(owner.getId());
        new Dialogue()
                .addNpcLine("Oh, what an exquisite " + NAME + "!", 2500)
                .addNpcLine("Your CrocBank Inc. sponsorship is now active until the end of this streak!\n" +
                "Any money dropped by vaults will go to the CrocBank Inc., but you receive the following benefits:\n" +
                "- Bonus trophy and vault rate: " + Util.bonusMultToAddPercString(getTrophyVaultRateMultiplier()) + "\n" +
                "- if the participating counters are wealthy enough: A " + CommunityVault.NAME +
                " spawning on your count will be converted to a " + VaultOfPublicGoods.NAME + ".\n" + VaultOfPublicGoods.SPONSOR_TAG, 0)
                .setNpcLineConverter(BankCommandHandler::toCrocText)
                .play(message);

        return true;
    }

    private double modifyRate(double rate, CountingContext context) {
        if (context.getCounter() != owner) {
            return rate;
        }
        if (!context.getStreak().getCounterIdsOfActiveSponsoredMonocles().contains(owner.getId())) {
            System.out.println("Do not modify rate, as monocle of " + owner.getName() + " is not active in this streak.");
            return rate;
        }
        System.out.println("Monocle: Modify rate from " + rate + " to " + Util.multiplyProbabilityThreshold(rate, getTrophyVaultRateMultiplier()));
        return Util.multiplyProbabilityThreshold(rate, getTrophyVaultRateMultiplier());
    }

    @Override
    public double modifyVaultRate(double rate, CountingContext context) {
        return modifyRate(rate, context);
    }

    private double getTrophyVaultRateMultiplier() {
        return BASE_TROPHY_AND_VAULT_RATE_MULTIPLIER;
    }

    public void notifyMoneyTransfer(int money) {
        moneyToCrocBankInc += money;
    }
}
