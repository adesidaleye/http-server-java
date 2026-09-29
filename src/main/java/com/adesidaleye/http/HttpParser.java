package com.adesidaleye.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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

        parseRequestLine(reader, request);
        parseHeaders(reader, request);
        parseBody(reader, request);

        return request;
    }

    // parse request line (method, path, version)
    private void parseRequestLine(InputStreamReader reader, HttpRequest request) throws HttpParsingException {
        // holds whatever item (method/target/version) is currently being read, cleared after each SP
        StringBuilder processingRequestBuffer = new StringBuilder();

        /* tracks which piece of the request line I'm currently reading
        * both false = reading method,
        * methodParsed true = reading target,
        * both true = reading version */
        boolean methodParsed = false;
        boolean requestTargetParsed = false;

        try {
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
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failure Parsing Request Line", e);

            // if what's been read so far is already longer than any real method name, throw exception
            if (!methodParsed) {
                if (processingRequestBuffer.length() > HttpMethod.MAX_LENGTH) {
                    throw new HttpParsingException(HttpStatusCode.SERVER_ERROR_501_NOT_IMPLEMENTED);
                }
            }
        }
    }

    private void parseHeaders(InputStreamReader reader, HttpRequest request) {

    }

    private void parseBody(InputStreamReader reader, HttpRequest request) {

    }
}
