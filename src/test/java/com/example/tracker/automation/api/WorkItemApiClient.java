package com.example.tracker.automation.api;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Thin wrapper around the work-item REST endpoints. Deliberately returns
 * REST Assured's own Response rather than a custom model class - tests own
 * their assertions on the raw HTTP status and JSON body, the same way
 * TrackerPage hands back Playwright's own Locator for the UI layer. Not a
 * dependency on the app's DTOs, just its observable HTTP contract.
 */
public class WorkItemApiClient {

    private final String baseUrl;

    public WorkItemApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Response listOwners() {
        return given().baseUri(baseUrl).get("/api/owners");
    }

    public Response list() {
        return given().baseUri(baseUrl).get("/api/items");
    }

    public Response create(String title, Long ownerId, String description) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("ownerId", ownerId);
        body.put("description", description);
        return given().baseUri(baseUrl)
                .contentType("application/json")
                .body(body)
                .post("/api/items");
    }

    public Response get(long id) {
        return given().baseUri(baseUrl).get("/api/items/{id}", id);
    }

    public Response update(long id, String title, Long ownerId, String description) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("ownerId", ownerId);
        body.put("description", description);
        return given().baseUri(baseUrl)
                .contentType("application/json")
                .body(body)
                .put("/api/items/{id}", id);
    }

    public Response patchStatus(long id, String status) {
        return given().baseUri(baseUrl)
                .contentType("application/json")
                .body(Map.of("status", status))
                .patch("/api/items/{id}/status", id);
    }
}
