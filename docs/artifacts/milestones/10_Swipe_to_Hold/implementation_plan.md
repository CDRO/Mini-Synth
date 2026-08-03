# Implementation Plan - Milestone 10: Swipe-to-Hold Pad Recording

Enhancing the sampling workflow by allowing users to define sample durations using swipe gestures on the 4x4 pad grid.

## User Review Required

> [!IMPORTANT]
> - A "Hold" mechanism will be implemented for pad sampling.
> - Instead of a fixed 5-second buffer, the recording will continue as long as the user's finger is on the pad grid (up to the maximum 5s limit).
> - Swiping from the initial pad to any other pad on the grid will "hold" the recording active.
> - Releasing all contact with the pad grid will stop the recording.
> - This requires internal touch tracking in `KeyboardPadView` to handle "grid-wide" capture state.

## Proposed Changes

### UI Component (Kotlin)

#### [MODIFY] [KeyboardPadView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt)
- Update `onTouchEvent` to track if any finger is currently touching *any* pad while in `PAD_GRID` mode and sampling is active.
- Implement a `isAnyPadTouched` helper.
- Update the listener interface or add a new callback to notify when the "grid touch state" changes.

### Native Audio Engine (C++)

#### [MODIFY] [SamplePlayer.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/SamplePlayer.cpp)
- No changes needed to the core logic, as it already supports `stopRecording()` to finalize the buffer.

#### [MODIFY] [AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- Ensure `startPadSampling` correctly clears the buffer before the first sample arrives.

### UI Integration (Kotlin)

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Update the `synthView.listener` to handle the new "hold" logic.
- Recording starts on first pad touch (in sampling mode).
- Recording stops only when `isAnyPadTouched` becomes false.

## Verification Plan

### Automated Tests
- **Instrumented Test**: `SwipeToHoldTest.kt` using `MotionEvent` simulation to verify that recording continues across pad boundaries.
- **Unit Test**: Verify that `SamplePlayer` buffer size matches the duration of the touch.

### Manual Verification
- Enable "Map to Pad".
- Touch Pad 0.
- Swipe your finger across Pads 1, 2, and 3.
- Release finger.
- Verify that Pad 0 now contains a sample long enough to cover the entire swipe duration.
