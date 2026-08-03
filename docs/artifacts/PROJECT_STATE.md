# Consolidated Project Status: Mini-Synth

Merging context from initial feature implementation (`6392750b`) and stability refinements (`29f4297d`).

## Current System State

### Audio Engine (C++ / Oboe)
- **Core**: 16-voice polyphonic engine with Sine, Square, Saw, Triangle waveforms.
- **Stability**: Summing mixer (consistent volume), automatic error recovery with 5-retry limit and 2s cool-down.
- **Modules**: ADSR Envelopes, Resonant Low-Pass Filter (LPF), LFO (Modulating Pitch, Volume, or Filter).
- **Metronome**: Sample-accurate native tick generator, synced with BPM (40-240).
- **Recording**: Real-time MP3 encoding using LAME (asynchronous thread).

### UI (Kotlin)
- **Aesthetic**: Dark DAW theme (Charcoal/Matte Grey/Acid Green).
- **Layout**: Non-scrolling top header with perfectly centered Visualizer (50%) and compact Metronome controls (25% right).
- **Components**: `KeyboardPadView` supports multi-touch, 13-key keyboard, and 4x4 pad grid modes.
- **Feedback**: Robust backlight priority (Touch > Record > Play).
- **Presets**: Jetpack DataStore persistence for all parameters.

## Feature Roadmap

### [DONE] Milestone 1: Core Synthesis & Voice Management
### [DONE] Milestone 2: Resonant Filter & ADSR
### [DONE] Milestone 3: LFO & Parameter Modulation
### [DONE] Milestone 4: Visualization & MP3 Recording
### [DONE] Milestone 5: Metronome & BPM Control
### [DONE] Milestone 6: Visual MIDI Sequencer

### [NEXT] Milestone 7: Keyboard Step-Recording
- **Objective**: Capture keyboard performance into the sequencer step-by-step.
- **Key Features**:
    - Recording Mode toggle.
    - Automatic step advance on key press.
    - Integration with existing `MidiSequencer` grid.

## Quality Assurance Status
- **Unit Tests**: 5 passed (Oscillators, Envelopes, ViewModel logic).
- **Android Tests**: 17 passed (Stress tests for high BPM, polyphony, UI visibility, and lifecycle safety).
- **Workflow**: Mandatory branching, 10-cycle review loop, and detailed walkthroughs established.
