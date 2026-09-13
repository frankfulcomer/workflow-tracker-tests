package com.example.tracker.automation.tests.sql;

import com.example.tracker.automation.sql.BaseSqlTest;
import com.example.tracker.automation.sql.WorkItemSqlHelper;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WorkItemPersistenceSqlTest extends BaseSqlTest {

    private static Long validOwnerId;
    private static String validOwnerName;

    @BeforeAll
    static void discoverOwner() {
        Response owners = api.listOwners();
        validOwnerId = owners.jsonPath().getLong("[0].id");
        validOwnerName = owners.jsonPath().getString("[0].name");
    }

    @Test
    void createdItemPersistsSuppliedValuesAndOwnerRelationship() throws Exception {
        String title = "SQL test - persisted values " + UUID.randomUUID();
        String description = "Verifying persistence via direct SQL";

        long id = createItem(title, validOwnerId, description);

        // WF-001 AC-1/AC-3, WF-006 AC-5: a JOIN across the owner FK proves the
        // relationship is actually stored, not just returned by the API.
        WorkItemSqlHelper.WorkItemRow row = sql.findWorkItem(id);
        assertEquals(title, row.title());
        assertEquals(description, row.description());
        assertEquals("NEW", row.status());
        assertEquals(validOwnerId, row.ownerId());
        assertEquals(validOwnerName, row.ownerName());
    }

    @Test
    void creationPersistsExactlyOneInitialHistoryRow() throws Exception {
        long id = createItem("SQL test - initial history " + UUID.randomUUID(), validOwnerId,
                "Verifying the initial history row via direct SQL");

        // WF-003 AC-2/AC-7: exactly one row, no previous status, NEW as the new status.
        assertEquals(1, sql.countHistoryRows(id));

        WorkItemSqlHelper.HistoryRow onlyRow = sql.historyRows(id).get(0);
        assertNull(onlyRow.previousStatus());
        assertEquals("NEW", onlyRow.newStatus());
    }

    @Test
    void ownerAssignmentAndUnassignedAreRepresentedCorrectly() throws Exception {
        long assignedId = createItem("SQL test - assigned owner " + UUID.randomUUID(), validOwnerId,
                "Should have a stored owner relationship");
        long unassignedId = createItem("SQL test - unassigned owner " + UUID.randomUUID(), null,
                "Should have no stored owner relationship");

        // WF-001 AC-1: assigned owner is stored as the matching foreign key.
        assertEquals(validOwnerId, sql.findWorkItem(assignedId).ownerId());

        // WF-001 AC-2/AC-9: no assigned owner means no stored owner relationship (NULL FK).
        assertNull(sql.findWorkItem(unassignedId).ownerId());
    }

    private long createItem(String title, Long ownerId, String description) {
        Response response = api.create(title, ownerId, description);
        response.then().statusCode(201);
        return response.jsonPath().getLong("id");
    }
}
