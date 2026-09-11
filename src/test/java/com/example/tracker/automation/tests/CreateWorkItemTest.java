package com.example.tracker.automation.tests;

import com.example.tracker.automation.BaseUiTest;
import com.example.tracker.automation.pageobjects.TrackerPage;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class CreateWorkItemTest extends BaseUiTest {

    @Test
    void createsANewItemAndDisplaysItInTheTable() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        String title = "Automated test - create " + UUID.randomUUID();
        tracker.createItem(title, "Frank", "Created by a black-box Playwright UI test");

        // assertThat(...) here is Playwright's web-first assertion: it retries
        // until the element appears (or a timeout is hit) instead of failing
        // on the first check, which is what makes these tests resist flake.
        assertThat(tracker.rowWithTitle(title)).isVisible();
        assertThat(tracker.statusBadge(title)).hasText("NEW");
    }

    @Test
    void rejectsAnItemWithNoTitle() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        int rowsBefore = tracker.rowCount();
        tracker.createItem("", "Frank", "No title on purpose " + UUID.randomUUID());

        // #title is a required field, so the browser blocks the form
        // submission client-side and the row count should never change.
        assertThat(tracker.allRows()).hasCount(rowsBefore);
    }
}
