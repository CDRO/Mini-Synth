# Implementation Plan - Milestone 36: UI Polish & Interaction Repair

Repair critical visual regressions, restore visualizer fidelity, and decouple pad configuration from performance.

## User Review Required

> [!IMPORTANT]
> **Pad Interaction Model**: Introducing a dedicated **"EDIT" toggle** for pads. 
> - **EDIT OFF**: Pads behave musically. Long-press sustains the note. No dialog interrupts performance.
> - **EDIT ON**: Touching any pad immediately opens its configuration dialog.
> This replaces the conflicting 500ms long-press timer during performance.

> [!NOTE]
> **Guided Onboarding**: The integrated demo will be upgraded to include an **auto-scrolling "Masterclass"** segment that guides the user through sequencer loop creation.

## Proposed Changes

### [UI / Layout]

#### [MODIFY] [layout_header.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/layout_header.xml)
- **Fix Clipping**: Adjust constraints and weights for the BPM adjustment buttons (`-5`, `-1`, `+1`, `+5`) to ensure they remain fully visible on all screen sizes.
- **Visual Cleanup**: Remove the opaque grey background from the status bar area.

#### [MODIFY] [VisualizerView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/VisualizerView.kt)
- **Restore Red Peaks**: Refine the `LinearGradient` stops in the spectrum analyzer to ensure vibrant red colors are visible at high amplitudes.

### [Kotlin / Logic]

#### [MODIFY] [KeyboardPadView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt)
- Implement `isConfigMode` logic.
- Remove immediate long-press trigger in Play mode; replace with sustain/hold logic.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- **Recording UI Refresh**: Ensure newly recorded notes appear immediately in the step grid.
- **Educational Demo**: Expand `runIntegratedDemo()` to include a tutorial sequence with auto-scrolling to the sequencer.

## Verification Plan

### Automated Tests
- **Unit Tests**: Verify `KeyboardPadView` state transitions between Edit and Play modes.

### Manual Verification
- Confirm all 4 header buttons are visible.
- Run the demo and verify it scrolls to and demonstrates the sequencer.
- Verify pads can be held for sustain without triggering a popup.
