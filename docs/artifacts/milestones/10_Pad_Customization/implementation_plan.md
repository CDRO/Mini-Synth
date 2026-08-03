# Implementation Plan - Milestone 10: Dynamic Pad Customization

Expanding the 4x4 pad grid into a professional performance surface with dynamic columns and per-pad visual organization.

## User Review Required

> [!IMPORTANT]
> - The pad grid will now support a variable number of columns (default 4, up to 16).
> - Each pad will have a configurable "identity" including its backlight color and internal sound mapping.
> - A new "Pad Settings" drawer or module will be added to the UI to manage these properties.
> - The `KeyboardPadView` must be refactored to support dynamic layout calculations for the grid.

## Proposed Changes

### UI Component (Kotlin)

#### [MODIFY] [KeyboardPadView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt)
- Add `gridColumns: Int` and `gridRows: Int` properties.
- Refactor `onSizeChanged` to calculate `padRects` based on these dimensions.
- Add `setPadColor(padIndex: Int, color: Int)` and store it in a `Map<Int, Int>`.

### UI Integration (Kotlin)

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
- Add controls to adjust the number of grid columns.
- Add a "Pad Config" section that appears when a pad is long-pressed (or via a specific toggle).

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Implement long-press detection on the grid to open the Pad Config UI.
- Allow users to select a color and sound source (Oscillator vs. Sample) for the selected pad.

### Native Engine (C++)

#### [MODIFY] [VoiceManager.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/VoiceManager.cpp)
- Ensure the voice allocation logic correctly handles the expanded number of potential pad triggers (beyond the initial 16).

## Verification Plan

### Automated Tests
- **Instrumented Test**: Verify that changing the number of columns correctly updates the `padRects` count and layout.
- **Unit Test**: Verify that pad color settings are correctly stored and retrieved.

### Manual Verification
- Expand the grid to 8x4.
- Long-press Pad 0 and change its color to `Electric Blue`.
- Verify the change is reflected immediately and persists through mode toggles.
