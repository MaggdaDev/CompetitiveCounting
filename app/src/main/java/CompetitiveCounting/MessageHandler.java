/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.EmojiData;
import discord4j.discordjson.json.ReactionData;
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
        if(messageChannel.getType() != Channel.Type.GUILD_TEXT) {
            return;
        }
        countingBot.message(message);
        
    }
}
