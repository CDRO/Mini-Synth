# Task: Audio Debugging & UX Refinement

Fixing silence on emulators via multi-channel mapping and improving waveform selection UI.

## Checklist
- `[/]` **Native Engine Fixes**
    - `[ ]` Implement stereo channel mapping in `AudioEngine.cpp`
    - `[ ]` Add audio thread logging for debugging
    - `[ ]` Implement `mMasterVolume` in `VoiceManager`
- `[ ]` **UI Refinement**
    - `[ ]` Replace Waveform Spinner with Icon-based Toggle Group
    - `[ ]` Add Master Volume slider
    - `[ ]` Fix initialization order in `MainActivity`
- `[ ]` **Testing & Validation**
    - `[ ]` Unit test for multi-channel buffer filling
    - `[ ]` Manual verification of sound on Pixel 9 emulator
    - `[ ]` **Regression**: Run all previous tests
- `[ ]` **Workflow & Review**
    - `[ ]` Push branch to GitHub
    - `[ ]` Create PR with technical "Why/Tests/Value" description
    - `[ ]` Review Cycle 1-5 (10 comments + fixes, separate comments)
    - `[ ]` Merge Message Review Loop (2 iterations)
    - `[ ]` Create `audio_refinement_walkthrough.artifact.md`
    - `[ ]` Squash and Merge to `main`

## Evidence of Success
- `[ ]` `./gradlew connectedCheck` Output
