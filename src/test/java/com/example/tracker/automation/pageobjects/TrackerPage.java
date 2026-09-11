package com.example.tracker.automation.pageobjects;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object for the workflow-tracker main screen.
 *
 * All locators here are derived purely from the observable DOM contract of
 * the app (element ids, classes, and structure) - never from its source
 * code. Tests talk only to this class, never to raw locators, so a markup
 * change only requires updating one file.
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

    public TrackerPage createItem(String title, String assignee, String description) {
        page.fill("#title", title);
        page.fill("#assignee", assignee);
        page.fill("#description", description);
        page.click("#create-form button[type='submit']");
        return this;
    }

    public String createErrorText() {
        return page.locator("#create-error").innerText();
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
        rowWithTitle(title).locator("select").selectOption(status);
    }

    public String statusBadgeText(String title) {
        return rowWithTitle(title).locator(".badge").innerText();
    }

    public void deleteRow(String title) {
        rowWithTitle(title).locator("button").click();
    }

    public int rowCount() {
        return page.locator("#items-body tr").count();
    }

    public Locator allRows() {
        return page.locator("#items-body tr");
    }
}
