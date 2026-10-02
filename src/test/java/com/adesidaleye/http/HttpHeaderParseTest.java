package com.adesidaleye.http;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpHeaderParseTest {
    private HttpParser httpParser;
    // holds a reflective handle to the private parseHeaders method
    private Method parseHeaderMethod;

    @BeforeAll
    public void beforeClass() throws NoSuchMethodException {
        httpParser = new HttpParser();

        Class<HttpParser> clss = HttpParser.class;
        parseHeaderMethod = clss.getDeclaredMethod("parseHeaders", InputStreamReader.class, HttpRequest.class);

        // bypasses the "private" access check so invoke() below is actually allowed to call it
        parseHeaderMethod.setAccessible(true);
    }

    // one header line, should end up with exactly one entry in the map
    @Test
    public void testSimpleSingleHeader() throws InvocationTargetException, IllegalAccessException {
        HttpRequest request = new HttpRequest();
        parseHeaderMethod.invoke(httpParser, generateSingleHeaderMessage(), request);

        assertEquals(1, request.getHeaderNames().size());
        assertEquals("localhost:8080", request.getHeader("host"));
    }

    // several header lines, but this only checks the Host header specifically
    @Test
    public void testMultipleHeader() throws InvocationTargetException, IllegalAccessException {
        HttpRequest request = new HttpRequest();
        parseHeaderMethod.invoke(httpParser, generateMultipleHeadersMessage(), request);

        assertEquals(10, request.getHeaderNames().size());
        assertEquals("localhost:8080", request.getHeader("host"));
    }

    @Test
    public void testSpaceBeforeColonErrorHeader() throws IllegalAccessException {
        try {
            HttpRequest request = new HttpRequest();
            parseHeaderMethod.invoke(httpParser, generateSpaceBeforeColonErrorHeaderMessage(), request);

            // if invoke() didn't throw, the regex wrongly accepted "Host :", so fail the test
            fail();
        } catch (InvocationTargetException e) {
            // reflection wraps the real exception inside InvocationTargetException, unwrap with getCause()
            if (e.getCause() instanceof HttpParsingException) {
                assertEquals(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST, ((HttpParsingException) e.getCause()).getErrorCode());
            } else {
                // some other, unexpected exception type was thrown instead
                fail(e.getCause());
            }
        }
    }

    private InputStreamReader generateSingleHeaderMessage() {
        String rawData =
                "Host: localhost:8080\r\n" +
                // "Connection: keep-alive\r\n" +
                // "Cache-Control: max-age=0\r\n" +
                // "sec-ch-ua: \"Chromium\";v=\"154\", \"Brave\";v=\"154\", \"Not A(Brand\";v=\"99\"\r\n" +
                // "sec-ch-ua-mobile: ?0\r\n" +
                // "sec-ch-ua-platform: \"Windows\"\r\n" +
                // "Upgrade-Insecure-Requests: 1\r\n" +
                // "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/154.0.0.0 Safari/537.36\r\n" +
                // "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8\r\n" +
                // "Sec-GPC: 1\r\n" +
                // "Sec-Fetch-Site: none\r\n" +
                // "Sec-Fetch-Mode: navigate\r\n" +
                // "Sec-Fetch-User: ?1\r\n" +
                // "Sec-Fetch-Dest: document\r\n" +
                // "Accept-Encoding: gzip, deflate, br, zstd\r\n" +
                // "Accept-Language: en-US,en;q=0.7\r\n" +
                "\r\n";

        InputStream inputStream = new ByteArrayInputStream(rawData.getBytes(StandardCharsets.US_ASCII)); // US_ASCII because HTTP headers are plain ASCII

        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.US_ASCII);
        return reader;
    }

    private InputStreamReader generateMultipleHeadersMessage() {
        String rawData = "Host: localhost:8080\r\n" +
                "Connection: keep-alive\r\n" +
                "Upgrade-Insecure-Requests: 1\r\n" +
                "User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36\r\n" +
                "Sec-Fetch-User: ?1\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\r\n" +
                "Sec-Fetch-Site: none\r\n" +
                "Sec-Fetch-Mode: navigate\r\n" +
                "Accept-Encoding: gzip, deflate, br\r\n" +
                "Accept-Language: en-US,en;q=0.9,es;q=0.8,pt;q=0.7,de-DE;q=0.6,de;q=0.5,la;q=0.4\r\n" +
                "\r\n";

        InputStream inputStream = new ByteArrayInputStream(rawData.getBytes(StandardCharsets.US_ASCII));

        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.US_ASCII);
        return reader;
    }

    private InputStreamReader generateSpaceBeforeColonErrorHeaderMessage() {
        String rawData = "Host : localhost:8080\r\n" +
                "\r\n";

        InputStream inputStream = new ByteArrayInputStream(rawData.getBytes(StandardCharsets.US_ASCII));

        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.US_ASCII);
        return reader;
    }

}