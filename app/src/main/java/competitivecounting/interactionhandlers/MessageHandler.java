/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competitivecounting.interactionhandlers;

import competitivecounting.CountingBot;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

import java.util.Optional;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

/**
 *
 * @author DavidPrivat
 */
public class MessageHandler extends BaseSubscriber<MessageCreateEvent>{
    
    private CountingBot countingBot;
    
    public MessageHandler(CountingBot bot) {
        countingBot = bot;
    }
    
    @Override
    public void hookOnSubscribe(Subscription subscription) {
        System.out.println("Subscribed");
        request(1);
    }
    
    @Override
    public void hookOnNext(MessageCreateEvent value) {
        handleMessage(value);
        request(1);
    }
    
    private void handleMessage(MessageCreateEvent event) {
        Message message = event.getMessage();
        Optional<User> opt = message.getAuthor();
        if(opt.isEmpty()) {
            return;
        }
        User author = opt.get();
        if(author.isBot()) {
            return;
        }
        MessageChannel messageChannel = message.getChannel().block();
        switch (messageChannel.getType()) {
            case GUILD_TEXT:
                countingBot.message(message);
                break;
            case DM:
                countingBot.getUserDMHandler().handleUserMessage(message);
                break;

        }
    }
}
