package com.example.tracker.automation.tests;

import com.example.tracker.automation.BaseUiTest;
import com.example.tracker.automation.pageobjects.TrackerPage;
import com.microsoft.playwright.Dialog;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class WorkItemDetailTest extends BaseUiTest {

    @Test
    void editsAndSavesWorkItemDetails() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        String originalTitle = "Automated test - edit before " + UUID.randomUUID();
        tracker.createItem(originalTitle, "FirstName1 LastName1", "Original description");

        tracker.openDetail(originalTitle);
        // WF-005 AC-1: a single Save Changes button covers all editable fields.
        assertThat(tracker.saveChangesButton()).hasCount(1);
        // WF-005 AC-2: Save Changes starts disabled - nothing has been edited yet.
        assertThat(tracker.saveChangesButton()).isDisabled();
        // WF-004 AC-4 / WF-005 AC-7: only the initial creation history row exists
        // before any edit is made.
        assertThat(tracker.historyRows()).hasCount(1);

        String newTitle = "Automated test - edit after " + UUID.randomUUID();
        String newDescription = "Updated description " + UUID.randomUUID();
        tracker.setDetailTitle(newTitle);
        tracker.setDetailOwner("FirstName2 LastName2");
        tracker.setDetailDescription(newDescription);

        // WF-005 AC-3: changing title, owner, or description enables Save Changes.
        assertThat(tracker.saveChangesButton()).isEnabled();

        tracker.saveChanges();

        // WF-005 AC-6: a successful save closes the modal and the list reflects
        // the saved title, owner, and modification timestamp.
        assertThat(tracker.detailModal()).isHidden();
        assertThat(tracker.rowWithTitle(newTitle)).isVisible();
        assertThat(tracker.ownerCell(newTitle)).hasText("FirstName2 LastName2");
        assertThat(tracker.updatedCell(newTitle)).hasText(Pattern.compile(".+"));

        // WF-005 AC-6 / WF-004 AC-1/AC-2: reopening shows every saved field -
        // title, owner, and description - persisted, not just what the list
        // row happens to display.
        tracker.openDetail(newTitle);
        assertThat(tracker.detailTitleInput()).hasValue(newTitle);
        assertThat(tracker.detailOwnerSelected()).hasText("FirstName2 LastName2");
        assertThat(tracker.detailDescriptionInput()).hasValue(newDescription);

        // WF-001 AC-8 / WF-004 AC-3: the Updated field is populated after a save.
        // Actual timestamp *advancement* is verified at the SQL layer
        // (WorkItemPersistenceSqlTest.editingAWorkItemAdvancesItsPersistedUpdatedDate),
        // since the UI only displays second-level precision (toLocaleString())
        // and a fast create-then-edit can land within the same displayed second.
        assertThat(tracker.detailUpdated()).hasText(Pattern.compile(".+"));

        // WF-004 AC-4 / WF-005 AC-7: editing title/owner/description does not
        // add a status-history record - still just the initial creation row.
        assertThat(tracker.historyRows()).hasCount(1);
    }

    @Test
    void changesOwnerFromAssignedToUnassigned() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        String title = "Automated test - unassign owner " + UUID.randomUUID();
        tracker.createItem(title, "FirstName1 LastName1", "Starts with an assigned owner");

        tracker.openDetail(title);
        assertThat(tracker.detailOwnerSelected()).hasText("FirstName1 LastName1");

        // WF-001 AC-6: a work item may be changed from assigned to unassigned.
        tracker.setDetailOwner(null);
        assertThat(tracker.saveChangesButton()).isEnabled();
        tracker.saveChanges();

        assertThat(tracker.detailModal()).isHidden();
        assertThat(tracker.ownerCell(title)).hasText("");

        tracker.openDetail(title);
        assertThat(tracker.detailOwnerSelected()).hasText("Unassigned");
    }

    @Test
    void revertingEditsToOriginalValuesDisablesSaveChangesAgain() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        String title = "Automated test - revert edit " + UUID.randomUUID();
        tracker.createItem(title, "FirstName1 LastName1", "Original description");

        tracker.openDetail(title);
        assertThat(tracker.saveChangesButton()).isDisabled();

        tracker.setDetailTitle(title + " (temporarily edited)");
        assertThat(tracker.saveChangesButton()).isEnabled();

        // WF-005 AC-4: returning an edited field to its original value
        // disables Save Changes again.
        tracker.setDetailTitle(title);
        assertThat(tracker.saveChangesButton()).isDisabled();
    }

    @Test
    void promptsToDiscardUnsavedChangesOnClose() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        String title = "Automated test - unsaved edit " + UUID.randomUUID();
        tracker.createItem(title, null, "Original description");

        tracker.openDetail(title);
        String editedTitle = title + " (edited, unsaved)";
        tracker.setDetailTitle(editedTitle);
        assertThat(tracker.saveChangesButton()).isEnabled();

        // WF-005 AC-9/AC-10: closing with unsaved changes prompts a discard
        // confirmation. Playwright auto-dismisses dialogs (i.e. Cancel) by
        // default, so canceling here should leave the modal open with the
        // edit intact.
        tracker.closeDetail();
        assertThat(tracker.detailModal()).isVisible();
        assertThat(tracker.detailTitleInput()).hasValue(editedTitle);

        // WF-005 AC-11: confirming the discard closes the modal without saving.
        page.onDialog(Dialog::accept);
        tracker.closeDetail();
        assertThat(tracker.detailModal()).isHidden();

        // The original title (never saved) is still what's on the list/reopen.
        tracker.openDetail(title);
        assertThat(tracker.detailTitleInput()).hasValue(title);
    }

    @Test
    void showsStatusHistoryForCreationAndTransitions() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        String title = "Automated test - status history " + UUID.randomUUID();
        tracker.createItem(title, null, "Should record NEW on creation");

        tracker.openDetail(title);
        // WF-003 AC-7: creation itself writes one history row: blank Previous, NEW as New.
        assertThat(tracker.historyRows()).hasCount(1);
        assertThat(tracker.historyRows().first().locator("td").nth(0)).hasText("");
        assertThat(tracker.historyRows().first().locator("td").nth(1)).hasText("NEW");
        // WF-003 AC-2: the history record also carries a change timestamp.
        assertThat(tracker.historyRows().first().locator("td").nth(2)).hasText(Pattern.compile(".+"));

        tracker.closeDetail();

        tracker.setStatusForRow(title, "OPEN");
        assertThat(tracker.statusBadge(title)).hasText("OPEN");

        tracker.openDetail(title);
        // WF-003 AC-1/AC-2/AC-6: the valid transition adds a second row on top
        // of the original creation entry, in order.
        assertThat(tracker.historyRows()).hasCount(2);
        assertThat(tracker.historyRows().nth(1).locator("td").nth(0)).hasText("NEW");
        assertThat(tracker.historyRows().nth(1).locator("td").nth(1)).hasText("OPEN");
        assertThat(tracker.historyRows().nth(1).locator("td").nth(2)).hasText(Pattern.compile(".+"));
    }

    @Test
    void closesImmediatelyWhenNoUnsavedChanges() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        String title = "Automated test - clean close " + UUID.randomUUID();
        tracker.createItem(title, null, "Should close without a discard prompt");

        tracker.openDetail(title);
        assertThat(tracker.saveChangesButton()).isDisabled();

        // WF-005 AC-8: closing with no unsaved changes closes immediately -
        // no discard-confirmation dialog is raised to auto-dismiss.
        tracker.closeDetail();
        assertThat(tracker.detailModal()).isHidden();
    }
}
