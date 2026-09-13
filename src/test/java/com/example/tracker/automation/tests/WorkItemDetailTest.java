package com.example.tracker.automation.tests;

import com.example.tracker.automation.BaseUiTest;
import com.example.tracker.automation.pageobjects.TrackerPage;
import com.microsoft.playwright.Dialog;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class WorkItemDetailTest extends BaseUiTest {

    @Test
    void editsAndSavesWorkItemDetails() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        String originalTitle = "Automated test - edit before " + UUID.randomUUID();
        tracker.createItem(originalTitle, "FirstName1 LastName1", "Original description");

        tracker.openDetail(originalTitle);
        // WF-005 AC-2: Save Changes starts disabled - nothing has been edited yet.
        assertThat(tracker.saveChangesButton()).isDisabled();

        String newTitle = "Automated test - edit after " + UUID.randomUUID();
        String newDescription = "Updated description " + UUID.randomUUID();
        tracker.setDetailTitle(newTitle);
        tracker.setDetailOwner("FirstName2 LastName2");
        tracker.setDetailDescription(newDescription);

        // WF-005 AC-3: changing title, owner, or description enables Save Changes.
        assertThat(tracker.saveChangesButton()).isEnabled();

        tracker.saveChanges();

        // WF-005 AC-6: a successful save closes the modal and the list reflects
        // the saved title and owner.
        assertThat(tracker.detailModal()).isHidden();
        assertThat(tracker.rowWithTitle(newTitle)).isVisible();
        assertThat(tracker.ownerCell(newTitle)).hasText("FirstName2 LastName2");

        // WF-004 AC-1/AC-2: the saved description persisted and is shown on reopen.
        tracker.openDetail(newTitle);
        assertThat(tracker.detailDescriptionInput()).hasValue(newDescription);
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

        tracker.closeDetail();

        tracker.setStatusForRow(title, "OPEN");
        assertThat(tracker.statusBadge(title)).hasText("OPEN");

        tracker.openDetail(title);
        // WF-003 AC-1/AC-2/AC-6: the valid transition adds a second row on top
        // of the original creation entry, in order.
        assertThat(tracker.historyRows()).hasCount(2);
        assertThat(tracker.historyRows().nth(1).locator("td").nth(0)).hasText("NEW");
        assertThat(tracker.historyRows().nth(1).locator("td").nth(1)).hasText("OPEN");
    }
}
