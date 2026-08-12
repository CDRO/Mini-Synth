# Implementation Plan - Milestone 36: UI Stability, Pad UX & Sequencer Repair

Consolidate visual and interactive fixes for the Mini-Synth, focusing on layout stability, performance interaction, and sequencer onboarding.

## User Review Required

> [!IMPORTANT]
> **Pad Interaction Model**: Introducing a dedicated **"EDIT" toggle** for pads. 
> - **EDIT OFF**: Pads support long-press sustaining. No interruptions.
> - **EDIT ON**: Touching any pad immediately opens its config.
> This replaces the conflicting 500ms long-press logic.

> [!NOTE]
> **Guided Sequencer Demo**: The automated tour will now auto-scroll to the sequencer and perform a step-by-step masterclass on loop recording.

## Proposed Changes

### [UI / Layout]

#### [MODIFY] [layout_header.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/layout_header.xml)
- **Fix Clipping**: Implement `app:layout_constraintHorizontal_weight` for the 4 BPM adjustment buttons to ensure they remain fully visible on all aspect ratios.
- **Visual Cleanup**: Remove the opaque grey `surface_bright` background from the engine status bar.

#### [MODIFY] [VisualizerView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/VisualizerView.kt)
- **Restore Red Peaks**: Adjust the `LinearGradient` stops to bring back the red saturation for high-amplitude frequency bars.

### [Kotlin / Logic]

#### [MODIFY] [KeyboardPadView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt)
- Implement `isConfigMode` property.
- Update `onTouchEvent` to branch based on `isConfigMode`.
- Enable "Slide Up to Hold" for pads in performance mode.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- **Sequencer Recording**: Audit the MIDI routing path to ensure bits are written to the sequencer grid in real-time.
- **Educational Demo**: Expand `runIntegratedDemo()` with a segment that auto-scrolls to the sequencer and demonstrates manual vs. real-time editing.

## Verification Plan

### Automated Tests
- **UI Test**: Verify that the "EDIT" toggle correctly enables/disables the pad config dialog trigger.
- **Sequencer Logic**: Verify bits are correctly written to the native sequencer grid during recording.

### Manual Verification
- Confirm all 4 header buttons are visible.
- Run the demo and verify auto-scrolling to the sequencer.
- Verify pads can be held for sustain without triggering a popup.
