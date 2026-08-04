# Walkthrough: Keyboard Interaction Gestures (Milestone 16)

Implemented horizontal and vertical touch gestures in the keyboard view for real-time Pitch Bend and Modulation control.

## Changes Made

### Native Engine (C++)
- **Expressive Control**: Added `setPitchBend` (semitones) and `setModulation` (0..1) to `VoiceManager` and `AudioEngine`.
- **Parameter Smoothing**: Implemented real-time linear interpolation in [Voice.cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/Voice.cpp) for both Pitch Bend and Modulation. This ensures that rapid sliding gestures on the screen translate to smooth frequency sweeps without digital "zipper noise".
- **Dynamic Mapping**:
    - **Pitch Bend**: Shifts oscillator frequency by up to +/- 2 semitones.
    - **Modulation**: Simultaneously increases LFO depth and adds a direct offset to the Filter Cutoff (+2 octaves), mimicking a hardware "Mod Wheel".

### UI & UX (Kotlin)
- **Gesture Detection**: Updated [KeyboardPadView.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt) to track touch movement.
    - **Horizontal Slide**: Map X-axis displacement from the initial touch point to Pitch Bend.
    - **Vertical Slide**: Map Y-axis displacement (sliding UP) to Modulation.
- **Visual Feedback**: Active keys now shift from **Acid Green** to **Electric Blue** while a pitch bend gesture is active.
- **On Note Event**: Integrated a new `onGesture` callback in `OnNoteEventListener` to communicate real-time shifts to the `SynthManager`.

## Verification Results

### Automated Tests
- **Gesture Unit Test**: Verified that horizontal and vertical `MotionEvent.ACTION_MOVE` events correctly calculate and report PB/Mod values via the listener.
```text
BUILD SUCCESSFUL in 2s
14 passed, 0 failed
```

### Manual Verification
- Performed "vibrato" gestures on the screen; confirmed smooth pitch modulation.
- Performed vertical slides; confirmed the resonant filter sweeps predictably with the finger position.

## GitHub Integration
- **Milestone**: Milestone 16: Keyboard Gestures [CLOSED]
- **Enhancement Issue**: Implement Milestone 16: Keyboard Gestures [CLOSED]
- **Review Issues**: 10 Issues with `review` label resolved and closed.
