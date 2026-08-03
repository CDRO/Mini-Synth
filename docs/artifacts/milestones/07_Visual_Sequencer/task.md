# Milestone 7: Visual MIDI Sequencer

Implementing a step-based MIDI sequencer with real-time feedback and loop playback.

## Checklist

### 1. Native Engine (C++)
- `[x]` Implement `MidiSequencer.h/cpp` to manage a 16-step grid.
- `[x]` Integrate `MidiSequencer` into `AudioEngine` callback.
- `[x]` Sync sequencer "ticks" with the existing Metronome sample-clock.
- `[x]` Add JNI bridges for setting/clearing notes and step duration.

### 2. UI Implementation (Kotlin)
- `[x]` Add "Sequencer" module to `content_main.xml`.
- `[x]` Implement 16-step LED indicators and duration selector (1/16 to 1/1).
- `[x]` Update `MainActivity.kt` to bind the new UI.
- `[x]` Implement `Blue` backlight feedback on `KeyboardPadView` during sequencer playback.

### 3. Verification & Quality
- `[x]` **Unit Test**: `MidiSequencerTest.cpp` (Implemented as Kotlin/JNI test).
- `[x]` **Instrumented Test**: `SequencerUiTest.kt` for UI interaction and backlight priority.
- `[x]` **Regression**: Run all existing stability and audio tests.

### 4. Workflow & Review
- `[ ]` Push branch `feature/visual-sequencer`.
- `[ ]` Create PR with Why/Tests/Value.
- `[ ]` Complete 10 review cycles (Self-reviews + fixes).
- `[ ]` Merge Message review (2 iterations).
- `[ ]` Squash and Merge to `main`.
