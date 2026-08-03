# Implementation Plan - Milestone 7: Visual MIDI Sequencer

Implementing a step-based MIDI sequencer that integrates with the existing high-performance audio engine and adheres to the "Stealth Synth" design guide.

## User Review Required

> [!IMPORTANT]
> - The sequencer will be hard-synced to the native Metronome clock to ensure sample-accurate timing.
> - `KeyboardPadView` will prioritize its `Blue` backlight for sequencer playback, unless the user is actively touching a key (where `Acid Green` takes priority).
> - Initial implementation will support 16 steps, with a future roadmap for expansion.

## Proposed Changes

### Native Audio Engine (C++)

#### [NEW] [MidiSequencer.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/MidiSequencer.h)
- Store a 16x128 bitmask (or sparse list) of active notes per step.
- Track current step based on samples elapsed.
- Support for `1/16`, `1/8`, `1/4`, `1/2`, and `1/1` step durations.
- Automatically trigger/release voices in the `VoiceManager` when crossing step boundaries.

#### [MODIFY] [AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- Add `MidiSequencer` instance.
- Pass `numFrames` and `mSamplesPerBeat` to the sequencer in `onAudioReady`.

### UI & Integration (Kotlin)

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
- Add a "Sequencer" header in the `ScrollView`.
- Horizontal row of 16 custom "LED" toggle buttons for step selection.
- Spinner for "Step Duration" (mapped to metronome divisions).

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Bind the 16 toggles to JNI sequencer methods.
- Update UI state based on the engine's current playback position.

## Verification Plan

### Automated Tests
- **C++ Unit Tests**: Verify that `MidiSequencer` calculates step transitions correctly at different BPMs and durations.
- **Espresso Tests**: Verify that toggling a step in the UI correctly updates the engine and triggers the `Blue` backlight on the keyboard.

### Manual Verification
- Set metronome to 120 BPM.
- Input a simple 4-beat melody.
- Verify timing is consistent and backlights follow the sequence.
- Stress test by changing BPM while the sequencer is running.
