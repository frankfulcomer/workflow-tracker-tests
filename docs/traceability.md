# Traceability

This table maps every acceptance criterion in workflow-tracker's
[`product-stories.md`](https://github.com/frankfulcomer/workflow-tracker/blob/main/docs/product-stories.md)
(stories WF-001 through WF-006) to the automated and/or manual test(s) that
verify it, based on the automated suite's last full run (**30 tests: 18 UI,
6 API, 6 SQL — 0 failures, 0 errors, 0 skipped**) and the three manual test
workbooks under `docs/manual-tests/`.

Coverage here is derived directly from each acceptance criterion's text
against what the test code actually asserts (for automated tests) and what
each manual test step's *Expected Result* actually instructs the tester to
check (for manual tests) - not from the `// WF-00X AC-Y` shorthand comments
alone, and not from a manual step's Requirement-column citation alone. A
test that merely creates or uses a behavior as setup (e.g. creating an item
with an owner in order to test something else) is **not** counted as
coverage of that behavior unless the test also asserts on it.

## Coverage classifications

- **Covered** - at least one automated test explicitly asserts the behavior described by the AC.
- **Manual only** - no automated test explicitly asserts it, but a documented manual test step does.
- **Partial** - some verification exists (automated and/or manual) but it does not fully exercise the AC as written.
- **Not covered** - no automated or manual test explicitly addresses the AC.

WF-005 AC-12 and AC-13 (description-field sizing and action-area layout) are
intentionally **Manual only**: they are visual/layout requirements, not
business logic, and are deliberately left to manual verification rather than
automated as a DOM/CSS proxy check.

## Automated test key

| Abbrev. | Class |
|---|---|
| CWI | `CreateWorkItemTest` (UI) |
| SW | `StatusWorkflowTest` (UI) |
| WID | `WorkItemDetailTest` (UI) |
| SAF | `SearchAndFilterTest` (UI) |
| WCA | `WorkItemCreationApiTest` (API) |
| STA | `StatusTransitionApiTest` (API) |
| WPS | `WorkItemPersistenceSqlTest` (SQL) |
| STS | `StatusTransitionSqlTest` (SQL) |

Manual test IDs (`UI-0xx`, `API-0xx`, `SQL-0xx`) refer to the sheets in the
corresponding workbook under `docs/manual-tests/`.

---

## WF-001 — Assign an Owner to a Work Item

| AC | Acceptance Criterion | Automated | Manual | Status |
|---|---|---|---|---|
| AC-1 | Owner may be assigned at creation | CWI.createsAnItemWithOwnerAndDescription(); WCA.createsItemWithSuppliedValuesNewStatusAndInitialHistoryEntry(); WPS.createdItemPersistsSuppliedValuesAndOwnerRelationship(), .ownerAssignmentAndUnassignedAreRepresentedCorrectly() | UI-001, UI-003, UI-014, SQL-001, SQL-003 | Covered |
| AC-2 | May be created without an owner | WPS.ownerAssignmentAndUnassignedAreRepresentedCorrectly() (creates an unassigned item and asserts it succeeds) | SQL-003 | Covered |
| AC-3 | Assigned owner displayed on the list | CWI.createsAnItemWithOwnerAndDescription(); WID.editsAndSavesWorkItemDetails(); WPS.createdItemPersistsSuppliedValuesAndOwnerRelationship() | UI-001, UI-003, UI-009, UI-014, SQL-001 | Covered |
| AC-4 | Assigned owner displayed on the detail view | CWI.createsAnItemWithOwnerAndDescription(); WID.editsAndSavesWorkItemDetails(), .changesOwnerFromAssignedToUnassigned() | UI-003, UI-009, UI-014 | Covered |
| AC-5 | Owner can be changed after creation | WID.editsAndSavesWorkItemDetails() | UI-009 | Covered |
| AC-6 | Owner can be changed from assigned to unassigned | WID.changesOwnerFromAssignedToUnassigned() | UI-014 | Covered |
| AC-7 | Creation records a creation timestamp | WCA.createsItemWithSuppliedValuesNewStatusAndInitialHistoryEntry() (asserts `createdDate` is non-null on both the POST response and the follow-up GET) | API-001 (asserts the timestamp is present/nonblank, consistent with creation time, and still present on GET) | Covered |
| AC-8 | Modification records an updated timestamp | WPS.editingAWorkItemAdvancesItsPersistedUpdatedDate() (asserts the persisted `updated_date` is strictly later after an edit); WID.editsAndSavesWorkItemDetails() (asserts the Updated value is present after save - the UI only renders second-level precision, so timestamp *advancement* is verified at the SQL layer, not the UI) | UI-009 (step 7 compares before/after) | Covered |
| AC-9 | No owner ⇒ no stored owner relationship (NULL FK) | WPS.ownerAssignmentAndUnassignedAreRepresentedCorrectly() | SQL-003 | Covered |

## WF-002 — Manage Work-Item Status

| AC | Acceptance Criterion | Automated | Manual | Status |
|---|---|---|---|---|
| AC-1 | Supported statuses are NEW/OPEN/IN_PROGRESS/RESOLVED/CLOSED | SW.movesAnItemThroughTheHappyPathWorkflow() (asserts a row's status-select options are exactly these five, in order) | *(none)* | Covered |
| AC-2 | New item starts in NEW | CWI.createsANewItemAndDisplaysItInTheTable(); WCA.createsItemWithSuppliedValuesNewStatusAndInitialHistoryEntry(); WPS.createdItemPersistsSuppliedValuesAndOwnerRelationship() | UI-001, UI-003, UI-004, UI-005, UI-006, UI-008, UI-011, UI-013; API-004, API-005, API-006; SQL-001, SQL-004, SQL-005 | Covered |
| AC-3 | NEW → OPEN allowed | SW.movesInAndOutOfOpenStatus(); WID.showsStatusHistoryForCreationAndTransitions() | UI-006, UI-011 | Covered |
| AC-4 | NEW → IN_PROGRESS allowed | SW.movesAnItemThroughTheHappyPathWorkflow(), .supportsAllowedBackwardTransitions(); STA.validStatusTransitionAddsOneHistoryEntry(); STS.validTransitionUpdatesStatusAndAddsOneHistoryRow() | UI-004, UI-008, UI-012, UI-013; API-004; SQL-004 | Covered |
| AC-5 | OPEN → IN_PROGRESS allowed | SW.movesInAndOutOfOpenStatus(), .supportsAllowedBackwardTransitions() | UI-006 | Covered |
| AC-6 | IN_PROGRESS → RESOLVED allowed | SW.movesAnItemThroughTheHappyPathWorkflow(), .supportsAllowedBackwardTransitions() | UI-004, UI-008, UI-012, UI-013 | Covered |
| AC-7 | RESOLVED → CLOSED allowed | SW.movesAnItemThroughTheHappyPathWorkflow(), .supportsAllowedBackwardTransitions() | UI-004, UI-013 | Covered |
| AC-8 | IN_PROGRESS → OPEN allowed | SW.supportsAllowedBackwardTransitions() | UI-012 | Covered |
| AC-9 | RESOLVED → IN_PROGRESS allowed | SW.supportsAllowedBackwardTransitions() | UI-012 | Covered |
| AC-10 | RESOLVED → OPEN allowed | SW.supportsAllowedBackwardTransitions() | UI-012 | Covered |
| AC-11 | CLOSED → OPEN allowed | SW.supportsAllowedBackwardTransitions(), .preservesStatusHistoryWhenReopenedFromClosed() | UI-013 | Covered |
| AC-12 | Any unlisted transition is rejected | SW.rejectsDirectTransitionFromNewToClosed(), .movesInAndOutOfOpenStatus(); STA.invalidStatusTransitionIsRejectedAndUnchanged(); STS.rejectedTransitionLeavesStatusAndHistoryUnchanged() | UI-005, UI-006; API-005; SQL-005 | Covered |
| AC-13 | A rejected transition leaves status unchanged | SW.rejectsDirectTransitionFromNewToClosed(), .movesInAndOutOfOpenStatus(); STA.invalidStatusTransitionIsRejectedAndUnchanged(); STS.rejectedTransitionLeavesStatusAndHistoryUnchanged() | UI-005, UI-006; API-005; SQL-005 | Covered |
| AC-14 | Re-selecting the current status is a no-op | STA.sameStatusTransitionIsNoOpWithNoNewHistory() (no UI or SQL automated test exercises the no-op case) | API-006 | Covered |
| AC-15 | A rejected transition shows an error message | SW.rejectsDirectTransitionFromNewToClosed() (captures and asserts a non-blank dialog message); STA.invalidStatusTransitionIsRejectedAndUnchanged() (asserts a non-blank `error` body) | UI-005, UI-006; API-005 | Covered |

## WF-003 — Preserve Status History

| AC | Acceptance Criterion | Automated | Manual | Status |
|---|---|---|---|---|
| AC-1 | Every successful status change creates a history record | WID.showsStatusHistoryForCreationAndTransitions(); SW.preservesStatusHistoryWhenReopenedFromClosed(); STA.validStatusTransitionAddsOneHistoryEntry(); STS.validTransitionUpdatesStatusAndAddsOneHistoryRow() | UI-011, UI-013; API-004; SQL-004 | Covered |
| AC-2 | Record has previous status, new status, and change timestamp | WID.showsStatusHistoryForCreationAndTransitions() (asserts previousStatus, newStatus, **and** that the "Changed" column is non-blank, for both the creation row and a transition row) | UI-011, UI-013 (review timestamps/ordering) | Covered |
| AC-3 | Record is associated with the correct work item | STS/WPS queries and API responses are scoped to the created item's own id throughout | SQL-004 | Covered |
| AC-4 | Rejected transitions create no history record | STA.invalidStatusTransitionIsRejectedAndUnchanged(); STS.rejectedTransitionLeavesStatusAndHistoryUnchanged() (no UI automated test checks history count after a rejection) | API-005; SQL-005 (UI-005 step 4 says "if practical") | Covered |
| AC-5 | Existing history is preserved when a work item is reopened | SW.preservesStatusHistoryWhenReopenedFromClosed() | UI-013 | Covered |
| AC-6 | Status history displayed on the detail view | WID.showsStatusHistoryForCreationAndTransitions(), .preservesStatusHistoryWhenReopenedFromClosed() | UI-011, UI-013 | Covered |
| AC-7 | Creation writes an initial NEW history record | WID.showsStatusHistoryForCreationAndTransitions(); WCA.createsItemWithSuppliedValuesNewStatusAndInitialHistoryEntry(); WPS.creationPersistsExactlyOneInitialHistoryRow() | UI-011, UI-013; API-001; SQL-002 | Covered |

## WF-004 — Update a Work-Item Description

| AC | Acceptance Criterion | Automated | Manual | Status |
|---|---|---|---|---|
| AC-1 | Description displayed on the detail view | WID.editsAndSavesWorkItemDetails() | UI-009 | Covered |
| AC-2 | Description editable after creation | WID.editsAndSavesWorkItemDetails() | UI-009 | Covered |
| AC-3 | Saving a changed description updates the modification timestamp | WPS.editingAWorkItemAdvancesItsPersistedUpdatedDate() (asserts the persisted `updated_date` is strictly later after an edit); WID.editsAndSavesWorkItemDetails() (asserts the Updated value is present after save) | UI-009 (step 7 compares before/after) | Covered |
| AC-4 | Changing the description creates no history record | WID.editsAndSavesWorkItemDetails() (asserts `historyRows()` count is unchanged - still 1 - before and after the title/owner/description edit+save) | *(none)* | Covered |

## WF-005 — Save Work-Item Changes

| AC | Acceptance Criterion | Automated | Manual | Status |
|---|---|---|---|---|
| AC-1 | Single Save Changes button covers all editable fields | WID.editsAndSavesWorkItemDetails() (asserts `saveChangesButton()` has exactly one match, and demonstrates it persisting title+owner+description together) | UI-016 (step 2) | Covered |
| AC-2 | Save Changes starts disabled | WID.editsAndSavesWorkItemDetails() | UI-009 | Covered |
| AC-3 | Changing title/owner/description enables Save Changes | WID.editsAndSavesWorkItemDetails(), .revertingEditsToOriginalValuesDisablesSaveChangesAgain(), .promptsToDiscardUnsavedChangesOnClose() | UI-009, UI-010 | Covered |
| AC-4 | Reverting edits to originals disables Save Changes again | WID.revertingEditsToOriginalValuesDisablesSaveChangesAgain() | UI-015 | Covered |
| AC-5 | Save Changes persists all changed fields together | WID.editsAndSavesWorkItemDetails() | UI-009, UI-014 | Covered |
| AC-6 | Successful save updates the list (values + timestamp) and closes the detail view | WID.editsAndSavesWorkItemDetails() | UI-009, UI-014 | Covered |
| AC-7 | Editing title/owner/description creates no history record | WID.editsAndSavesWorkItemDetails() (same before/after `historyRows()` count check as WF-004 AC-4) | *(none)* | Covered |
| AC-8 | Closing with no unsaved changes closes immediately | WID.closesImmediatelyWhenNoUnsavedChanges() (opens a clean detail view, closes it, asserts it's hidden immediately) | *(none)* | Covered |
| AC-9 | Closing with unsaved changes prompts to discard | WID.promptsToDiscardUnsavedChangesOnClose() | UI-010 | Covered |
| AC-10 | Canceling the discard prompt keeps the edit | WID.promptsToDiscardUnsavedChangesOnClose() | UI-010 | Covered |
| AC-11 | Confirming discard closes without saving | WID.promptsToDiscardUnsavedChangesOnClose() | UI-010 | Covered |
| AC-12 | Description field has a fixed, non-resizable size | *(intentionally not automated - visual/layout requirement)* | UI-016 (step 3) | **Manual only** |
| AC-13 | Close/Save Changes grouped in a bottom-right action area, Save Changes primary | *(intentionally not automated - visual/layout requirement)* | UI-016 (steps 4-5) | **Manual only** |

## WF-006 — Create a Work Item

| AC | Acceptance Criterion | Automated | Manual | Status |
|---|---|---|---|---|
| AC-1 | A nonblank title is required | CWI.rejectsAnItemWithNoTitle() | UI-001, UI-002 | Covered |
| AC-2 | May be created with or without a description | CWI.createsAnItemWithOwnerAndDescription() (with); CWI.createsAnItemWithNoDescription() (without - creates with a blank description and asserts success) | UI-001, UI-003 (with only) | Covered |
| AC-3 | May be created with an existing owner or left unassigned | CWI.createsAnItemWithOwnerAndDescription() (with owner); WPS.ownerAssignmentAndUnassignedAreRepresentedCorrectly() (unassigned) | UI-002, SQL-003 | Covered |
| AC-4 | Create is disabled until title is nonblank | CWI.rejectsAnItemWithNoTitle() | UI-002 | Covered |
| AC-5 | Create uses the entered title, description, and owner | WCA.createsItemWithSuppliedValuesNewStatusAndInitialHistoryEntry(); WPS.createdItemPersistsSuppliedValuesAndOwnerRelationship() | UI-001, UI-003; API-001; SQL-001 | Covered |
| AC-6 | New item starts in NEW | CWI.createsANewItemAndDisplaysItInTheTable(); WCA.createsItemWithSuppliedValuesNewStatusAndInitialHistoryEntry(); WPS.createdItemPersistsSuppliedValuesAndOwnerRelationship() | UI-001, UI-003; SQL-001 | Covered |
| AC-7 | New item appears in the work-item list | CWI.createsANewItemAndDisplaysItInTheTable(); WCA.createsItemWithSuppliedValuesNewStatusAndInitialHistoryEntry() (persisted, retrievable by id) | UI-001, UI-003; API-001 | Covered |
| AC-8 | Form resets and Create re-disables after success | CWI.createsANewItemAndDisplaysItInTheTable() | UI-001 | Covered |
| AC-9 | Failed creation keeps entered form values | CWI.keepsEnteredValuesAndShowsErrorWhenCreationFails() (intercepts the POST via `page.route()` to force a failure, asserts title/description inputs still hold what was entered) | *(none)* | Covered |
| AC-10 | Failed creation informs the user | CWI.keepsEnteredValuesAndShowsErrorWhenCreationFails() (asserts `#create-error` becomes non-blank) | *(none)* | Covered |
| AC-11 | Blank/whitespace-only title rejected server-side | WCA.rejectsCreationWithBlankOrWhitespaceTitle() | API-003 (UI-002 step 8 is exploratory only) | Covered |
| AC-12 | Nonexistent owner id rejected server-side | WCA.rejectsCreationWithNonexistentOwner() | API-002 | Covered |

---

## Other coverage (outside WF-001–WF-006 scope)

Per the Scope Notes in `product-stories.md`, existing search and filtering
behavior is pre-existing regression scope, not a WF-00X acceptance
criterion, so it isn't in the tables above:

| Layer | Test | Notes |
|---|---|---|
| UI | `SearchAndFilterTest.searchNarrowsTheListByTitle()` | Regression: title search narrows the list |
| UI | `SearchAndFilterTest.filteringByStatusShowsOnlyMatchingItems()` | Regression: status filter narrows the list |

Manual equivalents: UI-007, UI-008.

---

## Coverage summary

60 acceptance criteria across WF-001–WF-006:

| Status | Count |
|---|---|
| Covered | 58 |
| Manual only | 2 |
| Partial | 0 |
| Not covered | 0 |

**No acceptance criterion is Partial or Not covered.** Every gap identified
in the previous review round - the WF-002 status enumeration, the WF-003
history-record timestamp, the WF-005 single-button and no-op-history
checks, the WF-005 clean-close behavior, the WF-006 no-description and
creation-failure paths, and the WF-001/WF-004 "timestamp actually changes"
checks - has been closed by new or strengthened automated tests, cross-
checked against the corresponding manual workbook steps.

**Manual only (2), both intentional:**

- **WF-005 AC-12** (description field has a fixed, non-resizable size) and
  **WF-005 AC-13** (Close/Save Changes grouped bottom-right, Save Changes
  primary) are visual/layout requirements from `ux-conventions.md`, not
  business logic. They are deliberately left to manual verification
  (`UI-016`) rather than automated as a brittle CSS/DOM proxy check. This is
  by design, not a coverage gap.
