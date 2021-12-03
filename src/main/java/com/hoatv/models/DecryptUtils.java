package com.hoatv.models;

import java.util.Base64;

public class DecryptUtils {

    private DecryptUtils() {
    }

    public static String decryptJWTBase64(String token) {
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        return new String(decoder.decode(chunks[1]));
    }
}
