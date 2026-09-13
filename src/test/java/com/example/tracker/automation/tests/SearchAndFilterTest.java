package com.example.tracker.automation.tests;

import com.example.tracker.automation.BaseUiTest;
import com.example.tracker.automation.pageobjects.TrackerPage;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class SearchAndFilterTest extends BaseUiTest {

    @Test
    void searchNarrowsTheListByTitle() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        String marker = UUID.randomUUID().toString();
        String matchingTitle = "Findable via search " + marker;
        String otherTitle = "Should stay hidden " + UUID.randomUUID();

        tracker.createItem(matchingTitle, null, "Should be found by search");
        tracker.createItem(otherTitle, null, "Should not show up in the filtered results");

        tracker.search(marker);

        assertThat(tracker.rowWithTitle(matchingTitle)).isVisible();
        assertThat(tracker.rowWithTitle(otherTitle)).hasCount(0);
    }

    @Test
    void filteringByStatusShowsOnlyMatchingItems() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        String resolvedTitle = "Automated test - resolved item " + UUID.randomUUID();
        String newTitle = "Automated test - still new item " + UUID.randomUUID();

        tracker.createItem(resolvedTitle, null, "Will be moved to RESOLVED");
        tracker.setStatusForRow(resolvedTitle, "RESOLVED");

        tracker.createItem(newTitle, null, "Stays in the default NEW status");

        tracker.filterByStatus("RESOLVED");

        assertThat(tracker.rowWithTitle(resolvedTitle)).isVisible();
        assertThat(tracker.rowWithTitle(newTitle)).hasCount(0);
    }
}
