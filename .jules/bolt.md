## 2024-06-25 - Task Filtering Performance
**Learning:** Re-renders triggered by state variables, when hoisted to the same level as expensive computations (like filtering lists), cause those computations to fire on every keystroke if not memoized.
**Action:** When implementing new features, ensure that the derived state (like a filtered list) relies only on the required inputs, by using `remember()` combined with variables like lists and selection filters to memoize computations.
