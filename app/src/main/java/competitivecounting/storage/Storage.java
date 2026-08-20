/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competitivecounting.storage;
import competitivecounting.interactionhandlers.CaptureHandler;
import competitivecounting.Counter;
import competitivecounting.CountingGuild;
import competitivecounting.CountingStreak;
import competitivecounting.items.equippables.Equippable;
import competitivecounting.items.equippables.EquippablesDeserializer;
import competitivecounting.rules.NumberRule;
import competitivecounting.rules.NumberRuleDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
    public final static String STREAKS_PATH = "./src/data/streaks.json";
    public final static String STREAKS_OLD_PATH = "./src/data/streaks_old.json";
    private HashMap<String, CountingGuild> guilds;
    private List<CaptureHandler.Capture> captures;
    private static Storage instance;
    private final HashMap<String, CountingStreak> streaks;

    static {
        checkFileExists(COUNTERS_PATH);
        checkFileExists(CAPTURES_PATH);
        checkFileExists(CONFIG_PATH);
    }

    private static void checkFileExists(String path) {
        File file = new File(path);
        if (!file.exists()) throw new RuntimeException("Required file not found: " + path);
    }

    /**
     *
     * @param streaks - global streaks map. If we find saved streaks, load them into this map.
     */
    public Storage(HashMap<String, CountingStreak> streaks) {
        this.streaks = streaks;
    }
    public static class Captures {
        public List<CaptureHandler.Capture> captures;
    }


    // Load:
    public static String loadConfig() throws Exception {
        FileInputStream countersIn = new FileInputStream(CONFIG_PATH);
        return new String(countersIn.readAllBytes(), StandardCharsets.UTF_8);
    }
    public HashMap<String, CountingGuild> loadGuilds() {
        if(guilds != null) {
            return guilds;
        }
        String asString = loadJsonStringFromFile(COUNTERS_PATH);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Equippable.class, new EquippablesDeserializer())
                .create();
        guilds = gson.fromJson(asString, (new TypeToken<HashMap<String,CountingGuild>>(){}).getType());
        if(guilds == null) {
            guilds = new HashMap<String,CountingGuild>();
        }
        guilds.forEach((String key, CountingGuild countingGuild)->{
            countingGuild.getCounters().forEach((String key2, Counter counter)->{
                counter.init(countingGuild.getGuildId());
            });
            countingGuild.init();
        });
        guilds.forEach((String key, CountingGuild countingGuild)->{
            countingGuild.getCounters().forEach((String key2, Counter counter)->{
                counter.getContractHandler().initIncomingContracts(countingGuild);
            });
            countingGuild.getBank().getContractHandler().initIncomingContracts(countingGuild);
        });
        return guilds;
    }

    public void loadStreaksIntoMapIfFilePresent() {
        if(!streakSaveFileFound()) {
            return;
        }
        String asString = loadJsonStringFromFile(STREAKS_PATH);
        if (asString == null || asString.isEmpty()) {
            return;
        }
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(NumberRule.class, new NumberRuleDeserializer())
                .create();
        HashMap<String, CountingStreak> loadedStreaks = gson.fromJson(asString, (new TypeToken<HashMap<String,CountingStreak>>(){}).getType());
        if(loadedStreaks != null) {
            loadedStreaks.forEach((key, streak) -> streak.initialize());
            streaks.putAll(loadedStreaks);
            System.out.println("Loaded " + loadedStreaks.size() + " streaks from file.");
            try {
                Files.move(Path.of(STREAKS_PATH), Path.of(STREAKS_OLD_PATH), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.err.println("Failed to move streaks file to streaks_old file.");
                e.printStackTrace();
            }
        } else {
            System.out.println("No streak save file found.");
        }
    }

    private boolean streakSaveFileFound() {
        Path path = Path.of(STREAKS_PATH);
        return Files.exists(path);
    }

    /**
     *    if captures don't exist:<br>
     *    write into src/data/captures.json:<br>
     *    {"captures": [ {"question":"test_captcha_answer_is_1","answer":1} ]}
     **/
    public List<CaptureHandler.Capture> loadCaptures() {

        if(captures != null) {
            return captures;
        }
        String asString = loadJsonStringFromFile(CAPTURES_PATH);
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
    public static String loadJsonStringFromFile(String path) {
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

    // Save
    public void save() {
        saveObjectAsJson(guilds, COUNTERS_PATH);
    }
    public void saveStreaks() {
        saveObjectAsJson(streaks, STREAKS_PATH);
    }
    private void saveObjectAsJson(Object obj, String path) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString;
        try {
            jsonString = gson.toJson(obj);
        } catch (Exception e) {
            System.err.println("Failed to convert object to json string:");
            e.printStackTrace();
            return;
        }
        try {

            FileOutputStream countersOut = new FileOutputStream(path);

            countersOut.write(jsonString.getBytes(StandardCharsets.UTF_8));
            countersOut.flush();
            countersOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
