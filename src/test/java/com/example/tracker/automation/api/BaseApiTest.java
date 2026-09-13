package com.example.tracker.automation.api;

import org.junit.jupiter.api.BeforeAll;

/**
 * Base class for API-layer regression tests. Deliberately independent of
 * BaseUiTest / Playwright - these tests never touch a browser, they exercise
 * the app's REST contract directly. Reads the same "base.url" system
 * property as the UI suite, so both layers share the same -Dbase.url= entry
 * point without any extra CI plumbing.
 */
public abstract class BaseApiTest {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    protected static String baseUrl;
    protected static WorkItemApiClient api;

    @BeforeAll
    static void setUpApiClient() {
        baseUrl = System.getProperty("base.url", DEFAULT_BASE_URL);
        api = new WorkItemApiClient(baseUrl);
    }
}
