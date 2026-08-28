# Implementation Plan - Milestone 49: Advanced Modulation Matrix

Upgrade the modulation system with BPM-synced LFOs and a multi-target modulation matrix.

## User Review Required

> [!IMPORTANT]
> **Modulation Matrix UI**: I will implement a "MATRIX" button in the LFO section. Clicking this will open a dialog allowing you to set the modulation depth for all targets (Pitch, Volume, Filter, Phase Distortion) simultaneously. This replaces the single-target spinner for more complex sound design.

> [!NOTE]
> **LFO Sync Divisions**: Supported divisions will be: 1/1, 1/2, 1/4 (Beat), 1/8, and 1/16.

## Proposed Changes

### [Audio Engine]

#### [MODIFY] [Lfo.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/Lfo.h)
- Add `mIsSynced` and `mSyncDivision` properties.
- Add `setBpm(float bpm)` to update the phase increment based on the global tempo.
- Ensure smooth transitions when switching between manual frequency (Hz) and sync mode.

#### [MODIFY] [Voice.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/Voice.cpp)
- Replace `mLfoTarget` with `float mLfoTargets[4]` (Weights for Pitch, Vol, Filter, PD).
- Update `nextSample()` to calculate the LFO value once and apply it to all parameters using their respective weights.
- Refactor the modulation summing logic to handle the new multi-target paths.

#### [MODIFY] [AudioCommon.h](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioCommon.h)
- Update `EngineParams` to store the new matrix weights and sync settings.

### [JNI Bridge]

#### [MODIFY] [SynthManager.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/audio/SynthManager.kt)
- Add `setLfoSync(trackIndex, enabled, division)` JNI declaration.
- Add `setLfoMatrixAmount(trackIndex, targetIndex, amount)` JNI declaration.

### [UI / Kotlin]

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
- Add a "SYNC" ToggleButton to the LFO row.
- Add a "DIV" Spinner for choosing the sync division.
- Add a "MATRIX" Button to trigger the new routing dialog.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Implement the "MATRIX" dialog showing 4 sliders for the LFO targets.
- Update `TrackState` to persist the new modulation routings.
- Ensure the BPM display and LFO are in sync during the educational demo.

## Verification Plan

### Automated Tests
- **LFO Sync Test**: Verify that at 120 BPM, a 1/1 sync division results in exactly one LFO cycle every 2 seconds.
- **Matrix Summation Test**: Ensure that routing LFO to both Pitch and Filter doesn't cause clipping or unexpected voice termination.

### Manual Verification
- Enable **SYNC**. Change BPM. Confirm the LFO speed changes proportionally.
- Open **MATRIX**. Set Pitch to 20% and Filter to 80%. Confirm both are modulated simultaneously by a single LFO.
