package com.example.tracker.automation.tests;

import com.example.tracker.automation.BaseUiTest;
import com.example.tracker.automation.pageobjects.TrackerPage;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class StatusWorkflowTest extends BaseUiTest {

    @Test
    void movesAnItemThroughTheHappyPathWorkflow() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        String title = "Automated test - happy path " + UUID.randomUUID();
        tracker.createItem(title, "FirstName2 LastName2", "Should move NEW -> IN_PROGRESS -> RESOLVED -> CLOSED");
        assertThat(tracker.statusBadge(title)).hasText("NEW");

        // WF-002 AC-1: exactly these five statuses are supported, in this order.
        assertThat(tracker.statusSelectOptions(title))
                .hasText(new String[] {"NEW", "OPEN", "IN PROGRESS", "RESOLVED", "CLOSED"});

        tracker.setStatusForRow(title, "IN_PROGRESS");
        assertThat(tracker.statusBadge(title)).hasText("IN PROGRESS");

        tracker.setStatusForRow(title, "RESOLVED");
        assertThat(tracker.statusBadge(title)).hasText("RESOLVED");

        tracker.setStatusForRow(title, "CLOSED");
        assertThat(tracker.statusBadge(title)).hasText("CLOSED");
    }

    @Test
    void rejectsDirectTransitionFromNewToClosed() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        String title = "Automated test - illegal transition " + UUID.randomUUID();
        tracker.createItem(title, null, "Should stay NEW - can't jump straight to CLOSED");
        assertThat(tracker.statusBadge(title)).hasText("NEW");

        // WF-002 AC-15: a rejected transition surfaces an error message to the
        // user (here, a native alert()). Registering our own dialog handler -
        // instead of relying on Playwright's default auto-dismiss - lets the
        // test capture and verify that message.
        AtomicReference<String> dialogMessage = new AtomicReference<>();
        page.onDialog(dialog -> {
            dialogMessage.set(dialog.message());
            dialog.accept();
        });
        tracker.setStatusForRow(title, "CLOSED");
        page.waitForCondition(() -> dialogMessage.get() != null);
        assertFalse(dialogMessage.get().isBlank(), "a rejected transition should surface a non-blank error");

        // The business rule under test: a NEW item can't skip straight to
        // CLOSED. The badge should still read NEW once the app settles.
        assertThat(tracker.statusBadge(title)).hasText("NEW");
    }

    @Test
    void supportsAllowedBackwardTransitions() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        String title = "Automated test - backward transitions " + UUID.randomUUID();
        tracker.createItem(title, null, "Should support all four allowed backward transitions");

        tracker.setStatusForRow(title, "IN_PROGRESS");
        assertThat(tracker.statusBadge(title)).hasText("IN PROGRESS");

        // WF-002 AC-8: IN_PROGRESS -> OPEN is allowed.
        tracker.setStatusForRow(title, "OPEN");
        assertThat(tracker.statusBadge(title)).hasText("OPEN");

        tracker.setStatusForRow(title, "IN_PROGRESS");
        assertThat(tracker.statusBadge(title)).hasText("IN PROGRESS");

        tracker.setStatusForRow(title, "RESOLVED");
        assertThat(tracker.statusBadge(title)).hasText("RESOLVED");

        // WF-002 AC-9: RESOLVED -> IN_PROGRESS is allowed.
        tracker.setStatusForRow(title, "IN_PROGRESS");
        assertThat(tracker.statusBadge(title)).hasText("IN PROGRESS");

        tracker.setStatusForRow(title, "RESOLVED");
        assertThat(tracker.statusBadge(title)).hasText("RESOLVED");

        // WF-002 AC-10: RESOLVED -> OPEN is allowed.
        tracker.setStatusForRow(title, "OPEN");
        assertThat(tracker.statusBadge(title)).hasText("OPEN");

        tracker.setStatusForRow(title, "IN_PROGRESS");
        assertThat(tracker.statusBadge(title)).hasText("IN PROGRESS");

        tracker.setStatusForRow(title, "RESOLVED");
        assertThat(tracker.statusBadge(title)).hasText("RESOLVED");

        tracker.setStatusForRow(title, "CLOSED");
        assertThat(tracker.statusBadge(title)).hasText("CLOSED");

        // WF-002 AC-11: CLOSED -> OPEN is allowed.
        tracker.setStatusForRow(title, "OPEN");
        assertThat(tracker.statusBadge(title)).hasText("OPEN");
    }

    @Test
    void preservesStatusHistoryWhenReopenedFromClosed() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        String title = "Automated test - reopen history " + UUID.randomUUID();
        tracker.createItem(title, null, "Should keep full status history after CLOSED -> OPEN");

        tracker.setStatusForRow(title, "IN_PROGRESS");
        tracker.setStatusForRow(title, "RESOLVED");
        tracker.setStatusForRow(title, "CLOSED");
        assertThat(tracker.statusBadge(title)).hasText("CLOSED");

        tracker.openDetail(title);
        // Creation + 3 transitions (IN_PROGRESS, RESOLVED, CLOSED) = 4 rows.
        assertThat(tracker.historyRows()).hasCount(4);
        tracker.closeDetail();

        // WF-002 AC-11: CLOSED -> OPEN is allowed (reopening the item).
        tracker.setStatusForRow(title, "OPEN");
        assertThat(tracker.statusBadge(title)).hasText("OPEN");

        tracker.openDetail(title);
        // WF-003 AC-5: existing status history is preserved when a work item
        // is reopened - the reopen adds one more row on top of the prior
        // history rather than clearing it.
        assertThat(tracker.historyRows()).hasCount(5);
        assertThat(tracker.historyRows().last().locator("td").nth(0)).hasText("CLOSED");
        assertThat(tracker.historyRows().last().locator("td").nth(1)).hasText("OPEN");
    }

    @Test
    void movesInAndOutOfOpenStatus() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        String title = "Automated test - open status " + UUID.randomUUID();
        tracker.createItem(title, null, "Should support NEW -> OPEN -> IN_PROGRESS, and reject OPEN -> CLOSED");

        // WF-002 AC-3: NEW -> OPEN is allowed.
        tracker.setStatusForRow(title, "OPEN");
        assertThat(tracker.statusBadge(title)).hasText("OPEN");

        // WF-002 AC-12/AC-13: OPEN's only legal forward transition is to
        // IN_PROGRESS, so OPEN -> CLOSED must be rejected and leave the
        // status unchanged.
        tracker.setStatusForRow(title, "CLOSED");
        assertThat(tracker.statusBadge(title)).hasText("OPEN");

        // WF-002 AC-5: OPEN -> IN_PROGRESS is allowed.
        tracker.setStatusForRow(title, "IN_PROGRESS");
        assertThat(tracker.statusBadge(title)).hasText("IN PROGRESS");
    }
}
