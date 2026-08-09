# Implementation Plan - Milestone 30: Pad Holding & Expressive Gestures

Implement advanced touch interactions for the pad grid, including swipe-to-hold (sustained notes) and gesture-based modulation (pitch/filter).

## User Review Required

> [!IMPORTANT]
> **Pad Hold Logic**: We will use a "velocity-based" swipe detection. A rapid swipe from one pad to another will trigger polyphonic playback (existing behavior). A "slow and long" vertical swipe on a single pad (or after triggering) will toggle the HOLD state for that specific pad.

## Proposed Changes

### [UI / KeyboardPadView]

#### [MODIFY] [KeyboardPadView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt)
- Implement `padHold` logic similar to the keyboard hold.
- Enable vertical gesture detection in `Mode.PAD_GRID` to trigger Aftertouch/Modulation.
- Visual feedback for held pads (dashed border or color glow).

### [Logic / JNI]

#### [MODIFY] [SynthManager.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/audio/SynthManager.kt)
- Ensure pad-specific modulation parameters are accessible.

## Verification Plan

### Manual Verification
- Trigger Pad 0.
- Slide UP on Pad 0 > 50% height.
- Release touch. Verify Pad 0 stays active.
- Slide DOWN on Pad 0. Verify Pad 0 releases.
- Play a pad and slide UP slowly. Verify Aftertouch modulation (Volume/Filter) is audible.
