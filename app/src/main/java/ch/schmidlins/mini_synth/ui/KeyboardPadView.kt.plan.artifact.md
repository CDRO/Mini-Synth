# Implementation Plan - Fix IndexOutOfBoundsException and Stabilize Tests

## Problem Statement
The application crashes with a `java.lang.IndexOutOfBoundsException` in `KeyboardPadView.drawKeyboard` because it attempts to access `whiteKeyRects` before they are populated in `onSizeChanged`. This is caught by the automated Android tests.

## Proposed Changes

### 1. Fix `KeyboardPadView.kt`
- Add safety checks in `drawKeyboard`, `drawPadGrid`, and `getMidiAt` to ensure rectangle lists are not empty before accessing elements by index.
- Ensure `onSizeChanged` completes its clearing and populating atomically (or at least safely relative to `onDraw`).

### 2. Fix `TransformFragment.kt`
- Add a safety check in `onBindViewHolder` to prevent `IndexOutOfBoundsException` if the dataset size exceeds the hardcoded `drawables` list (16 items).

### 3. New Tests
- Create a new Android Test file `KeyboardLifecycleTest.kt` that specifically attempts to trigger drawing/touch events on `KeyboardPadView` in various states (e.g., before layout).
- Add a unit test `PresetRepositoryTest.kt` if not already present, to ensure robust preset handling.

### 4. Development Workflow Guide
- Create a document `DEVELOPMENT_WORKFLOW.md` outlining the required steps for testing and committing.

## Verification Plan
1. Run all unit tests: `./gradlew :app:testDebugUnitTest`
2. Run all Android tests: `./gradlew :app:connectedDebugAndroidTest`
3. Verify all tests pass on the emulator.

## Commit Plan
- Message: "Fix IndexOutOfBoundsException in KeyboardPadView and stabilize UI tests. Add safety checks for view layout timing."
