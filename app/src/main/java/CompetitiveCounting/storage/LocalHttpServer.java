package CompetitiveCounting.storage;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class LocalHttpServer implements HttpHandler {
    private final static int PORT = 8080;
    private Runnable saveStreaksRunnable = () -> {};
    public LocalHttpServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/countingbotadmin/save", this);
        server.start();
        System.out.println("Local http server running on:  "+ PORT);
    }

    public void setSaveStreaksRunnable(Runnable saveStreaksRunnable) {
        this.saveStreaksRunnable = saveStreaksRunnable;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        System.out.println("Saving streaks...");
        byte[] response;
        try {
            saveStreaksRunnable.run();
            response = "Streaks saved!\n".getBytes();
            System.out.println("Streaks saved!");
            exchange.sendResponseHeaders(200, response.length);
        } catch (Exception e) {
            System.err.println("Failed to save streaks:");
            e.printStackTrace();
            response = "Failed to save streaks.\n".getBytes();
            exchange.sendResponseHeaders(500, response.length);
        }

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}
