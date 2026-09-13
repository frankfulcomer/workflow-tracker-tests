package com.example.tracker.automation.tests.api;

import com.example.tracker.automation.api.BaseApiTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

class StatusTransitionApiTest extends BaseApiTest {

    private static Long validOwnerId;

    @BeforeAll
    static void discoverOwner() {
        validOwnerId = api.listOwners().jsonPath().getLong("[0].id");
    }

    @Test
    void validStatusTransitionAddsOneHistoryEntry() {
        long id = createItem("valid transition");

        // WF-002 AC-4 (NEW -> IN_PROGRESS is allowed), WF-003 AC-1/AC-2/AC-3:
        // a legal transition succeeds and appends exactly one history entry.
        api.patchStatus(id, "IN_PROGRESS").then()
                .statusCode(200)
                .body("status", equalTo("IN_PROGRESS"))
                .body("statusHistory", hasSize(2))
                .body("statusHistory[1].previousStatus", equalTo("NEW"))
                .body("statusHistory[1].newStatus", equalTo("IN_PROGRESS"));

        api.get(id).then()
                .statusCode(200)
                .body("status", equalTo("IN_PROGRESS"))
                .body("statusHistory", hasSize(2));
    }

    @Test
    void invalidStatusTransitionIsRejectedAndUnchanged() {
        long id = createItem("invalid transition");

        // WF-002 AC-12/AC-13/AC-15, WF-003 AC-4: NEW -> CLOSED is not an
        // allowed transition, must leave status unchanged, and must not
        // create a history entry.
        api.patchStatus(id, "CLOSED").then()
                .statusCode(422)
                .body("error", not(emptyOrNullString()));

        api.get(id).then()
                .statusCode(200)
                .body("status", equalTo("NEW"))
                .body("statusHistory", hasSize(1));
    }

    @Test
    void sameStatusTransitionIsNoOpWithNoNewHistory() {
        long id = createItem("same status no-op");

        // WF-002 AC-14: re-selecting the current status succeeds as a no-op -
        // status unchanged, no new history entry.
        api.patchStatus(id, "NEW").then()
                .statusCode(200)
                .body("status", equalTo("NEW"))
                .body("statusHistory", hasSize(1));

        api.get(id).then()
                .statusCode(200)
                .body("status", equalTo("NEW"))
                .body("statusHistory", hasSize(1));
    }

    private long createItem(String label) {
        String title = "API test - " + label + " " + UUID.randomUUID();
        Response response = api.create(title, validOwnerId, "Created for status-transition test");
        response.then().statusCode(201);
        return response.jsonPath().getLong("id");
    }
}
