# Implementation Plan - Milestone 36: Pad Configuration Refinement

Decouple pad playback from configuration to allow for expressive long-press sustaining and fix interaction conflicts.

## User Review Required

> [!IMPORTANT]
> **New Interaction Model**:
> 1.  **Play Mode**: (Default) Long-pressing a pad will sustain the note. The configuration dialog will NOT open.
> 2.  **Edit Mode**: (Via "EDIT" toggle) Touching a pad will immediately open its configuration dialog.
> This replaces the previous "500ms long-press to config" logic which conflicted with musical performance.

## Proposed Changes

### [UI / Layout]

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
- Ensure the "EDIT" toggle for pads is clearly labeled and accessible.

### [Kotlin / Logic]

#### [MODIFY] [KeyboardPadView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt)
- Remove the `handler.postDelayed` logic for `onPadLongPress`.
- Implement logic where `onPadLongPress` (or immediate config open) only happens if `isConfigMode` is active.
- Ensure that in "Play Mode", a long touch on a pad adds it to the `heldMidiNotes` set (similar to the keyboard hold gesture).

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Update "Help" descriptions to explain the difference between Play and Edit modes for pads.

## Verification Plan

### Manual Verification
- **Sustaining**: Hold a pad for 2 seconds. The note should continue to play and NO dialog should appear.
- **Editing**: Toggle "EDIT" on. Touch the same pad. The configuration dialog should appear immediately.
- **Visuals**: Verify that the "H" indicator appears on held pads.
