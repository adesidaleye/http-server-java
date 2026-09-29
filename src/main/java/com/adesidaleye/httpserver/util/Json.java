package com.adesidaleye.httpserver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;

// wrapper around Jackson so the rest of the project never touches it directly
public class Json {
    private static ObjectMapper objectMapper = defaultObjectMapper();

    private static ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // ignore extra JSON fields that Configuration doesn't have, instead of crashing
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }

    // raw JSON string to generic JsonNode tree
    public static JsonNode parse(String jsonSrc) throws IOException {
        return objectMapper.readTree(jsonSrc);
    }

    // maps JsonNode tree to any class passed in
    public static <T> T fromJson(JsonNode node, Class<T> tClass) throws JsonProcessingException {
        return objectMapper.treeToValue(node, tClass);
    }

    // any Java object to JsonNode tree
    public static JsonNode toJson(Object object) {
        return objectMapper.valueToTree(object);
    }

    // JsonNode to compact JSON string
    public static String stringify(JsonNode node) throws JsonProcessingException {
        return generateJson(node, false);
    }

    // JsonNode to indented, readable JSON string
    public static String stringifyPretty(JsonNode node) throws JsonProcessingException {
        return generateJson(node, true);
    }

    // private helper for stringify/stringifyPretty use
    private static String generateJson(Object obj, boolean pretty) throws JsonProcessingException {
        ObjectWriter objectWriter = objectMapper.writer();

        if (pretty) {
            // INDENT_OUTPUT adds the line breaks and indentation
            objectWriter = objectWriter.with(SerializationFeature.INDENT_OUTPUT);
        }

        return objectWriter.writeValueAsString(obj);
    }
}
