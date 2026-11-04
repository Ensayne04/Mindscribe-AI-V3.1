## 2024-06-03 - [Keyboard Navigation in Compose Forms]
**Learning:** Jetpack Compose forms often lack default "Next"/"Done" keyboard actions out of the box, making it tedious for users to tab between fields or hide the keyboard on mobile. This pattern is missing in `TaskPlannerScreen.kt` and `JournalScreen.kt`.
**Action:** Add `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)` and `keyboardActions` with `FocusManager.moveFocus(FocusDirection.Down)` to text fields to enable smooth, accessible keyboard navigation.
## 2026-11-03 - [Form Keyboard Optimization]
**Learning:** Providing explicit `KeyboardOptions` and `KeyboardActions` (ImeAction.Next, ImeAction.Done) greatly improves keyboard/accessibility flows in Compose forms, allowing users to progress inputs without dismissing the soft keyboard or tapping manually.
**Action:** Use `KeyboardOptions` and `FocusManager.moveFocus(FocusDirection.Down)` for sequence inputs across standard app forms.
