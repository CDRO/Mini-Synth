# Tasks: Milestone 48 - Multi-Track Architecture & Demo Update

- [ ] Demo Modernization
    - [ ] Refactor `runIntegratedDemo` in `MainActivity.kt` to cover Morphing, Wavetables, Unison, and Phase Distortion.
    - [ ] Standardize all demo strings in `strings.xml`.
- [ ] Workstation Foundation
    - [ ] Create `Track.h` and integrate into `AudioEngine`.
    - [ ] Update `MidiSequencer` for 4-track grid support.
    - [ ] Refactor `VoiceManager` for track-aware voice allocation.
- [ ] JNI Expansion
    - [ ] Update JNI bridge with track-indexed setters.
    - [ ] Sync `SynthManager.kt` with new native signatures.
- [ ] UI Workstation Interface
    - [ ] Create `TrackSelectorView` custom tab component.
    - [ ] Integrate Track Selector into the main control bar.
    - [ ] Implement UI sync logic (switching active track data).
- [ ] Persistence & Polish
    - [ ] Update `ProjectManager` to handle multi-track JSON format.
    - [ ] Final stress testing for voice stealing efficiency.
