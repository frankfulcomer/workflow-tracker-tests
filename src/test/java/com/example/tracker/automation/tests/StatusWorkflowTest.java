package com.example.tracker.automation.tests;

import com.example.tracker.automation.BaseUiTest;
import com.example.tracker.automation.pageobjects.TrackerPage;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class StatusWorkflowTest extends BaseUiTest {

    @Test
    void movesAnItemThroughTheHappyPathWorkflow() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        String title = "Automated test - happy path " + UUID.randomUUID();
        tracker.createItem(title, "FirstName2 LastName2", "Should move NEW -> IN_PROGRESS -> RESOLVED -> CLOSED");
        assertThat(tracker.statusBadge(title)).hasText("NEW");

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

        // Any dialog the app pops up to report the rejection (e.g. a native
        // alert()) is auto-dismissed by Playwright by default, so this never
        // hangs regardless of how the app chooses to surface the error.
        tracker.setStatusForRow(title, "CLOSED");

        // The business rule under test: a NEW item can't skip straight to
        // CLOSED. The badge should still read NEW once the app settles.
        assertThat(tracker.statusBadge(title)).hasText("NEW");
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
