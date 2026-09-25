/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competitivecounting;


import competitivecounting.interactionhandlers.ButtonClickHandler;
import competitivecounting.interactionhandlers.MessageHandler;
import competitivecounting.items.Item;
import competitivecounting.storage.LocalHttpServer;
import competitivecounting.storage.Storage;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.User;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;

import java.io.IOException;

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
        GatewayDiscordClient client;
        try {
            
            String sec = Storage.loadConfig();
            sec = sec.replace("\n", "");
            client = DiscordClientBuilder.create(sec)
                    .build()
                    .gateway()
                    .setEnabledIntents(IntentSet.of(Intent.GUILD_MESSAGES, Intent.MESSAGE_CONTENT, Intent.DIRECT_MESSAGES))
                    .login()
                    .block();
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
        client.getEventDispatcher().on(ReadyEvent.class)
                .subscribe(event -> {
                    final User self = event.getSelf();
                    System.out.printf(
                            "Logged in as %s%n", self.getUsername()
                    );
                });
        LocalHttpServer httpServer;
        try {
            httpServer = new LocalHttpServer();
        } catch (IOException e) {
            System.err.println("Failed to start local HTTP server:");
            e.printStackTrace();
            return;
        }
        Item.initializeItems();
        bot = new CountingBot(client);
        httpServer.setSaveStreaksRunnable(bot::saveCountersAndStreaks);
        MessageHandler messageHandler = new MessageHandler(bot);
        bot.registerMessageHandler(messageHandler);
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
