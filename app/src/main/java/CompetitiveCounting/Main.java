/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;


import CompetitiveCounting.items.Item;
import CompetitiveCounting.storage.LocalHttpServer;
import CompetitiveCounting.storage.Storage;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;

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
            sec = sec.replaceAll("\n", "");
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
