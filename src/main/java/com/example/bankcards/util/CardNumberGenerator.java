package com.example.bankcards.util;

import org.apache.commons.lang3.RandomStringUtils;

public class CardNumberGenerator {
    private static final String BIN = "400000";

    private CardNumberGenerator() {}

    public static String generate() {
        String accountPart = RandomStringUtils.randomNumeric(9);
        String partial = BIN + accountPart;

        int checkDigit = LuhnUtil.calculateLuhnDigit(partial);
        return partial + checkDigit;
    }
}
