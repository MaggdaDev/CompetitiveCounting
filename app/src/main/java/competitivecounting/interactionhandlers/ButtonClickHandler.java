/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competitivecounting.interactionhandlers;

import competitivecounting.Counter;
import competitivecounting.CountingBot;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import reactor.core.publisher.BaseSubscriber;

/**
 *
 * @author DavidPrivat
 */
public class ButtonClickHandler extends BaseSubscriber<ButtonInteractionEvent>{
    
    private final CountingBot countingBot;
    public ButtonClickHandler(CountingBot bot) {
        countingBot = bot;
    }
    
    @Override
    public void hookOnNext(ButtonInteractionEvent event) {
        try {
            String customId = event.getCustomId();
            String userId = event.getInteraction().getUser().getId().asString();
            String guildId = event.getInteraction().getGuildId().get().asString();
            Counter counter = countingBot.getCounter(guildId, userId);
            if (counter != null) {
                event.reply(counter.buttonClick(customId, event.getMessage().get())).subscribe();
            }
        } catch (Exception ex) {
            System.err.println("Error in ButtonClickHandler:");
            ex.printStackTrace();
        }
    }
}
