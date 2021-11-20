package com.hoatv.controllers;

import java.util.Arrays;

public enum ExtSupportedMethod {
    GET,
    POST;

    public static final String INVALID_SUPPORTED_METHOD = "Invalid method name. Only support POST/GET";

    public static ExtSupportedMethod fromString(String methodName) {
        return Arrays.stream(ExtSupportedMethod.values()).filter(p-> p.name().equals(methodName)).findFirst().orElse(null);
    }

}
