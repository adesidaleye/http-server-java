package com.adesidaleye.httpserver.core;

import com.adesidaleye.http.HttpParser;
import com.adesidaleye.http.HttpParsingException;
import com.adesidaleye.http.HttpRequest;
import com.adesidaleye.http.HttpStatusCode;
import com.adesidaleye.httpserver.core.io.ReadFileException;
import com.adesidaleye.httpserver.core.io.WebRootHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

// Handles communication and messages on different thread
public class HttpConnectionWorkerThread extends Thread{
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpConnectionWorkerThread.class);
    private Socket socket;

    /**
     * one shared WebRootHandler passed in, not created per-connection,
     * so every request hits the same resolved webroot and its path-traversal checks
     */
    private WebRootHandler webRootHandler;

    public HttpConnectionWorkerThread(Socket socket, WebRootHandler webRootHandler) {
        this.socket = socket;
        this.webRootHandler = webRootHandler;
    }

    @Override
    public void run() {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            final String CRLF = "\r\n"; // HTTP line ending

            try {
                // parse the raw bytes into method/target/headers
                HttpParser httpParser = new HttpParser();
                HttpRequest request = httpParser.parseHttpRequest(inputStream);

                String requestTarget = request.getRequestTarget();

                try {
                    // validate the path and check for traversal internally
                    byte[] fileBytes = webRootHandler.getFileByteArrayData(requestTarget);
                    String mimeType = webRootHandler.getFileMimeType(requestTarget);

                    String responseHeaders =
                            "HTTP/1.1 200 OK" + CRLF +
                            "Content-Type: " + mimeType + CRLF +
                            "Content-Length: " + fileBytes.length + CRLF +
                            CRLF;

                    // headers as text, body as raw bytes
                    outputStream.write(responseHeaders.getBytes());
                    outputStream.write(fileBytes);

                } catch (FileNotFoundException e) {
                    // file is missing, or caught by the path-traversal check
                    sendErrorResponse(outputStream, HttpStatusCode.CLIENT_ERROR_404_NOT_FOUND);
                } catch (ReadFileException e) {
                    // file exists but reading it failed
                    sendErrorResponse(outputStream, HttpStatusCode.SERVER_ERROR_500_INTERNAL_SERVER_ERROR);
                }
            } catch (HttpParsingException e) {
                // malformed request, reuse status code the thrown exception carries
                sendErrorResponse(outputStream, e.getErrorCode());
            }

            LOGGER.info("Connection Processing Finished");
        } catch (IOException e) {
            LOGGER.error("Connection Processing Failed", e);
        } finally {
            // close all streams and sockets, even if something failed above
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {}
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {}
            }

            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {}
            }
        }
    }

    // builds and sends a minimal error response using a real status code, so error responses all look the same shape
    public void sendErrorResponse(OutputStream outputStream, HttpStatusCode statusCode) throws IOException {
        final String CRLF = "\r\n";
        String body = "<html><body><h1>" + statusCode.STATUS_CODE + " " + statusCode.MESSAGE + "</h1></body></html>";

        String response =
                "HTTP/1.1 " + statusCode.STATUS_CODE + " " + statusCode.MESSAGE + CRLF +
                "Content-Type: text/html" + CRLF +
                "Content-Length: " + body.getBytes().length + CRLF +
                CRLF +
                body;

        outputStream.write(response.getBytes());
    }
}
