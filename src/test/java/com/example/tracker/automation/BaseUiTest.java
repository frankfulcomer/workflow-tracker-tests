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

import java.util.List;

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
 *
 * Browsers run headless by default. Pass -Dheaded=true to watch tests run
 * in a visible browser window, e.g.:
 *   mvn test -Dheaded=true
 *
 * For headed/demo runs, -DslowMo=<ms> slows down Playwright actions by the
 * given number of milliseconds. It defaults to 0 (no delay) and works
 * independently of -Dheaded, e.g.:
 *   mvn test "-Dheaded=true" "-DslowMo=500"
 *
 * Headed runs also launch Chromium maximized, using the full available
 * screen space instead of Playwright's default viewport, purely for
 * demonstration purposes. This has no effect in headless mode.
 */
public abstract class BaseUiTest {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    protected static String baseUrl;

    private static Playwright playwright;
    private static Browser browser;
    private static boolean headed;
    private BrowserContext context;
    protected Page page;

    @BeforeAll
    static void launchBrowser() {
        baseUrl = System.getProperty("base.url", DEFAULT_BASE_URL);
        headed = Boolean.parseBoolean(System.getProperty("headed", "false"));
        double slowMo = Double.parseDouble(System.getProperty("slowMo", "0"));
        playwright = Playwright.create();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(!headed)
                .setSlowMo(slowMo);
        if (headed) {
            // Let Chromium own window sizing rather than hard-coding a resolution;
            // the context viewport must be cleared too, or Playwright's default
            // viewport overrides the maximized window.
            launchOptions.setArgs(List.of("--start-maximized"));
        }
        browser = playwright.chromium().launch(launchOptions);
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void newPage() {
        context = headed
                ? browser.newContext(new Browser.NewContextOptions().setViewportSize(null))
                : browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        if (context != null) context.close();
    }
}
