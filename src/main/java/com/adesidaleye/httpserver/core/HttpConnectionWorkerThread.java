package com.adesidaleye.httpserver.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Handles communication and messages on different thread
 */
public class HttpConnectionWorkerThread extends Thread{
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpConnectionWorkerThread.class);
    private Socket socket;

    public HttpConnectionWorkerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            // reading

            // writing to client
            String html = """
                    <html>
                    <head><title>Simple Java HTTP Server</title></head>
                    <body>
                        <h1>This page was served using a Simple Java HTTP Server</h1>
                    </body>
                    </html>
                    """;
            final String CRLF = "\r\n";

            String response =
                    "HTTP/1.1 200 OK" + CRLF + // Status line
                            "Content-Length: " + html.getBytes().length + CRLF + // Header
                            CRLF +
                            html;

            outputStream.write(response.getBytes());

            LOGGER.info("Connection Processing Finished");
        } catch (IOException e) {
            LOGGER.error("Connection Processing Failed", e);
        } finally {
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
}
