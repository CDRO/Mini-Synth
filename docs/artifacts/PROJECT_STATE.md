# Consolidated Project Status: Mini-Synth

Merging context from initial feature implementation and workspace refinements.

## Current System State

### Audio Engine (C++ / Oboe)
- **Core**: 16-voice polyphonic engine with Sine, Square, Saw, Triangle waveforms.
- **Stability**: Summing mixer (consistent volume), automatic error recovery.
- **Modules**: ADSR Envelopes, Resonant Low-Pass Filter (LPF), LFO.
- **Metronome**: Sample-accurate native tick generator, synced with BPM.
- **Recording**: Real-time MP3 encoding using LAME.
- **Persistence**: Milestone 12 completed. Recorded samples are persisted using versioned binary headers.

### UI (Kotlin)
- **Aesthetic**: Dark DAW theme.
- **Layout**: Milestone 13 completed. Optimized 20/30/50 ratio. perfectly centered Visualizer (20% height) and compact Metronome.
- **Modes**: 
    - Keyboard mode with 'Hold' gesture (Slide up > 50%).
    - Pad mode with fullscreen toggle and sampling logic.
    - Discovery mode (Help dialogs on click).
    - Demo mode (Predefined song playback).

## Feature Roadmap

### [DONE] Milestone 1-11: Core Synthesis, Sequencer, Mapping, Workspace.
### [DONE] Milestone 12: Sample Persistence.
### [DONE] Milestone 13: Layout Squashing & Onboarding.
### [DONE] Milestone 14: Advanced Export & Sequence Polish.
### [DONE] Milestone 15: Fast JVM-Based UI Testing with Robolectric.
### [DONE] Milestone 16: Pitch Bend and Modulation Gestures.

### [DONE] Milestone 17: Native Unit Testing.
### [DONE] Milestone 18: Project & Set Management.

### [NEXT] Milestone 19: MIDI Device Support
- **Objective**: Integrate USB and Bluetooth MIDI device support via Android MIDI API.
- **Key Features**:
    - USB-MIDI plug-and-play.
    - Bluetooth LE MIDI discovery.
    - Low-latency JNI note routing.

### [NEXT] Milestone 20: Built-in Effects
- **Objective**: Implement Delay and Reverb DSP modules in the C++ engine.
- **Key Features**:
    - Stereo Delay with feedback and sync.
    - Convolution or Algorithmic Reverb.
    - Per-channel dry/wet controls.

## Quality Assurance Status
- **Unit Tests**: 12 passed (Oscillators, Envelopes, Filter Stability, Binary Persistence).
- **Android Tests**: 17 passed (Stress tests for high BPM, UI transitions).
- **Workflow**: GitHub-integrated milestone tracking and 10-issue review loop.
