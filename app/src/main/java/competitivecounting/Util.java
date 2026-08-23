/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competitivecounting;

import java.util.regex.Pattern;

/**
 *
 * @author DavidPrivat
 */
public class Util {
    public static boolean isNumber(String in) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (in == null) {
            return false;
        }
        return pattern.matcher(in).matches();
    }

    public static String[] splitAtFirst(String str, String splitter) {
        String[] splitted = str.split(splitter);
        if (splitted.length <= 1) {
            return splitted;
        } else {
            String snd = "";
            for (int i = 1; i < splitted.length; i++) {
                if (i > 1) {
                    snd += splitter;
                }
                snd += splitted[i];
            }
            return new String[]{splitted[0], snd};
        }
    }

    public static String pingToUserId(String ping) {
        return ping.substring(2, ping.length() - 1);
    }

    public static String userIdToPing(String id) {
        return "<@" + id + ">";
    }

    public static String valueAndValueWithBoniToString(int value, int valueWithBoni) {
        if (value == valueWithBoni) {
            return String.valueOf(value);
        } else {
            return "~~" + value + "~~ " + valueWithBoni;
        }
    }

    public static String bonusMultToAddPercString(double bonusMultiplier) {
        return "+" + Math.round((bonusMultiplier - 1.0d) * 100.0d) + "%";
    }

    public static double multiplyProbabilityThreshold(double threshold, double multiplier) {
        return 1. - Math.pow(1. - threshold, multiplier);
    }
}
