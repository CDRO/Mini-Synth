# Implementation Plan - Milestone 4: Metronome & BPM Control

Implement a high-precision audio-visual metronome system to provide a timing reference for recording and performance.

## User Review Required

> [!NOTE]
> The metronome will be implemented natively in the `AudioEngine` to ensure sample-accurate timing, avoiding any jitter from the Android UI thread.

## Proposed Changes

### [Audio Engine] (C++)

#### [MODIFY] [AudioEngine.h](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.h)
- Add `mBpm` (float, default 120).
- Add `mMetronomeEnabled` (bool).
- Add `mSamplesPerBeat` and `mSampleCounter` for timing logic.
- Add `processMetronome(int32_t numFrames)` helper.

#### [MODIFY] [AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- In `onAudioReady`: If metronome is enabled, generate a "tick" sound (short decaying sine burst or square pulse) at the start of each beat.
- Handle 4/4 time signature by default with a higher pitch accent on beat 1.

#### [MODIFY] [native-lib.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/native-lib.cpp)
- Add JNI bindings: `setBpm(float bpm)`, `setMetronomeEnabled(bool enabled)`.

---

### [UI & Kotlin]

#### [MODIFY] [SynthManager.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/audio/SynthManager.kt)
- Add external methods for BPM and Metronome control.

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
- Add a "Metronome" toggle button.
- Add BPM control module: "BPM" label, decrement button (-), value text, increment button (+).

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Bind new UI controls to `SynthManager`.
- (Optional) Implement a visual "flash" in the `VisualizerView` or a dedicated LED in the UI that syncs with the metronome beats.

---

## Verification Plan

### Automated Tests
- **Unit Test (C++)**: `MetronomeTimingTest` to verify that `mSamplesPerBeat` is calculated correctly for different BPMs and sample rates.
- **Instrumented Test (Kotlin)**: Verify that clicking the metronome button toggles the engine state.

### Manual Verification
- **Audio Check**: Enable metronome, set to 120 BPM, and verify the clicks are regular and clear.
- **BPM Check**: Change BPM to 60 and 240, and verify the timing speeds up/slows down accordingly.
- **Visual Sync**: Verify that any visual beat indicator (if added) matches the audio clicks.
