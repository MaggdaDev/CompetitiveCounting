/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;

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
        String customId = event.getCustomId();
        String userId = event.getInteraction().getUser().getId().asString();
        Counter counter = countingBot.getCounter(userId);
        if(counter != null) {
            event.reply(counter.buttonClick(customId, event.getMessage().get())).subscribe();
        }
    }
}
