package com.example.tracker.automation.pageobjects;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

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
        page.click("#create-form button[type='submit']");
        return this;
    }

    public String createErrorText() {
        return page.locator("#create-error").innerText();
    }

    /** The Create action - disabled until a nonblank title has been entered (WF-006 AC-4). */
    public Locator createButton() {
        return page.locator("#create-btn");
    }

    public TrackerPage search(String text) {
        page.fill("#search-box", text);
        return this;
    }

    public TrackerPage filterByStatus(String status) {
        page.selectOption("#status-filter", status);
        return this;
    }

    /** Row locator scoped by the item's title text - waits automatically when asserted on. */
    public Locator rowWithTitle(String title) {
        return page.locator("#items-body tr", new Page.LocatorOptions().setHasText(title));
    }

    public void setStatusForRow(String title, String status) {
        rowWithTitle(title).locator("[data-testid^='status-select-']").selectOption(status);
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
}
