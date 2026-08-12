# Implementation Plan - Milestone 36: UI & Visualizer Stability

Repair critical UI regressions, restore visual fidelity to the spectrum analyzer, and standardize the Pad interaction model.

## User Review Required

> [!IMPORTANT]
> **Pad Configuration Change**: To resolve the conflict between "Pad Sustaining" and "Pad Config", I am introducing an **"EDIT" toggle**. 
> - **EDIT OFF**: Pads behave like keyboard keys (hold to sustain, no config dialog).
> - **EDIT ON**: Touching any pad immediately opens its configuration dialog.
> This eliminates the problematic 500ms long-press timer during performance.

## Proposed Changes

### [UI / Layout]

#### [MODIFY] [layout_header.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/layout_header.xml)
- Adjust the `metronome_container` constraints. Use `0dp` with `app:layout_constraintHorizontal_weight` instead of fixed percentages if needed to prevent clipping of the 4 BPM buttons on the right.
- Standardize the width of the BPM adjustment buttons to ensure they fit within the 25% header segment.

#### [MODIFY] [VisualizerView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/VisualizerView.kt)
- **Restore Red Gradient**: The linear gradient stops will be adjusted. `vibrantRed` will be moved closer to the `0.8f` mark to ensure it is visible even at moderate amplitudes.
- Verify the `LinearGradient` coordinates to ensure they align perfectly with the spectrum drawing area (bottom half).

### [Kotlin / Logic]

#### [MODIFY] [KeyboardPadView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt)
- Implement `isConfigMode` logic:
    - If `isConfigMode` is `true`, `onNoteOn` triggers `onPadLongPress` immediately.
    - If `isConfigMode` is `false`, `onTouchEvent` supports the same "Slide Up to Hold" logic as the keyboard.

## Verification Plan

### Manual Verification
- **Header**: Check that `-5`, `-1`, `+1`, `+5` buttons are fully visible on a standard smartphone aspect ratio.
- **Visualizer**: Play a loud sound and verify that the tops of the spectrum bars turn red.
- **Pad Mode**: Toggle "EDIT" and verify that pads can be sustained when it's off.
