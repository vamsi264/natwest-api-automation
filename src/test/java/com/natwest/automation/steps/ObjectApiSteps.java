// src/test/java/com/natwest/automation/steps/ObjectApiSteps.java
package com.natwest.automation.steps;

import com.natwest.automation.model.ObjectData;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.rest.SerenityRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Step definitions for the Objects API feature file.
 * Uses SerenityRest for making REST calls and managing state.
 */
public class ObjectApiSteps {

    private static final Logger logger = LoggerFactory.getLogger(ObjectApiSteps.class);

    private RequestSpecification request;
    private Response response;
    private ObjectData objectPayload; // Holds the data for the object being created
    private String createdObjectId; // Stores the ID of the object created in a scenario
    private String baseUrl;
    private String objectIdToDeleteOrGet; // Stores ID for specific GET/DELETE operations


    /**
     * Sets up the base URL for the API requests.
     * Reads from system property or defaults.
     */
    @Given("the API base URL is configured")
    public void theAPIBaseURLIsConfigured() {
        baseUrl = System.getProperty("serenity.base.url", "https://api.restful-api.dev");
        logger.info("API Base URL configured: {}", baseUrl);
        // Initialize request specification for subsequent steps
        request = SerenityRest.given()
                .baseUri(baseUrl)
                .contentType("application/json");
        objectPayload = new ObjectData(); // Initialize payload object
        objectPayload.setData(new ObjectData.Data()); // Initialize nested data object
    }

    /**
     * Initializes the object details for creation.
     */
    @Given("I have the details for a new object named {string}")
    public void iHaveTheDetailsForANewObjectNamed(String name) {
        objectPayload.setName(name);
        logger.info("Setting object name: {}", name);
    }

    /**
     * Sets the CPU model for the object data.
     */
    @Given("the object has {string} CPU model")
    public void theObjectHasCPUModel(String cpuModel) {
        objectPayload.getData().setCpuModel(cpuModel);
        logger.info("Setting CPU model: {}", cpuModel);
    }

    /**
     * Sets the price for the object data.
     */
    @Given("the object has a price of {double}")
    public void theObjectHasAPriceOf(Double price) {
        objectPayload.getData().setPrice(price);
        logger.info("Setting price: {}", price);
    }

    /**
     * Sets the capacity for the object data.
     */
    @Given("the object has a capacity of {string}")
    public void theObjectHasACapacityOf(String capacity) {
        objectPayload.getData().setCapacity(capacity);
        logger.info("Setting capacity: {}", capacity);
    }

    /**
     * Sends a POST request to the /objects endpoint with the prepared payload.
     */
    @When("I send a POST request to create the object")
    public void iSendAPOSTRequestToCreateTheObject() {
        logger.info("Sending POST request to /objects with payload: {}", objectPayload);
        response = request
                .body(objectPayload)
                .when()
                .post("/objects");
        logger.info("Received response: {}", response.getBody().asString());
    }

    /**
     * Verifies the HTTP response status code.
     */
    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int expectedStatusCode) {
        logger.info("Validating response status code. Expected: {}, Actual: {}", expectedStatusCode, response.getStatusCode());
        response.then().statusCode(expectedStatusCode);
        Serenity.recordReportData().withTitle("Received Response Status Code").andContents(String.valueOf(response.getStatusCode()));
    }

    /**
     * Verifies that the response body contains the details of the created/retrieved object.
     * Uses Hamcrest matchers for validation.
     */
    @Then("the response should contain the details of the created object")
    public void theResponseShouldContainTheDetailsOfTheCreatedObject() {
        logger.info("Validating response body contains created object details.");
        response.then()
                .body("id", notNullValue()) // Check that an ID is generated
                .body("name", equalTo(objectPayload.getName()))
                .body("createdAt", notNullValue()); // Check that createdAt timestamp exists
        // Optionally validate nested data fields if they are consistently returned
        if (objectPayload.getData() != null) {
            if (objectPayload.getData().getCpuModel() != null) {
                response.then().body("data.\"CPU model\"", equalTo(objectPayload.getData().getCpuModel()));
            }
            if (objectPayload.getData().getPrice() != null) {
                // Handle potential floating point comparison issues if necessary
                response.then().body("data.price", equalTo(objectPayload.getData().getPrice().floatValue()));
            }
            if (objectPayload.getData().getCapacity() != null) {
                response.then().body("data.capacity", equalTo(objectPayload.getData().getCapacity()));
            }
        }
    }

    /**
     * Verifies the name of the created object in the response.
     */
    @Then("the created object name should be {string}")
    public void theCreatedObjectNameShouldBe(String expectedName) {
        logger.info("Validating created object name. Expected: {}, Actual from response: {}", expectedName, response.jsonPath().getString("name"));
        response.then().body("name", equalTo(expectedName));
    }

    /**
     * Stores the ID of the created object from the response for later use.
     */
    @Then("the created object ID should be stored")
    public void theCreatedObjectIDShouldBeStored() {
        createdObjectId = response.jsonPath().getString("id");
        assertNotNull("Created object ID should not be null", createdObjectId);
        logger.info("Stored created object ID: {}", createdObjectId);
    }

    // --- Steps for GET, LIST, DELETE and Edge Cases ---

    /**
     * Creates a new object as a prerequisite for GET/DELETE tests.
     * Reuses existing steps for object creation.
     */
    @Given("a new object is created with name {string}")
    public void aNewObjectIsCreatedWithName(String name) {
        theAPIBaseURLIsConfigured(); // Ensure base setup
        iHaveTheDetailsForANewObjectNamed(name);
        theObjectHasCPUModel("Default CPU");
        theObjectHasAPriceOf(99.99);
        iSendAPOSTRequestToCreateTheObject();
        theResponseStatusCodeShouldBe(200);
        theCreatedObjectIDShouldBeStored(); // Store the ID of this newly created object
        objectIdToDeleteOrGet = createdObjectId; // Specifically store for the immediate GET/DELETE
        logger.info("Prerequisite: Created object with ID: {} for subsequent GET/DELETE", objectIdToDeleteOrGet);
    }

    /**
     * Sends a GET request to retrieve an object using the previously stored ID.
     */
    @When("I send a GET request to retrieve the object using its stored ID")
    public void iSendAGETRequestToRetrieveTheObjectUsingItsStoredID() {
        assertNotNull("Stored object ID must not be null for GET request", objectIdToDeleteOrGet);
        logger.info("Sending GET request to /objects/{}", objectIdToDeleteOrGet);
        response = request
                .when()
                .get("/objects/{id}", objectIdToDeleteOrGet);
        logger.info("Received response: {}", response.getBody().asString());
    }

    /**
     * Verifies the response contains details of the retrieved object.
     */
    @Then("the response should contain the details of the retrieved object")
    public void theResponseShouldContainTheDetailsOfTheRetrievedObject() {
        logger.info("Validating response body contains retrieved object details.");
        response.then()
                .body("id", equalTo(objectIdToDeleteOrGet)) // Verify the ID matches
                .body("name", notNullValue()); // Check name exists
    }

    /**
     * Verifies the name of the retrieved object.
     */
    @Then("the retrieved object name should be {string}")
    public void theRetrievedObjectNameShouldBe(String expectedName) {
        logger.info("Validating retrieved object name. Expected: {}, Actual: {}", expectedName, response.jsonPath().getString("name"));
        response.then().body("name", equalTo(expectedName));
    }

    /**
     * Sends a GET request to list all objects.
     */
    @When("I send a GET request to list all objects")
    public void iSendAGETRequestToListAllObjects() {
        logger.info("Sending GET request to /objects to list all");
        response = request
                .when()
                .get("/objects");
        logger.info("Received response: {}", response.getBody().asString());
    }

    /**
     * Verifies that the response list is not empty.
     */
    @Then("the response list should not be empty")
    public void theResponseListShouldNotBeEmpty() {
        logger.info("Validating response list is not empty.");
        response.then().body("$.size()", greaterThan(0)); // Check if the list has elements
    }

    /**
     * Verifies that the response list contains at least a certain number of objects.
     */
    @Then("the response list should contain at least {int} objects")
    public void theResponseListShouldContainAtLeastObjects(int minCount) {
        logger.info("Validating response list contains at least {} objects.", minCount);
        response.then().body("$.size()", greaterThanOrEqualTo(minCount));
    }

    /**
     * Sends a DELETE request to delete an object using the previously stored ID.
     */
    @When("I send a DELETE request to delete the object using its stored ID")
    public void iSendADELETERequestToDeleteTheObjectUsingItsStoredID() {
        assertNotNull("Stored object ID must not be null for DELETE request", objectIdToDeleteOrGet);
        logger.info("Sending DELETE request to /objects/{}", objectIdToDeleteOrGet);
        response = request
                .when()
                .delete("/objects/{id}", objectIdToDeleteOrGet);
        logger.info("Received response: {}", response.getBody().asString());
    }

    /**
     * Verifies the response message indicates successful deletion.
     */
    @Then("the response message should indicate successful deletion for the stored ID")
    public void theResponseMessageShouldIndicateSuccessfulDeletionForTheStoredID() {
        assertNotNull("Stored object ID must not be null for DELETE verification", objectIdToDeleteOrGet);
        logger.info("Validating successful deletion message for ID: {}", objectIdToDeleteOrGet);
        // This API returns a specific message format.
        String expectedMessage = String.format("Object with id = %s has been deleted.", objectIdToDeleteOrGet);
        response.then().body("message", equalTo(expectedMessage));
    }

    /**
     * Attempts to send a GET request for the ID that was supposedly deleted.
     */
    @When("I attempt to send a GET request for the deleted object ID")
    public void iAttemptToSendAGETRequestForTheDeletedObjectID() {
        assertNotNull("Stored object ID must not be null for GET-after-DELETE check", objectIdToDeleteOrGet);
        logger.info("Attempting GET request for deleted ID: /objects/{}", objectIdToDeleteOrGet);
        response = SerenityRest.given()
                .baseUri(baseUrl)
                .contentType("application/json") // Ensure content type if needed
                .when()
                .get("/objects/{id}", objectIdToDeleteOrGet);
        logger.info("Received response for GET after DELETE: {}", response.getBody().asString());
    }

    /**
     * Verifies the status code of the GET attempt made after a DELETE operation.
     */
    @Then("the response status code for the GET attempt should be {int}")
    public void theResponseStatusCodeForTheGETAttemptShouldBe(int expectedStatusCode) {
        logger.info("Validating status code for GET after DELETE. Expected: {}, Actual: {}", expectedStatusCode, response.getStatusCode());
        assertEquals("Status code for GET after DELETE mismatch", expectedStatusCode, response.getStatusCode());
    }

    // --- Edge Case Steps ---

    /**
     * Sets up an object payload with only a name (missing other fields).
     */
    @Given("I have the details for a new object with only the name {string}")
    public void iHaveTheDetailsForANewObjectWithOnlyTheName(String name) {
        theAPIBaseURLIsConfigured(); // Ensure base setup
        objectPayload = new ObjectData(); // Create a fresh payload
        objectPayload.setName(name);
        // Leaving intentionally the 'data' field null or empty
        objectPayload.setData(null); // null fields
        logger.info("Setting up incomplete object payload with name: {}", name);
    }

    /**
     * Stores a non-existent/invalid ID for testing GET/DELETE failure scenarios.
     */
    @Given("a non-existent object ID {string}")
    public void aNonExistentObjectID(String invalidId) {
        objectIdToDeleteOrGet = invalidId;
        logger.info("Using non-existent ID for test: {}", invalidId);
        theAPIBaseURLIsConfigured(); // Ensure base setup for the request
    }

    /**
     * Sends a GET request using an invalid/non-existent ID.
     */
    @When("I send a GET request to retrieve the object using the invalid ID")
    public void iSendAGETRequestToRetrieveTheObjectUsingTheInvalidID() {
        assertNotNull("Invalid object ID must not be null for GET request", objectIdToDeleteOrGet);
        logger.info("Sending GET request with invalid ID: /objects/{}", objectIdToDeleteOrGet);
        response = request
                .when()
                .get("/objects/{id}", objectIdToDeleteOrGet);
        logger.info("Received response for invalid GET: {}", response.getBody().asString());
    }

    /**
     * Sends a DELETE request using an invalid/non-existent ID.
     */
    @When("I send a DELETE request to delete the object using the invalid ID")
    public void iSendADELETERequestToDeleteTheObjectUsingTheInvalidID() {
        assertNotNull("Invalid object ID must not be null for DELETE request", objectIdToDeleteOrGet);
        logger.info("Sending DELETE request with invalid ID: /objects/{}", objectIdToDeleteOrGet);
        response = request
                .when()
                .delete("/objects/{id}", objectIdToDeleteOrGet);
        logger.info("Received response for invalid DELETE: {}", response.getBody().asString());
    }
}

