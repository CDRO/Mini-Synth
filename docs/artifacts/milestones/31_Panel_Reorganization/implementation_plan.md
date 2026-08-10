# Implementation Plan - Milestone 31: Panel Reorganization & Hierarchy

Refactor the Sequencer and Pad Customization panels to eliminate horizontal clipping and improve functional grouping.

## User Review Required

> [!IMPORTANT]
> **UI Changes**: This milestone will change the visual layout of the Sequencer controls. I propose moving `SAVE`, `LOAD`, `EXP`, and `IQ` into a popup overflow menu to free up space for the primary transport and recording controls.

## Proposed Changes

### [UI / Layout]

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
*   Refactor `sequencer_section` to use a more compact header.
*   Introduce an `ImageButton` for the "Options" overflow.
*   Reorganize `pad_customization_section` into a vertical layout using `LinearLayout` or `TableLayout`.

### [Kotlin / Logic]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
*   Implement `PopupMenu` logic for the Sequencer options.
*   Update view binding references to the new layout structure.

## Verification Plan

### Manual Verification
*   Verify that all sequencer functions (SAVE/LOAD/EXP/IQ) are accessible via the new menu.
*   Verify that the "Bank" control is fully visible on standard 16:9 landscape displays.
*   Confirm that button text no longer clips on narrower landscape screens.
