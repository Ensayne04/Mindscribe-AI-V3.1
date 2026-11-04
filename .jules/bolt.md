## 2024-06-25 - Task Filtering Performance
**Learning:** Re-renders triggered by state variables, when hoisted to the same level as expensive computations (like filtering lists), cause those computations to fire on every keystroke if not memoized.
**Action:** When implementing new features, ensure that the derived state (like a filtered list) relies only on the required inputs, by using `remember()` combined with variables like lists and selection filters to memoize computations.
## 2024-05-14 - Compose Recomposition Filtering Bottlenecks
**Learning:** In Compose UI files (like `TaskPlannerScreen.kt`), raw list filtering operations (e.g., `tasks.filter { ... }`) executed inside the @Composable function body without being wrapped in a `remember` block cause unnecessary O(N) operations during every recomposition (such as when typing in a text field).
**Action:** When working with large lists or expensive transformations in Jetpack Compose, always wrap the computation in `remember(dependencies...) { ... }` to ensure it only recalculates when the dependencies actually change.

## 2024-11-01 - [Avoid Unmemoized Operations in Timer-Driven Screens]
**Learning:** A running timer updates UI state frequently (e.g. `timeRemainingSeconds` collected as state). This causes the screen composable (like `DashboardScreen`) to recompose every second. If expensive data formatting (like creating `SimpleDateFormat` or `Calendar` instances) or heavy data processing (like loop-based string operations and Regex parsing for themes) are left unmemoized in such composables, it triggers constant garbage collection thrashing and unnecessary CPU burn on every timer tick.
**Action:** Always wrap expensive text parsing, heavy collection iteration (e.g. `list.count`), and instantiations of heavy standard library classes (e.g. `Calendar`, `SimpleDateFormat`) inside a `remember` block when writing Jetpack Compose components containing highly volatile state like ticking timers.
