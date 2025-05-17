/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;
import CompetitiveCounting.bank.Bank;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.lang.String;
import java.util.HashMap;

/**
 *
 * @author DavidPrivat
 */
public class Storage {

    private final static String COUNTERS_PATH = "./src/data/counters.json";
    private final static String CAPTURES_PATH = "./src/data/captures.json";
    public final static String CONFIG_PATH = "./src/data/config.txt";

    private HashMap<String, CountingGuild> guilds;
    private List<CaptureHandler.Capture> captures;
    private static Storage instance;

    public static Storage getInstance() {
        if(instance == null) {
            instance = new Storage();
        }
        if(instance.captures == null) {
            instance.loadCaptures();
        }
        if(instance.guilds == null) {
            instance.loadGuilds();
        }
        return instance;
    }

    public static String loadConfig() throws Exception {
        FileInputStream countersIn = new FileInputStream(CONFIG_PATH);
        return new String(countersIn.readAllBytes(), StandardCharsets.UTF_8);
    }

    public String loadJson(String path) {
        try {
            FileInputStream countersIn = new FileInputStream(path);
            String content = new String(countersIn.readAllBytes(), StandardCharsets.UTF_8);
            countersIn.close();
            
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public HashMap<String, CountingGuild> loadGuilds() {
        if(guilds != null) {
            return guilds;
        }
        String asString = loadJson(COUNTERS_PATH);
        Gson gson = new Gson();
        guilds = gson.fromJson(asString, (new TypeToken<HashMap<String,CountingGuild>>(){}).getType());
        if(guilds == null) {
            guilds = new HashMap<String,CountingGuild>();
        }
        guilds.forEach((String key, CountingGuild countingGuild)->{
            countingGuild.getCounters().forEach((String key2, Counter counter)->{
                counter.init();
            });
        });
        guilds.forEach((String key, CountingGuild countingGuild)->{
            countingGuild.getCounters().forEach((String key2, Counter counter)->{
                counter.initIncomingContracts(countingGuild);
            });
        });
        return guilds;
    }

    public List<CaptureHandler.Capture> loadCaptures() {
        if(captures != null) {
            return captures;
        }
        String asString = loadJson(CAPTURES_PATH);
        if (asString == null || asString.isEmpty()) {
            throw new RuntimeException("Captures not found");
        }
        Gson gson = new Gson();
        Captures capturesObjects = gson.fromJson(asString, (new TypeToken<Captures>(){}).getType());
        captures = capturesObjects.captures;
        if(captures == null) {
            throw new RuntimeException("Captures not found");
        }
        return captures;
    }

    public static class Captures {
        public List<CaptureHandler.Capture> captures;
    }

    public void save() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(guilds);
        try {

            FileOutputStream countersOut = new FileOutputStream(COUNTERS_PATH);

            countersOut.write(jsonString.getBytes(StandardCharsets.UTF_8));
            countersOut.flush();
            countersOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
