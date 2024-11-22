/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author DavidPrivat
 */
public class Storage {

    private final static String COUNTERS_PATH = "./src/data/counters.txt";
    public final static String CONFIG_PATH = "./src/data/config.txt";

    private HashMap<String,Counter> counters;

    public static String loadConfig() throws Exception {
        FileInputStream countersIn = new FileInputStream(CONFIG_PATH);
        return new String(countersIn.readAllBytes(), Charset.forName("UTF-8"));
    }

    public String loadJson() {
        try {
            FileInputStream countersIn = new FileInputStream(COUNTERS_PATH);
            String content = new String(countersIn.readAllBytes(), Charset.forName("UTF-8"));
            countersIn.close();
            
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public HashMap<String, Counter> loadCounters() {
        String asString = loadJson();
        Gson gson = new Gson();
        counters = gson.fromJson(asString, (new TypeToken<HashMap<String,Counter>>(){}).getType());
        if(counters == null) {
            counters = new HashMap<String,Counter>();
        }
        counters.forEach((String key, Counter counter)->{
            counter.init();
        });
        counters.forEach((String key, Counter counter)->{
            counter.initIncomingContracts(counters);
        });
        return counters;
    }
    
    public void safeCounters(HashMap<String, Counter> counters) {
        Gson gson = new Gson();
        String asString = gson.toJson(counters);
        /*
        JsonObject json = new JsonObject();
        counters.forEach((String key, Counter element)->{
            json.add(key, gson.toJsonTree(element.getScore()));
        });
*/
        safeJson(asString);
    }

    public void safeJson(String asString) {
        try {

            FileOutputStream countersOut = new FileOutputStream(COUNTERS_PATH);

            countersOut.write(asString.getBytes(Charset.forName("UTF-8")));//counters.toString().getBytes(Charset.forName("UTF-8")));
            countersOut.flush();
            countersOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
