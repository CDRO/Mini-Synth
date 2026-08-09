# Implementation Plan - Milestone 31: Dynamic Grid & Pad Customization

Expand the performance interface with a flexible pad grid and visual customization options.

## User Review Required

> [!WARNING]
> **Layout Shift**: Expanding the grid to 4x16 will require vertical scrolling for the pad area. We will introduce a `ScrollView` wrapper around the `KeyboardPadView` in `PAD_GRID` mode.

## Proposed Changes

### [UI / Layout]

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
- Wrap `keyboardPadView` in a `NestedScrollView`.
- Add "Config Toggle" button to the sidebar.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Implement `toggleConfigVisibility()` to hide parameter sliders.
- Add UI to select grid dimensions (4x4, 4x8, 4x16).

#### [MODIFY] [KeyboardPadView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt)
- Support dynamic rows beyond screen height.
- Implement per-pad color rendering.

## Verification Plan

### Manual Verification
- Toggle "Config OFF". Verify sliders disappear and Pad Grid expands.
- Select "4x8" grid. Verify scrollability.
- Map a sample and assign a custom color. Verify color persistence in UI.
