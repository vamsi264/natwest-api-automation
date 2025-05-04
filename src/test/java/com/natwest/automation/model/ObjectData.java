// src/test/java/com/natwest/automation/model/ObjectData.java
package com.natwest.automation.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the data structure for an object used in the restful-api.dev API.
 * Uses Jackson annotations for JSON serialization/deserialization.
 * JsonInclude(JsonInclude.Include.NON_NULL) ensures that null fields are not included in the JSON payload.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObjectData {

    // Fields corresponding to the JSON structure of the API object
    private String name;
    private Data data;

    // Default constructor
    public ObjectData() {
    }

    // Getters and setters for the fields
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    /**
     * Inner class representing the nested 'data' object within the main object JSON.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Data {
        private Double price;
        @JsonProperty("CPU model") // Maps the JSON key "CPU model" to this field
        private String cpuModel;
        // Added capacity based on the first scenario, assuming it might be part of 'data'
        private String capacity;


        // Default constructor
        public Data() {
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public String getCpuModel() {
            return cpuModel;
        }

        public void setCpuModel(String cpuModel) {
            this.cpuModel = cpuModel;
        }

        public String getCapacity() {
            return capacity;
        }

        public void setCapacity(String capacity) {
            this.capacity = capacity;
        }
    }
}

