package com.example.bankcards.util;

public class LuhnUtil {
    private LuhnUtil() {}

    public static int calculateLuhnDigit(String numberWithoutCheck) {
        int sum = 0;
        boolean doubleDigit = true;

        for (int i = numberWithoutCheck.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(numberWithoutCheck.charAt(i));
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }

        int mod = sum % 10;
        return mod == 0 ? 0 : 10 - mod;
    }
}
