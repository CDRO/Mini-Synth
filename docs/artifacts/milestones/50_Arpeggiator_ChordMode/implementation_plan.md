# Implementation Plan - Milestone 50: Advanced Arpeggiator & Chord Mode (Pre-task: Metronome UI Rework)

## Goal
Improve the Metronome UI by placing all controls on a single line and wiring up fine tempo adjustments (+1/-1 BPM).

## Proposed Changes

### [UI / Layout]

#### [MODIFY] [layout_header.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/layout_header.xml)
- Rework `metronome_container` to use a single horizontal row for all controls.
- Controls sequence: [ON/OFF] [BeatIndicator] [-5] [-1] [BPM Value] [+1] [+5].
- Use smaller buttons or condensed layout to fit within the allotted header width.

### [Kotlin / Logic]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Update `setupMetronome` to wire `btn_bpm_down_fine` (-1 BPM) and `btn_bpm_up_fine` (+1 BPM).
- Ensure `isMetronomeEnabled` state is correctly reflected in the ToggleButton text/style.

## Verification Plan

### Manual Verification
- Verify all metronome buttons are visible and on the same horizontal line.
- Click **-5** and **+5**: BPM should change by 5.
- Click **-1** and **+1**: BPM should change by 1.
- Toggle **ON/OFF**: Metronome sound and indicator should start/stop.
