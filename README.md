# workflow-tracker-tests

A **black-box** UI automation suite for [workflow-tracker](https://github.com/frankfulcomer/workflow-tracker),
built with Playwright (Java) and JUnit 5.

## What "black-box" means here

This project has **no dependency on the application's source code, build,
or Java classes** - not even a shared Maven module. It was written purely
against the app's *observable contract*: the HTML element ids/classes on
its main page, and the business rules a user would discover by clicking
around (e.g. "an item can't jump straight from NEW to CLOSED").

It can be run against **any running instance** of workflow-tracker -
local, staging, a Docker container, whatever - as long as it's reachable
over HTTP. The suite never starts, stops, seeds, or otherwise reaches into
the application under test; it only drives a real browser against it, the
same way a QA engineer would test a deployed environment they don't have
source access to.

## Requirements

- Java 17+
- Maven
- The [Playwright browser binaries](#installing-the-playwright-browser)
  (one-time setup)
- A running instance of workflow-tracker

## Installing the Playwright browser

First time only:

```bash
mvn exec:java -e -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install --with-deps chromium"
```

## Running the suite

By default the suite targets `http://localhost:8080`:

```bash
mvn test
```

Point it at a different environment with `-Dbase.url`:

```bash
mvn test -Dbase.url=https://staging.example.com
```

## Structure

```
src/test/java/com/example/tracker/automation/
  BaseUiTest.java              # launches headless Chromium; reads -Dbase.url (default http://localhost:8080)
  pageobjects/TrackerPage.java # locators + interactions, one place to update if the markup changes
  tests/
    CreateWorkItemTest.java     # creating an item, required-field validation
    StatusWorkflowTest.java     # the NEW -> IN_PROGRESS -> RESOLVED -> CLOSED workflow, including the NEW -> CLOSED rejection
    SearchAndFilterTest.java    # title search and status filtering
```

Each test creates whatever data it needs through the app's own UI (the
create form) rather than assuming any particular pre-existing/seeded
state, so the suite is self-contained and safe to run repeatedly against
a long-lived instance.
