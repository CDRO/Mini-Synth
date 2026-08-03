# Implementation Plan - Audio Optimization and Stability Refinement

This plan addresses the optimization and stability refinements identified in the engineering review for the `fix/audio-and-ui-stability` cycle.

## User Review Required

> [!NOTE]
> I will be creating a new branch `fix/audio-and-ui-usability` as requested to house these refinements.

## Proposed Changes

### Audio Engine (C++)

#### [MODIFY] [AudioEngine.h](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.h)
- Add a constant `MAX_RESTART_RETRIES`.
- Add `mRestartRetryCount` (int) and `mLastRestartTime` (chrono time_point) to track engine restarts.
- Define a `PI_F` constant.

#### [MODIFY] [AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- **Optimization**: Update `getMetronomeSample()` and `updateMetronomeParams()` to use `PI_F` and ensure all math is done using `float` literals.
- **Stability**: Update `onErrorAfterClose()` to implement a "cool-down" period and a retry limit. If the engine crashes too many times in a short interval, it will stop attempting to restart to avoid infinite loops.

## Verification Plan

### Automated Tests
- Run existing stress tests: `./gradlew :app:connectedDebugAndroidTest`.
- These tests already exercise high BPM and polyphony, which will trigger the optimized math paths.

### Manual Verification
- Deploy to emulator.
- Verify sound output remains consistent.
- (Destructive test): Simulate a stream error to verify the restart logic with cool-down.
