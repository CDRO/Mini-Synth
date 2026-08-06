# Implementation Plan - Milestone 24: Polyphonic Aftertouch Simulation

Enable per-voice expressive control by mapping vertical touch displacement on keyboard keys to synth parameters.

## User Review Required

> [!IMPORTANT]
> **Modulation Target**: By default, sliding up on a key will increase the Filter Cutoff for that specific voice only. A toggle to switch between Cutoff, LFO Depth, or Volume will be added.

## Proposed Changes

### [Audio Engine]

#### [MODIFY] [Voice.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/Voice.cpp)
- Add `setAftertouch(float amount)`.
- Apply aftertouch amount to the internal signal chain (e.g., as a multiplier for Cutoff).

#### [MODIFY] [VoiceManager.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/VoiceManager.cpp)
- Add `setVoiceAftertouch(int midiNote, float amount)`.
- Route the aftertouch value to the specific `Voice` object matching the note.

### [UI & Logic]

#### [MODIFY] [KeyboardPadView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt)
- Update `onTouchEvent` to track vertical displacement for EACH pointer independently.
- Calculate a 0.0 to 1.0 value based on Y-position within the key height.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Update `onNoteOn` / `onGesture` logic to pass per-note aftertouch to `SynthManager`.

## Verification Plan

### Automated Tests
- **Native Unit Test**: Verify that `Voice` cutoff responds correctly to `setAftertouch`.
- **Robolectric Test**: Verify that vertical touch movement triggers aftertouch JNI calls.

### Manual Verification
- Play a chord.
- Slide one finger up and another down.
- Verify that the brightness (filter) of individual notes changes independently.
