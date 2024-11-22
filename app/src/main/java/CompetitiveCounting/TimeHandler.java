/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/**
 *
 * @author DavidPrivat
 */
public class TimeHandler {
    
    public static boolean isYesterday(long epochDay) {
        if(getDaysBetween(epochDay) == 1) {
            return true;
        } else {
            return false;
        }
    }
    
    public static long getDaysBetween(long epochDay) {
        return LocalDate.now().minusDays(epochDay).toEpochDay();
    }
    
    public static long nowInEpochDay() {
        return LocalDate.now().toEpochDay();
    }
    
    public static long minutesUntilTomorrow() {
        return LocalTime.now().until(LocalTime.MAX, ChronoUnit.MINUTES);
    }
    
    public static String timeUntilTomorrowString() {
        long untilTom = minutesUntilTomorrow();
        String hours = String.valueOf((int)(untilTom/60.0d));
        String minutes = String.valueOf(untilTom % 60);
        return hours + "h" + minutes + "min";
    }
    
}
