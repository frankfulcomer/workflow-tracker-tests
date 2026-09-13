# Traceability

Every test in this suite carries a `// WF-00X AC-Y` comment tying it to a
specific acceptance criterion in workflow-tracker's
[`product-stories.md`](https://github.com/frankfulcomer/workflow-tracker/blob/main/docs/product-stories.md).
This table is sourced from those in-code comments — it isn't a separately
maintained artifact, so it can't drift from what the tests actually assert.

This is **deliberately selected regression coverage, not comprehensive
application coverage**. Many acceptance criteria — especially layout/UX
conventions in `ux-conventions.md` — aren't suited to automated assertion
and are intentionally not represented here.

| Layer | Test | Story / AC |
|---|---|---|
| UI | `CreateWorkItemTest.createsANewItemAndDisplaysItInTheTable` | WF-006 AC-5, AC-6, AC-7 |
| UI | `CreateWorkItemTest.rejectsAnItemWithNoTitle` | WF-006 AC-4 |
| UI | `CreateWorkItemTest.createsAnItemWithOwnerAndDescription` | WF-001 AC-1, AC-3 |
| UI | `SearchAndFilterTest.searchNarrowsTheListByTitle` | Pre-existing search behavior (see Scope Notes) |
| UI | `SearchAndFilterTest.filteringByStatusShowsOnlyMatchingItems` | Pre-existing filter behavior (see Scope Notes) |
| UI | `StatusWorkflowTest.movesAnItemThroughTheHappyPathWorkflow` | WF-002 AC-4, AC-6, AC-7 |
| UI | `StatusWorkflowTest.rejectsDirectTransitionFromNewToClosed` | WF-002 AC-12, AC-13 |
| UI | `StatusWorkflowTest.movesInAndOutOfOpenStatus` | WF-002 AC-3, AC-5, AC-12, AC-13 |
| UI | `WorkItemDetailTest.editsAndSavesWorkItemDetails` | WF-004 AC-1, AC-2; WF-005 AC-2, AC-3, AC-6 |
| UI | `WorkItemDetailTest.promptsToDiscardUnsavedChangesOnClose` | WF-005 AC-9, AC-10, AC-11 |
| UI | `WorkItemDetailTest.showsStatusHistoryForCreationAndTransitions` | WF-003 AC-1, AC-2, AC-6, AC-7 |
| API | `WorkItemCreationApiTest.createsItemWithSuppliedValuesNewStatusAndInitialHistoryEntry` | WF-001 AC-7; WF-003 AC-7; WF-006 AC-5, AC-6, AC-7 |
| API | `WorkItemCreationApiTest.rejectsCreationWithNonexistentOwner` | WF-006 AC-12 |
| API | `WorkItemCreationApiTest.rejectsCreationWithBlankOrWhitespaceTitle` | WF-006 AC-11 |
| API | `StatusTransitionApiTest.validStatusTransitionAddsOneHistoryEntry` | WF-002 AC-4; WF-003 AC-1, AC-2, AC-3 |
| API | `StatusTransitionApiTest.invalidStatusTransitionIsRejectedAndUnchanged` | WF-002 AC-12, AC-13, AC-15; WF-003 AC-4 |
| API | `StatusTransitionApiTest.sameStatusTransitionIsNoOpWithNoNewHistory` | WF-002 AC-14 |
| SQL | `WorkItemPersistenceSqlTest.createdItemPersistsSuppliedValuesAndOwnerRelationship` | WF-001 AC-1, AC-3; WF-006 AC-5 |
| SQL | `WorkItemPersistenceSqlTest.creationPersistsExactlyOneInitialHistoryRow` | WF-003 AC-2, AC-7 |
| SQL | `WorkItemPersistenceSqlTest.ownerAssignmentAndUnassignedAreRepresentedCorrectly` | WF-001 AC-1, AC-2, AC-9 |
| SQL | `StatusTransitionSqlTest.validTransitionUpdatesStatusAndAddsOneHistoryRow` | WF-002 (NEW → IN_PROGRESS); WF-003 AC-1, AC-2, AC-3 |
| SQL | `StatusTransitionSqlTest.rejectedTransitionLeavesStatusAndHistoryUnchanged` | WF-002 AC-12, AC-13; WF-003 AC-4 |

22 tests: 11 UI, 6 API, 5 SQL.
