# Implementation Plan - UI Polish & Functional Fixes

Resolve visual inconsistencies in the layout and repair broken or hidden functionality in Pad and Sequencer modes.

## User Review Required

> [!IMPORTANT]
> **Keyboard Toggle Placement**: I will move the "Hide Keyboard" (▼/▲) button to be anchored to the bottom right of the workspace area, ensuring it doesn't jump to the top when the keyboard is hidden.
>
> **Pad Mode Visibility**: I will re-enable the **Sequencer** and **Pad Customization** sections when in Pad mode. Currently, they are hidden, which prevents users from configuring pads or using the sequencer alongside them.

## Proposed Changes

### [UI / Layout]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Update `updateWorkspaceVisibility()`:
    - Change the `toggle_keyboard` anchor from `keyboard_pad_view` (internal view) to `keyboard_pad_scroll_view` (the actual container).
    - Ensure `toggle_keyboard` stays at the bottom of the visible workspace even when the keyboard is hidden.
    - Remove the logic that hides `sequencerSection` and `padCustomizationSection` when `isPadMode` is active.
- Fix "Mock Rec" file extension:
    - Change `.mp3` to `.wav` in `btnMockRec` listener to match the actual `WavEncoder` implementation.

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
- Ensure the `toggle_keyboard` button is correctly layered on top of other components to avoid being obscured.

### [UI / Help]

- Verify that **every** interactive element has a dedicated `isHelpMode` check in its listener.
- Ensure all help messages use localized strings from `R.string`.

## Verification Plan

### Automated Tests
- Run `LayoutRatioTest` and `WorkspaceUiTest` to ensure visibility changes don't break existing constraints.

### Manual Verification
1.  **Keyboard Toggle**: Press the ▼ button. Verify the keyboard slides away and the button stays at the bottom right. Press ▲ to restore.
2.  **Pad Mode Config**: Switch to "Pads". Verify the "Pad Customization" section (containing "EDIT OFF/ON") is visible. Toggle "EDIT ON" and touch a pad to verify the config dialog appears.
3.  **Sequencer in Pad Mode**: Switch to "Pads". Verify the Sequencer is visible and functional.
4.  **Recording**: Press "REC", play some notes, and press "REC" again. Use a file manager to verify a `.wav` file was created in the app's internal storage and contains audio.
