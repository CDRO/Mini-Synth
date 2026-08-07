# Implementation Plan - Milestone 28: Integrated Demo & Automated Sampling (Enhanced)

Implement a comprehensive scripted "Feature Tour" that showcases all synthesis, sequencer, and effect capabilities while automatically generating sample banks.

## User Review Required

> [!IMPORTANT]
> **Scripted Sequence**: The demo will be a non-interactive sequence that:
> 1. Resets the engine.
> 2. Demonstrates each waveform (Sine -> Square -> Saw -> Triangle).
> 3. Records each waveform into a separate pad (P0-P3).
> 4. Plays a polyphonic sequence using the recorded pads.
> 5. Modulates FX (Delay/Reverb) during playback.
> 6. Ends with a full "Project" demonstration.

## Proposed Changes

### [UI & Scripting]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Refactor `playDemoSong` to `runIntegratedDemo`.
- Implement step-by-step `delay()` based scripting.
- Add UI "Tele-prompter" style toasts to explain what's happening.

### [Logic]

#### [MODIFY] [SynthManager.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/audio/SynthManager.kt)
- Ensure all necessary parameters for the demo are exposed.

## Verification Plan

### Manual Verification
- Trigger 'DEMO' button.
- Verify that pads 0-3 are correctly populated with recorded oscillators.
- Verify that the sequence switches between Keys and Pads automatically.
