package com.example.tracker.automation.tests;

import com.example.tracker.automation.BaseUiTest;
import com.example.tracker.automation.pageobjects.TrackerPage;
import com.microsoft.playwright.Route;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class CreateWorkItemTest extends BaseUiTest {

    @Test
    void createsANewItemAndDisplaysItInTheTable() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        String title = "Automated test - create " + UUID.randomUUID();
        tracker.createItem(title, "FirstName1 LastName1", "Created by a black-box Playwright UI test");

        // assertThat(...) here is Playwright's web-first assertion: it retries
        // until the element appears (or a timeout is hit) instead of failing
        // on the first check, which is what makes these tests resist flake.
        assertThat(tracker.rowWithTitle(title)).isVisible();
        assertThat(tracker.statusBadge(title)).hasText("NEW");

        // WF-006 AC-8: on success the form resets to its initial state - title,
        // description, and owner all clear - and Create re-disables.
        assertThat(tracker.createTitleInput()).hasValue("");
        assertThat(tracker.createDescriptionInput()).hasValue("");
        assertThat(tracker.createOwnerSelect()).hasValue("");
        assertThat(tracker.createButton()).isDisabled();
    }

    @Test
    void rejectsAnItemWithNoTitle() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        int rowsBefore = tracker.rowCount();
        tracker.fillCreateForm("", "FirstName1 LastName1", "No title on purpose " + UUID.randomUUID());

        // WF-006 AC-4: the Create action is disabled until a nonblank title has
        // been entered, so there's nothing to click - and the row count never
        // changes.
        assertThat(tracker.createButton()).isDisabled();
        assertThat(tracker.allRows()).hasCount(rowsBefore);
    }

    @Test
    void createsAnItemWithOwnerAndDescription() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        String title = "Automated test - owner and description " + UUID.randomUUID();
        tracker.createItem(title, "FirstName1 LastName1", "Has both an owner and a description");

        // WF-001 AC-1/AC-3: an owner assigned at creation shows up on the list row.
        assertThat(tracker.rowWithTitle(title)).isVisible();
        assertThat(tracker.ownerCell(title)).hasText("FirstName1 LastName1");
        assertThat(tracker.statusBadge(title)).hasText("NEW");

        // WF-001 AC-1/AC-4: and on the detail view.
        tracker.openDetail(title);
        assertThat(tracker.detailOwnerSelected()).hasText("FirstName1 LastName1");
    }

    @Test
    void createsAnItemWithNoDescription() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        String title = "Automated test - no description " + UUID.randomUUID();
        tracker.createItem(title, "FirstName1 LastName1", "");

        // WF-006 AC-2: a work item may be created without a description.
        assertThat(tracker.rowWithTitle(title)).isVisible();
        tracker.openDetail(title);
        assertThat(tracker.detailDescriptionInput()).hasValue("");
    }

    @Test
    void keepsEnteredValuesAndShowsErrorWhenCreationFails() {
        TrackerPage tracker = new TrackerPage(page).open(baseUrl);

        // Force the creation POST to fail so the UI's failure-handling path -
        // otherwise unreachable, since a blank/whitespace title is blocked
        // client-side (WF-006 AC-4) and the Owner select only ever offers
        // valid owners - can be exercised.
        page.route(Pattern.compile(".*/api/items$"), route -> {
            if ("POST".equals(route.request().method())) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(400)
                        .setContentType("application/json")
                        .setBody("{\"error\":\"Simulated creation failure\"}"));
            } else {
                route.resume();
            }
        });

        String title = "Automated test - creation failure " + UUID.randomUUID();
        String description = "Should survive a failed creation " + UUID.randomUUID();
        tracker.fillCreateForm(title, "FirstName1 LastName1", description);
        tracker.submitCreateForm();

        // WF-006 AC-10: the user is informed that creation failed.
        assertThat(tracker.createError()).hasText(Pattern.compile(".+"));

        // WF-006 AC-9: entered values remain available, not cleared.
        assertThat(tracker.createTitleInput()).hasValue(title);
        assertThat(tracker.createDescriptionInput()).hasValue(description);
    }
}
