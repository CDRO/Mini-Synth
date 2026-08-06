# Walkthrough: Polyphonic Aftertouch Simulation (Milestone 24)

Implemented per-finger pressure simulation by mapping vertical touch displacement on each key to synthesizer parameters.

## Changes Made

### Native Engine (C++)
- **Per-Voice Aftertouch**: Added `setAftertouch` to the `Voice` class and `setVoiceAftertouch` to `VoiceManager`.
- **Expressive Mapping**: Implemented an Aftertouch Target system in [Voice.cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/Voice.cpp).
    - **Filter (Default)**: Sweeps Cutoff by up to +4 octaves.
    - **Volume**: Increases gain by up to +50%.
    - **Pitch**: Shifts frequency by +/- 2 semitones.
- **Smoothing**: Integrated per-voice linear interpolation for aftertouch values to ensure smooth transitions and avoid digital artifacts.

### UI & Interaction
- **Multi-Touch Expressiveness**: Updated [KeyboardPadView.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt) to calculate absolute Y-position (0.0 to 1.0) for every active pointer.
- **Target Selection**: Added a new Spinner to the sequencer bar to select the global Aftertouch Target.
- **Visual Intensity**: Keys now dynamically modulate their backlight brightness (alpha) based on the current aftertouch value, providing immediate visual feedback for finger pressure.

### Optimizations
- **JNI Efficiency**: Implemented a 1% change threshold in [MainActivity.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt) to reduce the frequency of JNI calls during rapid gestures.
- **State Integrity**: Ensured aftertouch values are reset to 0 in both the engine and UI state on every new note trigger and release.

## Verification Results

### Automated Tests
- Verified 16 passing local JVM tests (Robolectric).
- Gesture detection and reporting confirmed via existing `GestureTest.kt`.

### Manual Verification
- Played multi-finger chords; verified that sliding one finger while keeping others stationary only modulates the intended note's timbre.

## GitHub Integration
- **Milestone**: Milestone 24: Polyphonic Aftertouch [CLOSED]
- **Enhancement Issue**: #108 [CLOSED]
- **Review Issues**: 10 total review loops resolved and closed.
