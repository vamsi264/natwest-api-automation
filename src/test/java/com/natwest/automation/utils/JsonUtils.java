// src/test/java/com/natwest/automation/utils/JsonUtils.java
package com.natwest.automation.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.natwest.automation.model.ObjectData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for handling JSON operations, such as reading payloads from files.
 */
public class JsonUtils {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Reads a JSON file from the classpath (resources folder) and returns its content as a String.
     *
     */
    public static String readJsonFileAsString(String filePath) {
        logger.info("Reading JSON file from classpath: {}", filePath);
        try (InputStream inputStream = JsonUtils.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                logger.error("Cannot find file on classpath: {}", filePath);
                throw new RuntimeException("Cannot find file on classpath: " + filePath);
            }
            // Reading the input stream into a String
            String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            logger.debug("Successfully read JSON content from {}: {}", filePath, jsonContent);
            return jsonContent;
        } catch (IOException e) {
            logger.error("Failed to read JSON file: {}", filePath, e);
            throw new RuntimeException("Failed to read JSON file: " + filePath, e);
        }
    }

    /**
     * Reads a JSON file from the classpath and parses it into a specified POJO class.
     *
     */
    public static <T> T readJsonFileAsObject(String filePath, Class<T> valueType) {
        String jsonContent = readJsonFileAsString(filePath);
        try {
            T parsedObject = objectMapper.readValue(jsonContent, valueType);
            logger.info("Successfully parsed JSON from {} into object of type {}", filePath, valueType.getSimpleName());
            return parsedObject;
        } catch (IOException e) {
            logger.error("Failed to parse JSON content from {} into type {}: {}", filePath, valueType.getSimpleName(), jsonContent, e);
            throw new RuntimeException("Failed to parse JSON file " + filePath + " into type " + valueType.getSimpleName(), e);
        }
    }

    // Example usage (can be removed or kept for testing)
    public static void main(String[] args) {
        // Example: Reading as String
        String jsonString = readJsonFileAsString("payloads/new_object.json");
        System.out.println("JSON as String:\n" + jsonString);

        // Example: Read as ObjectData POJO
        ObjectData objectData = readJsonFileAsObject("payloads/new_object.json", ObjectData.class);
        System.out.println("\nParsed ObjectData:");
        System.out.println("Name: " + objectData.getName());
        if (objectData.getData() != null) {
            System.out.println("Year: " + objectData.getData().getYear());
            System.out.println("Price: " + objectData.getData().getPrice());
            System.out.println("CPU Model: " + objectData.getData().getCpuModel());
            System.out.println("HDD Size: " + objectData.getData().getHardDiskSize());
        }
    }
}

