package com.hoatv.models;

import java.util.Random;

public class SaltGeneratorUtils {
    private static final String SALT_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final String SALT_NUMS = "1234567890";

    private SaltGeneratorUtils() {
    }

    public static String getSaltString(Integer length) {
        StringBuilder salt = new StringBuilder();
        return getSalt(length, salt, SALT_CHARS);
    }

    public static String getSaltString(Integer length, String startWith) {
        StringBuilder salt = new StringBuilder(startWith);
        return getSalt(length, salt, SALT_CHARS);
    }

    public static String getSaltNums(Integer length) {
        StringBuilder salt = new StringBuilder();
        return getSalt(length, salt, SALT_NUMS);
    }

    public static String getSaltNums(Integer length, String startWith) {
        StringBuilder salt = new StringBuilder(startWith);
        return getSalt(length, salt, SALT_NUMS);
    }

    private static String getSalt(int length, StringBuilder salt, String saltNums) {
        Random rnd = new Random();
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * saltNums.length());
            salt.append(saltNums.charAt(index));
        }
        return salt.toString();
    }
}
