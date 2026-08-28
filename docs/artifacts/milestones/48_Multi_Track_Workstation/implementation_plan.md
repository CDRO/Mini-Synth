# Implementation Plan - Milestone 48: Multi-Track Architecture & Demo Update

Upgrade Mini-Synth to a professional 4-track workstation and update the educational demo to cover all modern synthesis features.

## User Review Required

> [!IMPORTANT]
> **Track Resource Management**: We will maintain a global limit of **16 voices**. These voices are dynamically allocated across the 4 tracks on a "first-come, first-served" basis with voice stealing. This ensures high performance while allowing complex layers.

## Proposed Changes

### [UI / Demo]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- **Refactor `runIntegratedDemo`**:
    - Create a clean, linear walkthrough:
        1. **Morphing**: Sweep the MORPH slider through Sine -> Square.
        2. **Wavetables**: Load and play the "Vocal Ah" table.
        3. **Phase Distortion**: Showcase the warped phase visualization in the header.
        4. **Unison**: Stack 8 oscillators with wide stereo spread.
        5. **Random LFO**: Demonstrate the new S&H modulation on filter cutoff.
    - Standardize all demo toasts to use `strings.xml`.

### [Audio Engine]

#### [NEW] [Track.h](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/Track.h)
- Structure to hold per-track state:
    - `EngineParams` (Oscillator settings, ADSR, Filter).
    - `float mVolume`, `float mPanning`.
    - `bool mMuted`, `bool mSoloed`.

#### [MODIFY] [MidiSequencer.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/MidiSequencer.h)
- Update `mGrid` to `[4][MAX_STEPS][2]` to store independent patterns for 4 tracks.
- Update `process()` to accept the `AudioEngine` track array and trigger notes with track-specific context.

#### [MODIFY] [VoiceManager.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/VoiceManager.h)
- Update `noteOn` to accept a `trackId`.
- Voices will now query the corresponding `Track` object in the `AudioEngine` for their active parameters.

#### [MODIFY] [AudioEngine.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.h)
- Hold `std::array<Track, 4> mTracks`.
- Routing logic: Sum all track outputs into the master FX chain (Delay/Reverb).

### [JNI Bridge]

#### [MODIFY] [SynthManager.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/audio/SynthManager.kt)
- Update JNI signatures to include `trackIndex: Int` for all parameter setters.
- Example: `setFilterCutoff(trackIndex: Int, frequency: Float)`.

### [UI / Kotlin]

#### [NEW] [TrackSelectorView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/TrackSelectorView.kt)
- Custom component with 4 tabs (T1, T2, T3, T4).
- Shows "Mute/Solo" status indicators.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Manage `activeTrackIndex`.
- When switching tracks:
    - Update all sliders (ADSR, LFO, Filter) to reflect the new track's state.
    - Update the Sequencer Grid buttons to show the new track's pattern.

## Verification Plan

### Automated Tests
- **Multi-Track Logic Test**: Unit test the `MidiSequencer` to ensure notes on Track 1 don't trigger voices configured for Track 2.
- **State Sync Test**: Verify that switching UI tracks correctly restores the slider positions.

### Manual Verification
- **Workstation Test**: Program a Kick on T1, a Snare on T2, and a Bass on T3. Verify they play together perfectly.
- **Demo Sweep**: Run the new Demo and verify it successfully showcases Morphing and PD visuals.
