# Implementation Plan - Milestone 36: UI Stability, Pad UX & Sequencer Repair

This plan addresses layout clipping, visual regressions in the visualizer, pad performance conflicts, and the broken sequencer recording workflow.

## User Review Required

> [!IMPORTANT]
> **New Pad Workflow**: I am introducing an **"EDIT" toggle** for the Pad area.
> - **OFF (Performance)**: Pads behave musically. Long-press sustains the note. No dialog interrupts you.
> - **ON (Configuration)**: Touching any pad immediately opens its configuration dialog.

> [!NOTE]
> **Demo Training**: The integrated demo will be upgraded to be educational. It will **auto-scroll** to the sequencer section and perform a "Sequencer Masterclass" showing the user exactly how to record and edit loops.

## Proposed Changes

### [UI / Layout]

#### [MODIFY] [layout_header.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/layout_header.xml)
- **Fix Clipping**: Adjust constraints and horizontal weights for BPM adjustment buttons to ensure they fit on all displays.
- **Visual Polish**: Remove the opaque grey background from the status bar for a cleaner "Stealth" look.
- **Latency Label**: Update format to `LATENCY: %1$d (%2$d)` and refine placeholder.

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
- Add a labeled "EDIT" toggle to the Pad customization section.

#### [MODIFY] [VisualizerView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/VisualizerView.kt)
- **Restore Red Gradient**: Adjust `LinearGradient` stops to ensure the red peak color is visible during high-amplitude signals.

### [Kotlin / Logic]

#### [MODIFY] [KeyboardPadView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt)
- Implement `isConfigMode` logic.
- Remove immediate long-press trigger in Play mode; replace with sustain/hold logic.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- **Recording Fix**: Audit and repair the MIDI routing from keyboard to sequencer buffer.
- **Educational Demo**: Expand `runIntegratedDemo()` to include a tutorial sequence with auto-scrolling to the sequencer.

## Verification Plan

### Automated Tests
- **UI Interaction**: Test `KeyboardPadView` state transitions between Edit and Play modes.
- **Sequencer Logic**: Verify bits are correctly written to the native sequencer grid during recording.

### Manual Verification
- Confirm all 4 header buttons are visible on a narrow phone emulator.
- Run the demo and verify it scrolls to and demonstrates the sequencer.
- Verify pads can be held for sustain without triggering a popup.
