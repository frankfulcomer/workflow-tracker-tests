# Exploratory Testing Charter

This charter replaces the scripted API and SQL manual workbooks, and absorbs
the investigative content that used to live inside several retired UI manual
test cases. It exists for a different purpose than the scripted UI suite in
`workflow_tracker_manual_ui_tests.xlsx`: the items below have no fixed
expected result to check off, no acceptance criterion of their own, or are
better investigated freely than re-walked through the same steps every run.

This is deliberately not a set of pass/fail test cases. Each item is a
prompt: spend a bounded amount of time investigating it, and log anything
noteworthy (a defect, a UX concern, a question about intended behavior, or
"looked fine") wherever the team currently tracks findings. Retire or add
charters as the product changes; a charter earns its place by still being
worth investigating, not by tradition.

## UI

- **Title-field whitespace-only input.** Type only spaces into the create
  form's Title field. Does the Create button's enabled state behave
  sensibly? Does anything get created? *(Moved from the former UI-002,
  step 8, which was already self-labeled exploratory.)*
- **Search behavior.** Case-sensitivity, partial-match behavior, the
  debounce/typing delay before results update, stale-result behavior (does
  a slow search response ever land after a faster one and clobber it?),
  and special characters in the search box. There is no acceptance
  criterion governing exact search semantics — this is existing,
  out-of-increment-scope behavior. *(Moved from the former UI-007.)*
- **Status filter behavior.** Same edge cases as search, plus combined
  search+filter interaction (what happens when both are set at once?).
  *(Moved from the former UI-008.)*
- **Save Changes dirty-state edge cases.** Does trailing/leading whitespace
  in a field, or a value changed and then changed back via
  focus/blur rather than a direct match, ever leave Save Changes enabled
  or disabled incorrectly? *(Moved from the former UI-015.)*
- **General visual/responsive investigation.** Browser zoom levels,
  narrow/mobile viewport widths, and unusual data (very long titles or
  descriptions, special characters, emoji) — does layout hold up?

## API

- **Malformed or unusual request payloads.** Missing required fields,
  wrong JSON types, unexpected extra fields, oversized strings, on both
  the create and update endpoints.
- **Concurrent status-transition requests for the same item.** Fire two or
  more overlapping `PATCH .../status` requests for one item and observe
  the result. This directly probes a known gap: `WorkItemService`'s status
  and edit paths have no per-item locking, so overlapping requests can be
  evaluated against a stale prior state (see the `StatusWorkflowTest`
  synchronization investigation in this repo's history for a concrete
  example of the underlying race). Not a scripted regression check — an
  ongoing probe for how bad the impact is.
- **API error-message quality and consistency.** Read the error body
  across different rejection scenarios (invalid transition, invalid
  owner, blank title, malformed payload) and judge whether the wording is
  consistent and useful to a developer integrating against the API. Ties
  into the already-logged "API error-response cleanup" backlog item in
  the application's `product-stories.md`.

## Data / SQL

- **Ad hoc integrity spot-checks** after any notable schema or data
  change — not a standing script, a targeted look driven by what changed.
- **Follow-up investigation tool**, not a charter in its own right: when
  UI or API exploratory testing turns up something that looks wrong, use
  direct SQL against the running instance to confirm what was actually
  persisted, the same way the automated SQL layer does.

## Why these moved here instead of staying scripted

Each item above either has no written acceptance criterion to script
against (search/filter semantics, general visual investigation), or its
deterministic core is already reliably automated and repeating it by hand
adds nothing beyond what a human notices while poking at the edges (title
whitespace, dirty-state edge cases). The full reasoning and per-test
classification is in `docs/traceability.md` and the manual-test
consolidation review that produced it.
