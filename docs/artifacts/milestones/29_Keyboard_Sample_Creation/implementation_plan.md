# Implementation Plan - Milestone 29: Keyboard Sample Creation

Implement step-by-step melody recording for the keyboard, allowing users to build complex sequences programmatically.

## User Review Required

> [!IMPORTANT]
> **Recording Model**: This will be a non-real-time recording mode. Each note pressed on the keyboard will advance the sequencer by one step (determined by the grid size).

## Proposed Changes

### [Audio Engine]

#### [MODIFY] [AudioEngine.h](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.h)
- Add hooks for step-recording mode.

#### [MODIFY] [MidiSequencer.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/MidiSequencer.cpp)
- Implement `recordStep(int note)` that clears the step and adds the note, then increments.

### [UI & Integration]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Add "Step Record" toggle to the Sequencer panel.
- Update keyboard listener to trigger `recordStep` when active.

## Verification Plan

### Automated Tests
- **Unit Test (C++)**: Verify `recordStep` increments and loops back.

### Manual Verification
- Enable 'Step Rec'.
- Press C, E, G, B.
- Play sequence and verify the 4-note loop.
