# Implementation Plan - Milestone 22: Advanced Pad Control

Implement expressive pad interactions including multi-pad hold gestures and a refined sample mapping interface.

## User Review Required

> [!IMPORTANT]
> **Pad Swipe Gesture**: Swiping from Pad A to Pad B while in 'Sampling Mode' or 'Hold Mode' will define a sustain duration or group multiple pads into a single trigger zone.

## Proposed Changes

### [Audio Engine]
#### [MODIFY] [SamplePlayer.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/SamplePlayer.cpp)
- Add support for **Loop Points** (Start/End) within a PCM buffer.
- Implement `setLooping(bool)` to allow pads to act as drones.

### [UI & Logic]
#### [MODIFY] [KeyboardPadView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt)
- Detect horizontal/vertical swipes within the Pad Grid.
- Provide visual feedback (line/glow) between connected pads during a swipe.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Update Pad Mode UI to include a **Sample Mapping Sidebar** on the left.
- Add 'Loop' toggle to individual pad configuration dialogs.

## Verification Plan
### Automated Tests
- **Unit Test**: Verify loop point logic in `SamplePlayer`.
- **UI Test**: Verify swipe gesture detection in `KeyboardPadView`.
