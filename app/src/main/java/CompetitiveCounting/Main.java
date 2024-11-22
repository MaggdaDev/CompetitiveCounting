/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;


import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.User;

import org.antlr.v4.runtime.*;

import CompetitiveCounting.Parser.TradeOfferParser.*;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 *
 * @author DavidPrivat
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    private static CountingBot bot;

    public static void main(String[] args) {
       //String subj = "~tradeoffer c";
       //System.out.println(subj + " valid?" + String.valueOf(TradeOfferChecker.isValid(subj, null)));
        
        
        // test end
        System.out.println(TimeHandler.nowInEpochDay());
        GatewayDiscordClient client;
        try {
            
            String sec = Storage.loadConfig();
            sec = sec.replaceAll("\n", "");
            System.out.println(sec);
            client = DiscordClientBuilder.create(sec)
                .build()
                .login()
                .block();
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
        client.getEventDispatcher().on(ReadyEvent.class)
                .subscribe(event -> {
                    final User self = event.getSelf();
                    System.out.println(String.format(
                            "Logged in as %s#%s", self.getUsername(), self.getDiscriminator()
                    ));
                });
        
        bot = new CountingBot(client);
        MessageHandler messageHandler = new MessageHandler(bot);
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(messageHandler);
        ButtonClickHandler buttonHandler = new ButtonClickHandler(bot);
        client.getEventDispatcher().on(ButtonInteractionEvent.class).subscribe(buttonHandler);
        
        // database
        /*
        DatabaseConnection databaseConnection = new DatabaseConnection();
        databaseConnection.printAll();
*/
        
        // database end
        
        client.onDisconnect().block();
        
    }
    
}
