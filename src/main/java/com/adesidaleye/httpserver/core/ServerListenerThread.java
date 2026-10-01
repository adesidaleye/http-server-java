package com.adesidaleye.httpserver.core;

import com.adesidaleye.httpserver.core.io.WebRootHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerListenerThread extends Thread{
    private final static Logger LOGGER = LoggerFactory.getLogger(ServerListenerThread.class);

    private int port;
    private WebRootHandler webRootHandler;
    private ServerSocket serverSocket;

    public ServerListenerThread(int port, WebRootHandler webRootHandler) throws IOException {
        this.port = port;
        this.webRootHandler = webRootHandler;

        // binds to the port
        this.serverSocket = new ServerSocket(this.port);
    }

    @Override
    public void run() {
        try {
            // keep listening until the server socket is closed
            while (serverSocket.isBound() && !serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                LOGGER.info("Connection accepted: {}", socket.getInetAddress());

                // each client gets its own thread so this loop can go straight back to accept()
                HttpConnectionWorkerThread workerThread = new HttpConnectionWorkerThread(socket, webRootHandler);
                workerThread.start();
            }
        } catch (IOException e) {
            LOGGER.error("Failure connecting socket", e);
        } finally {
            // always release the port, even if the loop crashed
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close server socket", e);
                }
            }
        }
    }
}
