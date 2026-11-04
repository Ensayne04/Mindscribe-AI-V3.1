## 2026-11-03 - [Form Keyboard Optimization]
**Learning:** Providing explicit `KeyboardOptions` and `KeyboardActions` (ImeAction.Next, ImeAction.Done) greatly improves keyboard/accessibility flows in Compose forms, allowing users to progress inputs without dismissing the soft keyboard or tapping manually.
**Action:** Use `KeyboardOptions` and `FocusManager.moveFocus(FocusDirection.Down)` for sequence inputs across standard app forms.
