# Implementation Plan - Milestone 8: Keyboard Step-Recording

Enabling users to compose melodies by playing notes on the keyboard, with each note automatically assigned to the current sequencer step.

## User Review Required

> [!IMPORTANT]
> - A new "Record Mode" will be added to the sequencer.
> - When active, any note played on the keyboard will be stored in the sequencer grid at the current step.
> - The sequencer will automatically advance to the next step after each note is recorded.
> - This mode works best when the sequencer is STOPPED, but can be used during playback for real-time capturing.

## Proposed Changes

### Native Audio Engine (C++)

#### [MODIFY] [MidiSequencer.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/MidiSequencer.h)
- Add `recordNote(int note)`.
- If `recordNote` is called, set bit for `note` at `mCurrentStep` and increment `mCurrentStep` (wrap at 16).

### UI & Integration (Kotlin)

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
- Add `ToggleButton` "REC MODE" next to the Play/Stop button.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Listen for keyboard `onNoteOn`.
- If `isSequencerRecordMode` is true, call JNI `recordNote`.
- Update the step LED grid to reflect the new note.

## Verification Plan

### Automated Tests
- **Unit Test**: Verify that calling `recordNote` multiple times correctly populates the grid and advances the index.
- **Espresso Test**: Verify that clicking a keyboard key with REC MODE on updates the sequencer step visuals.

### Manual Verification
- Turn on REC MODE.
- Play "C, E, G".
- Verify that steps 0, 1, 2 now have note 60, 64, 67.
- Play the sequence and verify sound output.
