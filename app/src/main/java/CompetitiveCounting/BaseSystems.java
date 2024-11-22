/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;
/**
 *
 * @author DavidPrivat
 */
public class BaseSystems {
    
    public static boolean isNumInSystem(String num, int base) {
        for(int i = 0; i < num.length(); i++) {
            char currChar = num.charAt(i);
            int digit = charToDigit(currChar);
            if(digit < 0 || (digit >= base && base != 1) || digit > base) {
                return false;
            }
        }
        return true;
    }
    
    public static int toDecimal(String num, int base) {
        int decimal = 0;
        for(int i = 0; i < num.length(); i++) {
            char currChar = num.charAt(num.length() - (1 + i));
            int digit = charToDigit(currChar);
            if(digit < 0 || (digit >= base && base != 1) || digit > base) {
                throw new NumberFormatException();
            }
            decimal += digit * (int)Math.pow(base, i);
        }
        return decimal;
    }
    
    public static String decimalToSystem(int dec, int base) {
        if(dec == 0) {
            return "0";
        }
        if(dec == 1) {
            return "1";
        }
        if(base == 1) {
            String ret = "";
            for(int i = 0; i < dec; i++) {
                ret += "1";
            }
            return ret;
        }
        String ret = "";
        int remaining = dec;
        boolean stillStartZeros = true;
        for(int i = (int)logn(dec, base); i >= 0; i--) {
            int digVal = (int)(remaining / Math.pow(base, i));
            remaining -= digVal * Math.pow(base,i);            
            ret += digitToChar(digVal);
        } 
        return ret;
    }
    
    private static double logn(double num, double base) {
        return Math.log(num) / Math.log(base);
    }
    
    public static char digitToChar(int digit) {
        if(digit >= 0 && digit <= 9) {
            return (char) (digit + 48);
        }
        if(digit > 9) {
            return (char) ((digit-10)+65);
        }
        throw new NumberFormatException();
    }
    
    public static int charToDigit(char c) {
        int unicode = (int)c;
        if(unicode >= 48 && unicode <= 57) {
            return unicode - 48;
        }
        if(unicode >= 65) {
            return 10 + unicode - 65;
        }
        return -1;
    }
}
