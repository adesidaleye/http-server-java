package com.adesidaleye.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpParser {
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpParser.class);

    // special characters in the HTTP format, as their ASCII codes
    private final static int SP = 0x20; // 32
    private final static int CR = 0x0D; // 13
    private final static int LF = 0x0A; // 10

    public HttpRequest parseHttpRequest(InputStream inputStream) throws HttpParsingException {
        // reader turns raw bytes from request into HTTP required ASCII chars
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.US_ASCII);
        HttpRequest request = new HttpRequest();

        try {
            parseRequestLine(reader, request);
        } catch (IOException e) {
            e.printStackTrace();

        }
        try {
            parseHeaders(reader, request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        parseBody(reader, request);

        return request;
    }

    // parse request line (method, path, version)
    private void parseRequestLine(InputStreamReader reader, HttpRequest request) throws HttpParsingException, IOException {
        // holds whatever item (method/target/version) is currently being read, cleared after each SP
        StringBuilder processingRequestBuffer = new StringBuilder();

        /* tracks which piece of the request line I'm currently reading
        * both false = reading method,
        * methodParsed true = reading target,
        * both true = reading version */
        boolean methodParsed = false;
        boolean requestTargetParsed = false;

        int _byte;
        while ((_byte = reader.read()) >= 0) {
            // CR followed by LF signals end of the line
            if (_byte == CR) {
                _byte = reader.read();

                if (_byte == LF) {
                    LOGGER.debug("Request Line VERSION to Process: {}", processingRequestBuffer.toString());

                    // line ended but method and/or target were never set, bad request
                    if (!methodParsed || !requestTargetParsed) {
                        throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
                    }

                    try {
                        request.setHttpVersion(processingRequestBuffer.toString());
                    } catch (BadHttpVersionException e) {
                        throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
                    }

                    return;
                } else {
                    // CR not followed by LF, not a valid line ending, bad request
                    throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
                }
            }

            if (_byte == SP) {
                // Process stored request data
                if (!methodParsed) {
                    LOGGER.debug("Request Line METHOD to Process: {}", processingRequestBuffer.toString());

                    request.setMethod(processingRequestBuffer.toString());
                    methodParsed = true;
                } else if (!requestTargetParsed) {
                    LOGGER.debug("Request Line REQ TARGET to Process: {}", processingRequestBuffer.toString());

                    request.setRequestTarget(processingRequestBuffer.toString());
                    requestTargetParsed = true;
                } else {
                    // a third space would mean a third piece, bad request
                    throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
                }

                // clear the buffer
                processingRequestBuffer.delete(0, processingRequestBuffer.length());
            } else {
                // not CR, LF, or SP, so it's a normal character, keep building the current piece
                processingRequestBuffer.append((char) _byte);

                if (!methodParsed) {
                    if (processingRequestBuffer.length() > HttpMethod.MAX_LENGTH) {
                        throw new HttpParsingException(HttpStatusCode.SERVER_ERROR_501_NOT_IMPLEMENTED);
                    }
                }
            }
        }
    }

    // reads header lines until the blank line that separates headers from the body
    private void parseHeaders(InputStreamReader reader, HttpRequest request) throws IOException, HttpParsingException {
        StringBuilder processingRequestBuffer = new StringBuilder();

        // tracks whether the previous line ended empty, since two CRLFs in a row means "headers are done"
        boolean crlfFound = false;

        int _byte;
        while ((_byte = reader.read()) >=0) {
            if (_byte == CR) {
                _byte = reader.read();
                if (_byte == LF) {
                    if (!crlfFound) {
                        // marks first CRLF seen, as this line had actual content, then process it as a header
                        crlfFound = true;

                        processSingleHeaderField(processingRequestBuffer, request);
                        processingRequestBuffer.delete(0, processingRequestBuffer.length());
                    } else {
                        // second CRLF in a row with nothing between signals end of headers
                        return;
                    }
                } else {
                    // CR not followed by LF, same bad-line-ending check as the request line
                    throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
                }
            } else {
                // got a real character, so this line isn't empty, reset the flag and keep building it
                crlfFound = false;
                processingRequestBuffer.append((char)_byte);
            }
        }
    }

    // validates and splits one raw header line, e.g. "Host: localhost:8080", into name + value
    private void processSingleHeaderField(StringBuilder processingDataBuffer, HttpRequest request) throws HttpParsingException {
        String rawHeaderField = processingDataBuffer.toString();
        Pattern pattern = Pattern.compile("^(?<fieldName>[!#$%&’*+\\-./^_‘|˜\\dA-Za-z]+):\\s?(?<fieldValue>[!#$%&’*+\\-./^_‘|˜(),:;<=>?@[\\\\]{}\" \\dA-Za-z]+)\\s?$");

        Matcher matcher = pattern.matcher(rawHeaderField);
        if (matcher.matches()) {
            String fieldName = matcher.group("fieldName");
            String fieldValue = matcher.group("fieldValue");
            request.addHeader(fieldName, fieldValue);
        } else{
            throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
        }
    }

    private void parseBody(InputStreamReader reader, HttpRequest request) {}
}
