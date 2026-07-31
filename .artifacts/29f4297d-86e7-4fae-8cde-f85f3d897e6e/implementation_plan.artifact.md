# Implementation Plan - Fix IndexOutOfBoundsException and Stabilize Tests

The application is crashing with a `java.lang.IndexOutOfBoundsException` in `KeyboardPadView` when drawing or handling touches before the view is fully laid out. This plan fixes these issues and establishes a robust testing workflow.

## User Review Required

> [!IMPORTANT]
> I will be modifying `KeyboardPadView.kt` and `TransformFragment.kt` to add defensive checks against `IndexOutOfBoundsException`. I will also add new tests to prevent regressions.

## Proposed Changes

### UI Components

#### [MODIFY] [KeyboardPadView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt)
- Add empty checks for `whiteKeyRects` and `padRects` in `drawKeyboard`, `drawPadGrid`, and `getMidiAt`.
- This ensures that if `onDraw` is called before `onSizeChanged` has populated these lists, the app won't crash.

#### [MODIFY] [TransformFragment.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/transform/TransformFragment.kt)
- Add a safety check in `onBindViewHolder` for the `drawables` list access.

### Testing & Workflow

#### [NEW] [KeyboardLifecycleTest.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/androidTest/java/ch/schmidlins/mini_synth/ui/KeyboardLifecycleTest.kt)
- Add a test that verifies `KeyboardPadView` can handle `onDraw` and `onTouchEvent` safely even if not yet laid out.

#### [NEW] [DEVELOPMENT_WORKFLOW.artifact.md](file:///C:/Users/tizia/Projekte/Mini-Synth/.artifacts/29f4297d-86e7-4fae-8cde-f85f3d897e6e/DEVELOPMENT_WORKFLOW.artifact.md)
- A guide for developers on the testing and PR process.

## Verification Plan

### Automated Tests
- Run all unit tests: `:app:testDebugUnitTest`
- Run all Android tests: `:app:connectedDebugAndroidTest`
- Expected: All tests (existing and new) should pass.

### Manual Verification
- Deploy the app to the emulator and interact with all screens immediately after launch.
