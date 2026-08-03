# Milestone 9: Pad Sampling & Playback

Implementing the ability to record live synth output into a pad-specific PCM buffer and trigger it using the 4x4 pad grid.

## Checklist

### 1. Native Engine (C++)
- `[x]` Create `SamplePlayer.h/cpp` (PCM playback oscillator).
- `[x]` Update `Voice` to support `SamplePlayer` as an alternative to `Oscillator`.
- `[x]` Implement `PadManager` logic (in `AudioEngine`) to manage 16 PCM buffers.
- `[x]` Add JNI method `startPadSampling(int padIndex)`.
- `[x]` Add JNI method `stopPadSampling()`.

### 2. UI & Integration (Kotlin)
- `[x]` Add "Sample to Pad" toggle to `content_main.xml`.
- `[x]` Update `MainActivity.kt` to handle pad sampling logic.
- `[x]` Trigger sample playback when in PAD_GRID mode.

### 3. Verification & Quality
- `[x]` **Unit Test**: `PadSamplingTest.kt` (Verifies JNI and engine state).
- `[x]` **Instrumented Test**: Verify pad triggering and UI feedback.
- `[x]` **Regression**: Run all previous tests.

### 4. Workflow & Review
- `[ ]` Complete 10 review cycles.
- `[ ]` Squash and Merge to `main`.
