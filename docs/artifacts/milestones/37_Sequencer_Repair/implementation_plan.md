# Implementation Plan - Milestone 37: Sequencer Logic & Recording Repair

Fix the real-time and step-recording regressions to restore full sequencer functionality.

## User Review Required

> [!CAUTION]
> This milestone involves auditing the C++ `MidiSequencer` logic to ensure that `handleRealTimeNoteOn` correctly writes bits to the atomic grid.

## Proposed Changes

### [Audio Engine]

#### [MODIFY] [MidiSequencer.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/MidiSequencer.cpp)
- Debug and fix `handleRealTimeNoteOn`. Ensure the `targetStep` calculation accounts for the current sample position correctly.
- Verify that `isRecording` flag is being respected in the `process` loop.

### [Kotlin / Integration]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Audit the `onNoteOn` listener to ensure `handleRealTimeNoteOn` is called with the correct MIDI note and is not being swallowed by other logic.

## Verification Plan

### Manual Verification
- Start Sequencer Playback.
- Enable REC.
- Play a melody.
- Verify that notes appear in the step grid and play back on the next loop iteration.
