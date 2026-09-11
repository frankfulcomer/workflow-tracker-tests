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
        tracker.createItem(title, "Frank", "Should move NEW -> IN_PROGRESS -> RESOLVED -> CLOSED");
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
        tracker.createItem(title, "Frank", "Should stay NEW - can't jump straight to CLOSED");
        assertThat(tracker.statusBadge(title)).hasText("NEW");

        // Any dialog the app pops up to report the rejection (e.g. a native
        // alert()) is auto-dismissed by Playwright by default, so this never
        // hangs regardless of how the app chooses to surface the error.
        tracker.setStatusForRow(title, "CLOSED");

        // The business rule under test: a NEW item can't skip straight to
        // CLOSED. The badge should still read NEW once the app settles.
        assertThat(tracker.statusBadge(title)).hasText("NEW");
    }
}
