package com.example.tracker.automation;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for all UI automation tests.
 *
 * This suite is deliberately black-box: it has no dependency on the
 * workflow-tracker application's source code, build, or Java classes. It
 * only assumes some instance of the app is already running and reachable
 * over HTTP - the same assumption a QA engineer testing a deployed
 * environment would make. It never starts, stops, or otherwise manages the
 * application under test.
 *
 * The base URL is read from the "base.url" system property, e.g.:
 *   mvn test -Dbase.url=http://localhost:8080
 * and defaults to http://localhost:8080 when not set.
 */
public abstract class BaseUiTest {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    protected static String baseUrl;

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    protected Page page;

    @BeforeAll
    static void launchBrowser() {
        baseUrl = System.getProperty("base.url", DEFAULT_BASE_URL);
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
        );
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void newPage() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        if (context != null) context.close();
    }
}
