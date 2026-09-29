package com.adesidaleye.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum HttpVersion {
    // currently supported version
    HTTP_1_1("HTTP/1.1", 1, 1);

    public final String LITERAL;
    public final int MAJOR;
    public final int MINOR;

    HttpVersion(String LITERAL, int MAJOR, int MINOR) {
        this.LITERAL = LITERAL;
        this.MAJOR = MAJOR;
        this.MINOR = MINOR;
    }

    private static final Pattern httpVersionRegexPattern = Pattern.compile("^HTTP/(?<major>\\d)\\.(?<minor>\\d)");

    // takes the raw version string off, returns the closest HttpVersion currently supported
    public static HttpVersion getBestCompatibleVersion(String literalVersion) throws BadHttpVersionException {
        Matcher matcher = httpVersionRegexPattern.matcher(literalVersion);

        // reject outright if it doesn't look like HTTP/num1.num2
        if (!matcher.find() || matcher.groupCount() != 2) {
            throw new BadHttpVersionException();
        }

        int major = Integer.parseInt(matcher.group("major"));
        int minor = Integer.parseInt(matcher.group("minor"));

        HttpVersion tempBestCompatible = null;
        for (HttpVersion version : values()) {
            if (version.LITERAL.equals(literalVersion)) {
                return version;
            } else {
                if (version.MAJOR == major) {
                    /* only settle for an older minor version than what was requested
                    * since support of newer features isn't guaranteed*/
                    if (version.MINOR < minor) {
                        tempBestCompatible = version;
                    }
                }
            }
        }

        return tempBestCompatible;
    }
}
