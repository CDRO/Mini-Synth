# Consolidated Project Status: Mini-Synth

Merging context from initial feature implementation and workspace refinements.

## Current System State

### Audio Engine (C++ / Oboe)
- **Core**: 16-voice polyphonic engine with Sine, Square, Saw, Triangle waveforms.
- **Stability**: Summing mixer (consistent volume), automatic error recovery.
- **Modules**: ADSR Envelopes, Resonant Low-Pass Filter (LPF), LFO.
- **Metronome**: Sample-accurate native tick generator, synced with BPM.
- **Recording**: Real-time MP3 encoding using LAME.
- **MIDI Sequencer**: Supports real-time loop overdub, input quantization, and up to 64 steps. Atomic bitmask implementation for thread safety.

### UI (Kotlin)
- **Aesthetic**: Dark DAW theme.
- **Layout**: Optimized 20/30/50 ratio. perfectly centered Visualizer (20% height).
- **Modes**: Keyboard (with Gestures), Pad (with Bank management), Discovery, and Demo modes.
- **Sequencer UI**: Paginated step view (16 steps per page), with multi-note highlighting.

## Feature Roadmap

### [DONE] Milestone 1-11: Core Synthesis, Sequencer, Mapping, Workspace.
### [DONE] Milestone 12: Sample Persistence.
### [DONE] Milestone 13: Layout Squashing & Onboarding.
### [DONE] Milestone 14: Advanced Export & Sequence Polish.
### [DONE] Milestone 15: Fast JVM-Based UI Testing.
### [DONE] Milestone 16: Keyboard Gestures.
### [DONE] Milestone 17-21: MIDI Support, Effects, Automated Sampling.
### [DONE] Milestone 23: MIDI Loop Recording.
### [DONE] Milestone 24: Polyphonic Aftertouch Simulation.
### [DONE] Milestone 25: FFT Frequency Visualization.
### [DONE] Milestone 26: Professional Demo Experience & Automated Sampling.

### [NEXT] Milestone 27: Adaptive Buffer Management
- **Objective**: Dynamically adjust Oboe buffer size based on CPU load and underrun counts.

### [NEXT] Milestone 28: Integrated Demo & Automated Sampling (Enhanced)
- **Objective**: Complete automated showreel demonstrating all app features with zero-touch sampling.
- **Key Features**:
    - Scripted "Feature Tour" (FX, Sequencer, MIDI).
    - Multi-bank automated sampling demonstration.

## Quality Assurance Status
- **Unit Tests**: 16 local JVM tests passing (Robolectric).
- **Workflow**: GitHub-integrated milestone tracking and 10-issue review loop.
