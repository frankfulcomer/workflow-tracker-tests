package com.example.tracker.automation.tests.api;

import com.example.tracker.automation.api.BaseApiTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkItemCreationApiTest extends BaseApiTest {

    private static Long validOwnerId;

    @BeforeAll
    static void discoverOwner() {
        validOwnerId = api.listOwners().jsonPath().getLong("[0].id");
    }

    @Test
    void createsItemWithSuppliedValuesNewStatusAndInitialHistoryEntry() {
        String title = "API test - create " + UUID.randomUUID();
        String description = "Created via REST Assured API test";

        Response created = api.create(title, validOwnerId, description);

        // WF-006 AC-5/AC-6, WF-001 AC-7, WF-003 AC-7: the created item carries the
        // supplied values, starts in NEW, and has exactly one initial history
        // entry with no previous status.
        created.then()
                .statusCode(201)
                .body("title", equalTo(title))
                .body("description", equalTo(description))
                .body("owner.id", equalTo(validOwnerId.intValue()))
                .body("status", equalTo("NEW"))
                .body("statusHistory", hasSize(1))
                .body("statusHistory[0].previousStatus", nullValue())
                .body("statusHistory[0].newStatus", equalTo("NEW"));

        long id = created.jsonPath().getLong("id");

        // Confirm it's actually persisted (WF-006 AC-7), not just echoed in the
        // POST response.
        api.get(id).then()
                .statusCode(200)
                .body("title", equalTo(title))
                .body("status", equalTo("NEW"))
                .body("statusHistory", hasSize(1));
    }

    @Test
    void rejectsCreationWithNonexistentOwner() {
        int countBefore = api.list().jsonPath().getList("$").size();

        String title = "API test - nonexistent owner " + UUID.randomUUID();
        Response response = api.create(title, Long.MAX_VALUE, "Should not be created");

        // WF-006 AC-12: an owner id that doesn't identify an existing owner is
        // rejected and creates nothing.
        response.then().statusCode(400);

        int countAfter = api.list().jsonPath().getList("$").size();
        assertEquals(countBefore, countAfter, "creation with a nonexistent owner must not create a work item");
    }

    @Test
    void rejectsCreationWithBlankOrWhitespaceTitle() {
        int countBefore = api.list().jsonPath().getList("$").size();

        // WF-006 AC-11: blank and whitespace-only titles are both rejected.
        api.create("", validOwnerId, "Should not be created (blank title)")
                .then().statusCode(400);
        api.create("   ", validOwnerId, "Should not be created (whitespace-only title)")
                .then().statusCode(400);

        int countAfter = api.list().jsonPath().getList("$").size();
        assertEquals(countBefore, countAfter, "creation with a blank/whitespace title must not create a work item");
    }
}
