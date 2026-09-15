# Traceability

This table maps every acceptance criterion in workflow-tracker's
[`product-stories.md`](https://github.com/frankfulcomer/workflow-tracker/blob/main/docs/product-stories.md)
(stories WF-001 through WF-006) to the automated and/or manual test(s) that
verify it, based on the automated suite's last full run (**30 tests: 18 UI,
6 API, 6 SQL — 0 failures, 0 errors, 0 skipped**) and the scripted manual UI
suite under `docs/manual-tests/`.

Coverage here is derived directly from each acceptance criterion's text
against what the test code actually asserts (for automated tests) and what
each manual test step's *Expected Result* actually instructs the tester to
check (for manual tests) - not from the `// WF-00X AC-Y` shorthand comments
alone, and not from a manual step's Requirement-column citation alone. A
test that merely creates or uses a behavior as setup (e.g. creating an item
with an owner in order to test something else) is **not** counted as
coverage of that behavior unless the test also asserts on it.

## Manual-suite consolidation (2026-09-15)

The manual regression suite was restructured from "every acceptance
criterion gets a scripted manual test" to a risk- and value-based strategy:
keep scripted manual coverage where human observation, workflow coherence,
or visual/usability judgment adds something automation can't, automate
deterministic/repetitive checks, and move genuinely open-ended
investigation to an exploratory charter instead of a fixed script.

Result: **27 scripted manual cases → 6.** All 6 API and all 5 SQL manual
cases were retired - those layers have no visual/usability dimension and
are already fully, reliably automated. Of the original 16 UI cases:

| Outcome | Cases |
|---|---|
| Kept, scripted | UI-001, UI-004, UI-005, UI-009, UI-013, UI-016 |
| Merged into a kept case | UI-003 → UI-001; UI-006 → UI-005; UI-010 → UI-009; UI-014 → UI-009 |
| Moved to the exploratory charter | UI-002 (partial), UI-007, UI-008, UI-015 (partial) |
| Removed (fully redundant/automated) | UI-011, UI-012 |

The retained UI-0xx IDs keep their original numbers even where a sheet's
content changed (e.g. UI-005 is now "Status Transition Rules," absorbing
former UI-006). The full per-case classification and reasoning lives in the
review that produced this consolidation; the scripted suite itself is
`docs/manual-tests/workflow_tracker_manual_ui_tests.xlsx`, and open-ended
investigation now lives in
[`docs/manual-tests/exploratory-testing-charter.md`](manual-tests/exploratory-testing-charter.md).

No acceptance criterion became uncovered as a result - every AC that lost
manual representation already had (and keeps) reliable automated coverage.
See the Coverage summary at the end of this document.

## Coverage classifications

- **Covered** - at least one automated test explicitly asserts the behavior described by the AC.
- **Manual only** - no automated test explicitly asserts it, but a documented manual test step does.
- **Partial** - some verification exists (automated and/or manual) but it does not fully exercise the AC as written.
- **Not covered** - no automated or manual test explicitly addresses the AC.

These four values are what the **Status** column uses, and are unaffected by
the manual-suite consolidation above - every AC that had automated coverage
before still has it. Where a manual case was retired in favor of an
exploratory-charter prompt instead of disappearing outright, the **Manual**
column says so explicitly (e.g. "Exploratory charter (...)"); this is
supplementary context, not a fifth Status value, since the AC's underlying
verification is already accounted for by its automated coverage.

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

Manual test IDs (`UI-0xx`) refer to the sheets in
`docs/manual-tests/workflow_tracker_manual_ui_tests.xlsx`. The former
`API-0xx` and `SQL-0xx` manual suites, and several retired `UI-0xx` cases,
were retired or folded into
[`docs/manual-tests/exploratory-testing-charter.md`](manual-tests/exploratory-testing-charter.md) -
see the consolidation summary above.

---

## WF-001 — Assign an Owner to a Work Item

| AC | Acceptance Criterion | Automated | Manual | Status |
|---|---|---|---|---|
| AC-1 | Owner may be assigned at creation | CWI.createsAnItemWithOwnerAndDescription(); WCA.createsItemWithSuppliedValuesNewStatusAndInitialHistoryEntry(); WPS.createdItemPersistsSuppliedValuesAndOwnerRelationship(), .ownerAssignmentAndUnassignedAreRepresentedCorrectly() | UI-001 | Covered |
| AC-2 | May be created without an owner | WPS.ownerAssignmentAndUnassignedAreRepresentedCorrectly() (creates an unassigned item and asserts it succeeds) | *(none - automated only; former SQL-003 retired, no visual/usability dimension)* | Covered |
| AC-3 | Assigned owner displayed on the list | CWI.createsAnItemWithOwnerAndDescription(); WID.editsAndSavesWorkItemDetails(); WPS.createdItemPersistsSuppliedValuesAndOwnerRelationship() | UI-001, UI-009 | Covered |
| AC-4 | Assigned owner displayed on the detail view | CWI.createsAnItemWithOwnerAndDescription(); WID.editsAndSavesWorkItemDetails(), .changesOwnerFromAssignedToUnassigned() | UI-001, UI-009 | Covered |
| AC-5 | Owner can be changed after creation | WID.editsAndSavesWorkItemDetails() | UI-009 | Covered |
| AC-6 | Owner can be changed from assigned to unassigned | WID.changesOwnerFromAssignedToUnassigned() | UI-009 (step 8) | Covered |
| AC-7 | Creation records a creation timestamp | WCA.createsItemWithSuppliedValuesNewStatusAndInitialHistoryEntry() (asserts `createdDate` is non-null on both the POST response and the follow-up GET) | *(none - automated only; former API-001 retired, pure JSON-contract check)* | Covered |
| AC-8 | Modification records an updated timestamp | WPS.editingAWorkItemAdvancesItsPersistedUpdatedDate() (asserts the persisted `updated_date` is strictly later after an edit); WID.editsAndSavesWorkItemDetails() (asserts the Updated value is present after save - the UI only renders second-level precision, so timestamp *advancement* is verified at the SQL layer, not the UI) | UI-009 (step 7 compares before/after - a human naturally takes more than a second between edit steps, so this remains a meaningful check) | Covered |
| AC-9 | No owner ⇒ no stored owner relationship (NULL FK) | WPS.ownerAssignmentAndUnassignedAreRepresentedCorrectly() | *(none - automated only; former SQL-003 retired, pure relational-integrity check)* | Covered |

## WF-002 — Manage Work-Item Status

| AC | Acceptance Criterion | Automated | Manual | Status |
|---|---|---|---|---|
| AC-1 | Supported statuses are NEW/OPEN/IN_PROGRESS/RESOLVED/CLOSED | SW.movesAnItemThroughTheHappyPathWorkflow() (asserts a row's status-select options are exactly these five, in order) | *(none)* | Covered |
| AC-2 | New item starts in NEW | CWI.createsANewItemAndDisplaysItInTheTable(); WCA.createsItemWithSuppliedValuesNewStatusAndInitialHistoryEntry(); WPS.createdItemPersistsSuppliedValuesAndOwnerRelationship() | UI-001, UI-004, UI-005, UI-013 | Covered |
| AC-3 | NEW → OPEN allowed | SW.movesInAndOutOfOpenStatus(); WID.showsStatusHistoryForCreationAndTransitions() | UI-005 | Covered |
| AC-4 | NEW → IN_PROGRESS allowed | SW.movesAnItemThroughTheHappyPathWorkflow(), .supportsAllowedBackwardTransitions(); STA.validStatusTransitionAddsOneHistoryEntry(); STS.validTransitionUpdatesStatusAndAddsOneHistoryRow() | UI-004, UI-013 | Covered |
| AC-5 | OPEN → IN_PROGRESS allowed | SW.movesInAndOutOfOpenStatus(), .supportsAllowedBackwardTransitions() | UI-005 | Covered |
| AC-6 | IN_PROGRESS → RESOLVED allowed | SW.movesAnItemThroughTheHappyPathWorkflow(), .supportsAllowedBackwardTransitions() | UI-004, UI-013 | Covered |
| AC-7 | RESOLVED → CLOSED allowed | SW.movesAnItemThroughTheHappyPathWorkflow(), .supportsAllowedBackwardTransitions() | UI-004, UI-013 | Covered |
| AC-8 | IN_PROGRESS → OPEN allowed | SW.supportsAllowedBackwardTransitions() | *(none - automated only; former UI-012 removed, pure state-machine check already exercised with per-step synchronization; see consolidation review)* | Covered |
| AC-9 | RESOLVED → IN_PROGRESS allowed | SW.supportsAllowedBackwardTransitions() | *(none - automated only; former UI-012 removed, same reasoning as AC-8)* | Covered |
| AC-10 | RESOLVED → OPEN allowed | SW.supportsAllowedBackwardTransitions() | *(none - automated only; former UI-012 removed, same reasoning as AC-8)* | Covered |
| AC-11 | CLOSED → OPEN allowed | SW.supportsAllowedBackwardTransitions(), .preservesStatusHistoryWhenReopenedFromClosed() | UI-013 | Covered |
| AC-12 | Any unlisted transition is rejected | SW.rejectsDirectTransitionFromNewToClosed(), .movesInAndOutOfOpenStatus(); STA.invalidStatusTransitionIsRejectedAndUnchanged(); STS.rejectedTransitionLeavesStatusAndHistoryUnchanged() | UI-005 | Covered |
| AC-13 | A rejected transition leaves status unchanged | SW.rejectsDirectTransitionFromNewToClosed(), .movesInAndOutOfOpenStatus(); STA.invalidStatusTransitionIsRejectedAndUnchanged(); STS.rejectedTransitionLeavesStatusAndHistoryUnchanged() | UI-005 | Covered |
| AC-14 | Re-selecting the current status is a no-op | STA.sameStatusTransitionIsNoOpWithNoNewHistory() (no UI or SQL automated test exercises the no-op case) | *(none - automated only; former API-006 retired. Never had UI manual coverage either - a pure deterministic no-op check)* | Covered |
| AC-15 | A rejected transition shows an error message | SW.rejectsDirectTransitionFromNewToClosed() (captures and asserts a non-blank dialog message); STA.invalidStatusTransitionIsRejectedAndUnchanged() (asserts a non-blank `error` body) | UI-005 (human judgment of error wording - two independent rejection scenarios) | Covered |

## WF-003 — Preserve Status History

| AC | Acceptance Criterion | Automated | Manual | Status |
|---|---|---|---|---|
| AC-1 | Every successful status change creates a history record | WID.showsStatusHistoryForCreationAndTransitions(); SW.preservesStatusHistoryWhenReopenedFromClosed(); STA.validStatusTransitionAddsOneHistoryEntry(); STS.validTransitionUpdatesStatusAndAddsOneHistoryRow() | UI-013 | Covered |
| AC-2 | Record has previous status, new status, and change timestamp | WID.showsStatusHistoryForCreationAndTransitions() (asserts previousStatus, newStatus, **and** that the "Changed" column is non-blank, for both the creation row and a transition row) | UI-013 (review timestamps/ordering) | Covered |
| AC-3 | Record is associated with the correct work item | STS/WPS queries and API responses are scoped to the created item's own id throughout | UI-013 (step 6: "entries remain associated with this item") | Covered |
| AC-4 | Rejected transitions create no history record | STA.invalidStatusTransitionIsRejectedAndUnchanged(); STS.rejectedTransitionLeavesStatusAndHistoryUnchanged() (no UI automated test checks history count after a rejection) | UI-005 (step 9 says "if practical") | Covered |
| AC-5 | Existing history is preserved when a work item is reopened | SW.preservesStatusHistoryWhenReopenedFromClosed() | UI-013 | Covered |
| AC-6 | Status history displayed on the detail view | WID.showsStatusHistoryForCreationAndTransitions(), .preservesStatusHistoryWhenReopenedFromClosed() | UI-013 | Covered |
| AC-7 | Creation writes an initial NEW history record | WID.showsStatusHistoryForCreationAndTransitions(); WCA.createsItemWithSuppliedValuesNewStatusAndInitialHistoryEntry(); WPS.creationPersistsExactlyOneInitialHistoryRow() | UI-013 (step 1) | Covered |

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
| AC-3 | Changing title/owner/description enables Save Changes | WID.editsAndSavesWorkItemDetails(), .revertingEditsToOriginalValuesDisablesSaveChangesAgain(), .promptsToDiscardUnsavedChangesOnClose() | UI-009 | Covered |
| AC-4 | Reverting edits to originals disables Save Changes again | WID.revertingEditsToOriginalValuesDisablesSaveChangesAgain() | *(none - automated only; former UI-015's core check retired, pure deterministic dirty-state logic. Whitespace/focus edge cases moved to the exploratory charter)* | Covered |
| AC-5 | Save Changes persists all changed fields together | WID.editsAndSavesWorkItemDetails() | UI-009 | Covered |
| AC-6 | Successful save updates the list (values + timestamp) and closes the detail view | WID.editsAndSavesWorkItemDetails() | UI-009 | Covered |
| AC-7 | Editing title/owner/description creates no history record | WID.editsAndSavesWorkItemDetails() (same before/after `historyRows()` count check as WF-004 AC-4) | *(none)* | Covered |
| AC-8 | Closing with no unsaved changes closes immediately | WID.closesImmediatelyWhenNoUnsavedChanges() (opens a clean detail view, closes it, asserts it's hidden immediately) | *(none)* | Covered |
| AC-9 | Closing with unsaved changes prompts to discard | WID.promptsToDiscardUnsavedChangesOnClose() | UI-009 (steps 9-10, absorbed from former UI-010) | Covered |
| AC-10 | Canceling the discard prompt keeps the edit | WID.promptsToDiscardUnsavedChangesOnClose() | UI-009 (step 11) | Covered |
| AC-11 | Confirming discard closes without saving | WID.promptsToDiscardUnsavedChangesOnClose() | UI-009 (step 12) | Covered |
| AC-12 | Description field has a fixed, non-resizable size | *(intentionally not automated - visual/layout requirement)* | UI-016 (step 3) | **Manual only** |
| AC-13 | Close/Save Changes grouped in a bottom-right action area, Save Changes primary | *(intentionally not automated - visual/layout requirement)* | UI-016 (steps 4-5) | **Manual only** |

## WF-006 — Create a Work Item

| AC | Acceptance Criterion | Automated | Manual | Status |
|---|---|---|---|---|
| AC-1 | A nonblank title is required | CWI.rejectsAnItemWithNoTitle() | UI-001 (positive path only - nonblank accepted; the negative/blank-rejected path is automated-only since former UI-002's core check was fully deterministic and redundant) | Covered |
| AC-2 | May be created with or without a description | CWI.createsAnItemWithOwnerAndDescription() (with); CWI.createsAnItemWithNoDescription() (without - creates with a blank description and asserts success) | UI-001 (with only, unchanged from before) | Covered |
| AC-3 | May be created with an existing owner or left unassigned | CWI.createsAnItemWithOwnerAndDescription() (with owner); WPS.ownerAssignmentAndUnassignedAreRepresentedCorrectly() (unassigned) | UI-001 (with an existing owner only; "left unassigned" is automated-only - former manual mapping here was already a loose one) | Covered |
| AC-4 | Create is disabled until title is nonblank | CWI.rejectsAnItemWithNoTitle() | *(none - automated only; former UI-002 removed, deterministic button-state check)* | Covered |
| AC-5 | Create uses the entered title, description, and owner | WCA.createsItemWithSuppliedValuesNewStatusAndInitialHistoryEntry(); WPS.createdItemPersistsSuppliedValuesAndOwnerRelationship() | UI-001 | Covered |
| AC-6 | New item starts in NEW | CWI.createsANewItemAndDisplaysItInTheTable(); WCA.createsItemWithSuppliedValuesNewStatusAndInitialHistoryEntry(); WPS.createdItemPersistsSuppliedValuesAndOwnerRelationship() | UI-001 | Covered |
| AC-7 | New item appears in the work-item list | CWI.createsANewItemAndDisplaysItInTheTable(); WCA.createsItemWithSuppliedValuesNewStatusAndInitialHistoryEntry() (persisted, retrievable by id) | UI-001 | Covered |
| AC-8 | Form resets and Create re-disables after success | CWI.createsANewItemAndDisplaysItInTheTable() | UI-001 | Covered |
| AC-9 | Failed creation keeps entered form values | CWI.keepsEnteredValuesAndShowsErrorWhenCreationFails() (intercepts the POST via `page.route()` to force a failure, asserts title/description inputs still hold what was entered) | *(none)* | Covered |
| AC-10 | Failed creation informs the user | CWI.keepsEnteredValuesAndShowsErrorWhenCreationFails() (asserts `#create-error` becomes non-blank) | *(none)* | Covered |
| AC-11 | Blank/whitespace-only title rejected server-side | WCA.rejectsCreationWithBlankOrWhitespaceTitle() | Exploratory charter (client-side whitespace-title behavior); the server-side contract itself is automated-only (former API-003 retired) | Covered |
| AC-12 | Nonexistent owner id rejected server-side | WCA.rejectsCreationWithNonexistentOwner() | *(none - automated only; former API-002 retired)* | Covered |

---

## Other coverage (outside WF-001–WF-006 scope)

Per the Scope Notes in `product-stories.md`, existing search and filtering
behavior is pre-existing regression scope, not a WF-00X acceptance
criterion, so it isn't in the tables above:

| Layer | Test | Notes |
|---|---|---|
| UI | `SearchAndFilterTest.searchNarrowsTheListByTitle()` | Regression: title search narrows the list |
| UI | `SearchAndFilterTest.filteringByStatusShowsOnlyMatchingItems()` | Regression: status filter narrows the list |

Manual equivalent: **Exploratory charter** (search and status-filter
sections) - former UI-007/UI-008 were retired as scripted cases since no
acceptance criterion governs exact search/filter semantics; the open-ended
edge cases they existed to probe (case sensitivity, partial match, timing,
stale results) are better suited to free investigation than a fixed script.

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
checked against the corresponding manual workbook steps. The 2026-09-15
manual-suite consolidation changed *how* several of these ACs get their
manual representation (or removed manual representation where automation
alone is reliable and sufficient) but did not reduce the Covered count.

**Manual only (2), both intentional:**

- **WF-005 AC-12** (description field has a fixed, non-resizable size) and
  **WF-005 AC-13** (Close/Save Changes grouped bottom-right, Save Changes
  primary) are visual/layout requirements from `ux-conventions.md`, not
  business logic. They are deliberately left to manual verification
  (`UI-016`) rather than automated as a brittle CSS/DOM proxy check. This is
  by design, not a coverage gap.

**Automated-only ACs introduced by the manual-suite consolidation:**
WF-001 AC-2, AC-7, AC-9; WF-002 AC-8, AC-9, AC-10, AC-14; WF-005 AC-4;
WF-006 AC-4, AC-12 (each previously had a now-retired scripted manual case
whose core check was fully deterministic and already reliably automated -
see the consolidation summary above for per-case reasoning). WF-006 AC-1
and AC-3 keep partial manual representation via UI-001 but lose it for
their negative/unassigned-specific half; WF-006 AC-11 keeps its
server-side contract automated-only while its client-side edge case moves
to the exploratory charter.
