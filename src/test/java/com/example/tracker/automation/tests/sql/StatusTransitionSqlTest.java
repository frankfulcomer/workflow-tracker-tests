package com.example.tracker.automation.tests.sql;

import com.example.tracker.automation.sql.BaseSqlTest;
import com.example.tracker.automation.sql.WorkItemSqlHelper;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatusTransitionSqlTest extends BaseSqlTest {

    private static Long validOwnerId;

    @BeforeAll
    static void discoverOwner() {
        validOwnerId = api.listOwners().jsonPath().getLong("[0].id");
    }

    @Test
    void validTransitionUpdatesStatusAndAddsOneHistoryRow() throws Exception {
        long id = createItem("valid transition");

        api.patchStatus(id, "IN_PROGRESS").then().statusCode(200);

        // WF-002 (NEW -> IN_PROGRESS allowed), WF-003 AC-1/AC-2/AC-3: the
        // persisted status changed and exactly one history row was appended.
        assertEquals("IN_PROGRESS", sql.findWorkItem(id).status());
        assertEquals(2, sql.countHistoryRows(id));

        WorkItemSqlHelper.HistoryRow latest = sql.latestHistoryRow(id);
        assertEquals("NEW", latest.previousStatus());
        assertEquals("IN_PROGRESS", latest.newStatus());
    }

    @Test
    void rejectedTransitionLeavesStatusAndHistoryUnchanged() throws Exception {
        long id = createItem("invalid transition");

        // NEW -> CLOSED is not an allowed transition.
        api.patchStatus(id, "CLOSED").then().statusCode(422);

        // WF-002 AC-12/AC-13, WF-003 AC-4: rejected at the app layer, and
        // nothing was written to storage - proven directly, not inferred
        // from the HTTP response alone.
        assertEquals("NEW", sql.findWorkItem(id).status());
        assertEquals(1, sql.countHistoryRows(id));
    }

    private long createItem(String label) {
        String title = "SQL test - " + label + " " + UUID.randomUUID();
        Response response = api.create(title, validOwnerId, "Created for status-transition SQL test");
        response.then().statusCode(201);
        return response.jsonPath().getLong("id");
    }
}
