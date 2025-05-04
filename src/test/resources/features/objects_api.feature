# Feature file defining scenarios for the Objects API (restful-api.dev)

@ApiTests
Feature: Objects API CRUD Operations
  As a user of the restful-api.dev service
  I want to perform Create, Read, List, and Delete operations on objects
  So that I can manage object data effectively

  Background:
    Given the API base URL is configured

  @PostObject @Positive
  Scenario: Verify a new object can be created successfully
    Given I have the details for a new object named "Apple MacBook Pro 16"
    And the object has "Intel Core i9" CPU model
    And the object has a price of 1849.99
    And the object has a capacity of "1 TB"
    When I send a POST request to create the object
    Then the response status code should be 200
    And the response should contain the details of the created object
    And the created object name should be "Apple MacBook Pro 16"
    And the created object ID should be stored

  @GetObject @Positive
  Scenario: Verify an existing object can be retrieved by ID
    Given a new object is created with name "Google Pixel 6 Pro"
    When I send a GET request to retrieve the object using its stored ID
    Then the response status code should be 200
    And the response should contain the details of the retrieved object
    And the retrieved object name should be "Google Pixel 6 Pro"

  @ListObjects @Positive
  Scenario: Verify multiple objects can be listed|
    When I send a GET request to list all objects
    Then the response status code should be 200
    And the response list should not be empty
    And the response list should contain at least 2 objects

  @DeleteObject @Positive
  Scenario: Verify an existing object can be deleted by ID
    Given a new object is created with name "Object To Be Deleted"
    When I send a DELETE request to delete the object using its stored ID
    Then the response status code should be 200
    And the response message should indicate successful deletion for the stored ID
    When I attempt to send a GET request for the deleted object ID
    Then the response status code for the GET attempt should be 404

  @PostObject @Negative @EdgeCase
  Scenario: Verify creating an object with missing mandatory fields
    Given I have the details for a new object with only the name "Incomplete Object"
    When I send a POST request to create the object
    Then the response status code should be 200

  @GetObject @Negative @EdgeCase
  Scenario: Verify retrieving an object with an invalid or non-existent ID
    Given a non-existent object ID "invalid-id-123"
    When I send a GET request to retrieve the object using the invalid ID
    Then the response status code should be 404

  @DeleteObject @Negative @EdgeCase
  Scenario: Verify deleting an object with an invalid or non-existent ID
    Given a non-existent object ID "invalid-id-456"
    When I send a DELETE request to delete the object using the invalid ID
    Then the response status code should be 404

