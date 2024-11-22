/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author DavidPrivat
 */
public class Util {
    public static boolean isNumber(String in) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if(in == null) {
            return false;
        }
        return pattern.matcher(in).matches();
    }
    
    public static String[] splitAtFirst(String str, String splitter) {
        String[] splitted = str.split(splitter);
        if(splitted.length <= 1) {
            return splitted;
        } else {
            String snd = "";
            for(int i = 1; i < splitted.length; i++) {
                if(i > 1) {
                    snd += splitter;
                }
                snd += splitted[i];
            }
            return new String[]{splitted[0], snd};
        }
    }
}
