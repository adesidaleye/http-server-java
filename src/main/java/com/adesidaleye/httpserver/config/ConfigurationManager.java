package com.adesidaleye.httpserver.config;

import com.adesidaleye.httpserver.util.Json;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ConfigurationManager {
    private static ConfigurationManager configurationManager;
    private static Configuration currentConfiguration;

    private ConfigurationManager() {}

    public static ConfigurationManager getInstance() {
        if (configurationManager == null) {
            configurationManager = new ConfigurationManager();
        }

        return configurationManager;
    }

    /**
     * For loading a configuration file with the path given
     */
    public void loadConfigurationFile(String filePath) {
        // StringBuffer sb = new StringBuffer();
        StringBuilder sb = new StringBuilder();

        try (FileReader fileReader = new FileReader(filePath)) {
            int i;
            while ((i = fileReader.read()) != -1) {
                sb.append((char) i);
            }
        } catch (FileNotFoundException e) {
            throw new HttpConfigurationException("Configuration File Not Found", e);
        } catch (IOException e) {
            throw new HttpConfigurationException("Error Reading Configuration File", e);
        }

        JsonNode config = null;
        try {
            config = Json.parse(sb.toString());
        } catch (IOException e) {
            throw new HttpConfigurationException("Error parsing the Configuration File", e);
        }

        try {
            currentConfiguration = Json.fromJson(config, Configuration.class);
        } catch (JsonProcessingException e) {
            throw new HttpConfigurationException("Error parsing the Configuration File from Json", e);
        }
    }

    /**
     * Returns the current loaded configuration
     */
    public Configuration getCurrentConfiguration() {
        if (currentConfiguration == null) {
            throw new HttpConfigurationException("No Current Configuration Set.");
        }

        return currentConfiguration;
    }
}
