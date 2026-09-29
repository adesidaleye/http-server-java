package com.adesidaleye.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpVersionTest {

    // testing for exact match "HTTP/1/1"
    @Test
    void getBestCompatibleVersion() {
        HttpVersion version = null;
        try {
            version = HttpVersion.getBestCompatibleVersion("HTTP/1.1");
        } catch (BadHttpVersionException e) {
            e.printStackTrace();
            fail();
        }

        assertNotNull(version);
        assertEquals(HttpVersion.HTTP_1_1, version);
    }

    // testing for lowercase "http"
    @Test
    void getBestCompatibleVersionBadFormat() {
        try {
            HttpVersion.getBestCompatibleVersion("http/1.1");
            fail();
        } catch (BadHttpVersionException e) {}
    }

    // testing for if client requests for higher HTTP version
    @Test
    void getBestCompatibleVersionHigherVersion() {
        HttpVersion version = null;
        try {
            version = HttpVersion.getBestCompatibleVersion("HTTP/1.3");

            assertNotNull(version);
            assertEquals(HttpVersion.HTTP_1_1, version);
        } catch (BadHttpVersionException e) {
            fail();
        }
    }
}