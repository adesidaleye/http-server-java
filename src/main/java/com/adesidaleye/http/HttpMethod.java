package com.adesidaleye.http;

public enum HttpMethod {
    GET, HEAD;
    public static final int MAX_LENGTH;

    // used here because MAX_LENGTH depends on looping over all the listed constants first to get its value
    static {
        int tempMaxLength = -1;

        for (HttpMethod method : values()) {
            int methodLen = method.name().length();

            if (methodLen > tempMaxLength) {
                tempMaxLength = methodLen;
            }
        }

        MAX_LENGTH = tempMaxLength;
    }
}
