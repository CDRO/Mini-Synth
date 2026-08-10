# Implementation Plan - Milestone 34: Pad UX & Audio Stability Refinement

Address interaction conflicts in Pad Mode, refine the Engine Status visuals, and mitigate audio clipping.

## User Review Required

> [!IMPORTANT]
> **Pad Interaction Change**: I am introducing a "CONFIG" toggle for the Pad Grid. When "CONFIG" is OFF, long-pressing pads will NOT open the configuration dialog, allowing for sustained notes. When "CONFIG" is ON, touching a pad will immediately open its configuration.

## Proposed Changes

### [UI / Layout]

#### [MODIFY] [layout_header.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/layout_header.xml)
- Remove the `surface_bright` background from `engine_status_bar` to eliminate the "greyish area".
- Use `surface_dark` or transparent background with a subtle border to fit the "Stealth Synth" aesthetic.

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
- Add a `ToggleButton` for "PAD CONFIG" in the Pad Customization section.

### [Kotlin / Logic]

#### [MODIFY] [KeyboardPadView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt)
- Add an `isConfigMode` property.
- Update `onTouchEvent` to only trigger `onPadLongPress` if `isConfigMode` is true.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Connect the "PAD CONFIG" toggle to `KeyboardPadView.isConfigMode`.

### [Audio Engine]

#### [MODIFY] [VoiceManager.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/VoiceManager.cpp)
- Investigate mixer normalization. If 16 voices are active, the sum can easily exceed 1.0. 
- Implement a more aggressive soft-clipper or reduce the per-voice gain slightly to prevent harsh digital clipping.

## Verification Plan

### Manual Verification
- **Pad Sustaining**: Verify that pads can be held indefinitely without opening the config dialog when "CONFIG" is OFF.
- **Visuals**: Confirm the "greyish area" in the header is gone.
- **Audio**: Listen for reduced clipping under heavy polyphonic load.
