# Milestone 10: Swipe-to-Hold Pad Recording

Implementing a gesture-based "hold" mechanism for sampling on the 4x4 pad grid.

## Checklist

### 1. UI Component (Kotlin)
- `[x]` Update `KeyboardPadView.kt` to track "any pad" touch state.
- `[/]` Implement swipe detection for sampling hold.

### 2. Activity Integration (Kotlin)
- `[ ]` Update `MainActivity.kt` listener to use hold logic.
- `[ ]` Ensure visual feedback (Red backlight) persists during the swipe.

### 3. Verification & Quality
- `[ ]` **Unit Test**: `SampleDurationTest.kt` for accurate timing.
- `[ ]` **Instrumented Test**: `SwipeToHoldTest.kt` for gesture simulation.
- `[ ]` **Regression**: Verify metronome and sequencer stability.

### 4. Workflow & Review [STRICT]
- `[ ]` Push branch `feature/swipe-to-hold`.
- `[ ]` **MANDATORY**: Create Pull Request via `gh pr create`.
- `[ ]` 10 Code Review Cycles (Requirement verification + fixes).
- `[ ]` Merge Message Review (2 iterations).
- `[ ]` Squash and Merge to `main`.
