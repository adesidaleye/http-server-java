package com.adesidaleye.httpserver;

import com.adesidaleye.httpserver.config.Configuration;
import com.adesidaleye.httpserver.config.ConfigurationManager;
import com.adesidaleye.httpserver.core.ServerListenerThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class HttpServer {
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

    public static void main(String[] args) {
        LOGGER.info("Server Running");

        // path is relative to the project root, so run from there, or it won't find the file
        ConfigurationManager.getInstance().loadConfigurationFile("src/main/resources/http.json");
        Configuration config = ConfigurationManager.getInstance().getCurrentConfiguration();

        LOGGER.info("Using port: {}", config.getPort());
        LOGGER.info("Using webRoot: {}", config.getWebroot());

        try {
            // constructor opens the port, throws exception if the port is already in use
            ServerListenerThread listenerThread = new ServerListenerThread(config.getPort(), config.getWebroot());

            // runs the accept() loop on its own thread, not on main
            listenerThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
