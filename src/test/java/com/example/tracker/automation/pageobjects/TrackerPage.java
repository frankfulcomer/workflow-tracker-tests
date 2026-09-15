package com.example.tracker.automation.pageobjects;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Page Object for the workflow-tracker main screen.
 *
 * All locators here are derived purely from the observable DOM contract of
 * the app (element ids, classes, data-testid attributes, and structure) -
 * never from its source code. Where the app exposes a data-testid, it's
 * preferred over a CSS class or bare tag name, since it's a hook meant for
 * automation and won't shift if styling or element order changes. Tests
 * talk only to this class, never to raw locators, so a markup change only
 * requires updating one file.
 */
public class TrackerPage {

    private final Page page;

    public TrackerPage(Page page) {
        this.page = page;
    }

    public TrackerPage open(String baseUrl) {
        page.navigate(baseUrl);
        page.locator("#title").waitFor();
        return this;
    }

    /**
     * Fills the create form without submitting it. {@code owner} must match the
     * visible label of an option in the Owner select (a predefined owner's name,
     * e.g. "FirstName1 LastName1"); pass null or "" to leave it as Unassigned.
     */
    public TrackerPage fillCreateForm(String title, String owner, String description) {
        page.fill("#title", title);
        if (owner != null && !owner.isEmpty()) {
            page.selectOption("#owner", new SelectOption().setLabel(owner));
        }
        page.fill("#description", description);
        return this;
    }

    public TrackerPage createItem(String title, String owner, String description) {
        fillCreateForm(title, owner, description);
        submitCreateForm();
        // WF-006 AC-8: on success the form resets (title clears) and Create
        // re-disables. Waiting for that here - rather than letting the caller
        // return immediately - is what makes back-to-back createItem() calls
        // safe: without it, a still-in-flight submission's reset can land
        // after a *later* createItem() has already filled a new title,
        // clobbering it and leaving Create stuck disabled.
        page.waitForFunction(
                "() => document.querySelector('#title').value === '' "
                        + "&& document.querySelector('#create-btn').disabled === true");
        return this;
    }

    /** Submits the create form without waiting for the success-reset condition - use this directly (instead of createItem()) when exercising a creation-failure path, since the form does not reset on failure. */
    public TrackerPage submitCreateForm() {
        page.click("#create-form button[type='submit']");
        return this;
    }

    public String createErrorText() {
        return page.locator("#create-error").innerText();
    }

    /** Locator (not a resolved String) so callers can use Playwright's auto-retrying assertions on it. */
    public Locator createError() {
        return page.locator("#create-error");
    }

    /** The Create action - disabled until a nonblank title has been entered (WF-006 AC-4). */
    public Locator createButton() {
        return page.locator("#create-btn");
    }

    /** The create form's Title input - exposed so tests can verify it resets after a successful create (WF-006 AC-8). */
    public Locator createTitleInput() {
        return page.locator("#title");
    }

    /** The create form's Owner select - exposed so tests can verify it resets to Unassigned after a successful create (WF-006 AC-8). */
    public Locator createOwnerSelect() {
        return page.locator("#owner");
    }

    /** The create form's Description textarea - exposed so tests can verify it resets after a successful create (WF-006 AC-8). */
    public Locator createDescriptionInput() {
        return page.locator("#description");
    }

    /**
     * Fills the search box and waits for the resulting (debounced) filtered
     * fetch to complete before returning. Without this, callers that assert
     * immediately after search() can observe the list from before the
     * filtered request lands rather than the actual filtered result.
     */
    public TrackerPage search(String text) {
        String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
        page.waitForResponse(
                response -> response.url().contains("/api/items?") && response.url().contains("q=" + encoded),
                () -> page.fill("#search-box", text));
        return this;
    }

    /**
     * Selects the status filter and waits for the resulting filtered fetch to
     * complete before returning - see {@link #search(String)}.
     */
    public TrackerPage filterByStatus(String status) {
        page.waitForResponse(
                response -> response.url().contains("/api/items?") && response.url().contains("status=" + status),
                () -> page.selectOption("#status-filter", status));
        return this;
    }

    /** Row locator scoped by the item's title text - waits automatically when asserted on. */
    public Locator rowWithTitle(String title) {
        return page.locator("#items-body tr", new Page.LocatorOptions().setHasText(title));
    }

    public void setStatusForRow(String title, String status) {
        rowWithTitle(title).locator("[data-testid^='status-select-']").selectOption(status);
    }

    /** The row's status-select options, in display order - lets tests assert exactly which statuses are supported (WF-002 AC-1). */
    public Locator statusSelectOptions(String title) {
        return rowWithTitle(title).locator("[data-testid^='status-select-'] option");
    }

    /** Locator (not a resolved String) so callers can use Playwright's auto-retrying assertions on it. */
    public Locator statusBadge(String title) {
        return rowWithTitle(title).locator("[data-testid^='item-status-']");
    }

    public void deleteRow(String title) {
        rowWithTitle(title).locator("[data-testid^='delete-btn-']").click();
    }

    public int rowCount() {
        return page.locator("#items-body tr").count();
    }

    public Locator allRows() {
        return page.locator("#items-body tr");
    }

    /** The list row's Owner column - no data-testid is exposed for it, so this relies on column order. */
    public Locator ownerCell(String title) {
        return rowWithTitle(title).locator("td").nth(1);
    }

    /** The list row's Updated column - no data-testid is exposed for it, so this relies on column order. */
    public Locator updatedCell(String title) {
        return rowWithTitle(title).locator("td").nth(3);
    }

    // --- Item detail modal ---

    public Locator detailModal() {
        return page.locator("#item-detail-modal");
    }

    public TrackerPage openDetail(String title) {
        rowWithTitle(title).locator("[data-testid^='view-btn-']").click();
        detailModal().waitFor();
        return this;
    }

    public Locator detailTitleInput() {
        return page.locator("#detail-title");
    }

    public TrackerPage setDetailTitle(String title) {
        page.fill("#detail-title", title);
        return this;
    }

    public Locator detailDescriptionInput() {
        return page.locator("#detail-description");
    }

    public TrackerPage setDetailDescription(String description) {
        page.fill("#detail-description", description);
        return this;
    }

    /** Selects an owner in the detail modal by name; pass null or "" to select Unassigned. */
    public TrackerPage setDetailOwner(String owner) {
        String label = (owner == null || owner.isEmpty()) ? "Unassigned" : owner;
        page.selectOption("#detail-owner", new SelectOption().setLabel(label));
        return this;
    }

    /** The currently selected option in the detail modal's Owner select - its visible label (an owner's name, or "Unassigned"). */
    public Locator detailOwnerSelected() {
        return page.locator("#detail-owner option:checked");
    }

    /** The detail modal's "Updated" timestamp text. */
    public Locator detailUpdated() {
        return page.locator("#detail-updated");
    }

    public Locator saveChangesButton() {
        return page.locator("[data-testid='save-changes-btn']");
    }

    public TrackerPage saveChanges() {
        saveChangesButton().click();
        return this;
    }

    public TrackerPage closeDetail() {
        page.click("#detail-close-btn");
        return this;
    }

    /** Status-history rows in the detail modal, in display order (oldest first). */
    public Locator historyRows() {
        return page.locator("#detail-history-body tr");
    }
}
