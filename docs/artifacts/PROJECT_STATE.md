# Consolidated Project Status: Mini-Synth

## Current System State

### Audio Engine (C++ / Oboe)
- **Core**: 16-voice polyphonic engine with Sine, Square, Saw, Triangle waveforms.
- **Stability**: Summing mixer with soft-clipping, automatic error recovery.
- **Modules**: ADSR Envelopes, Resonant Low-Pass Filter (LPF), LFO.
- **Metronome**: Sample-accurate native tick generator, synced with BPM.
- **Recording**: Real-time MP3 encoding using LAME.
- **MIDI Sequencer**: Supports up to 64 steps. Atomic bitmask implementation.

### UI (Kotlin)
- **Aesthetic**: Dark DAW theme.
- **Modes**: Keyboard (with Gestures), Pad (with Bank management), Discovery, and Demo modes.
- **Sequencer UI**: Paginated step view (16 steps per page).

## Feature Roadmap

### [DONE] Milestone 1-35: Core Synthesis, Visualization & FFT.

### [NEXT] Milestone 36: Pad Configuration Refinement
- **Objective**: Fix pad sustaining and separate config from performance.
- **Key Features**:
    - "EDIT" mode toggle for Pad grid.
    - Long-press sustain support for pads.

### [TODO] Milestone 37: UI Stability & Sequencer Repair
- **Objective**: Fix header clipping and visual regressions.
- **Key Features**:
    - Layout weighting for header buttons.
    - Repaired sequencer recording path.

### [TODO] Milestone 38: Educational Demo & Sequencer Training
- **Objective**: Fix sequencer recording and guide the user.
- **Key Features**:
    - Auto-scrolling educational demo.

### [TODO] Milestone 39: Recording Export & Sample Sharing

## Quality Assurance Status
- **Unit Tests**: 27 local JVM tests passing (Robolectric).
- **Android Tests**: 23 connected tests passing.
- **Workflow**: GitHub-integrated milestone tracking and 10-issue review loop.
