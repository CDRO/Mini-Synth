# Mini-Synth Master Implementation Plan

High-performance Android multi-track workstation. C++ (Oboe) for audio, Kotlin for UI.

## Core Architectural Requirements

### Audio & Performance
- **Performance**: C++ (Oboe/AAudio) with static Sine LUT and soft-clipping.
- **Architecture**: 4 independent synth tracks sharing 16 polyphonic voices.
- **Stereo**: True interleaved path with per-track panning.

### Logic & Features
- **Oscillators**: Morphing, Wavetables, and Phase Distortion.
- **Sequencer**: 4x64-step grid with real-time multi-track capture.
- **Sound Board**: Customizable pad grid with sampling (Track 0).

---

## Milestone 1-47: Core series through AVD Stability. [DONE]

---

## Milestone 48: Multi-Track Workstation & Demo Update [DONE]

### [Engine] [Workstation]
- **Tracks**: Implemented 4-track management in C++ and JNI.
- **Sequencer**: Expanded to support independent patterns per track.
- **Demo**: Modernized walkthrough showcasing Morphing, PD, and Wavetables.

---

## Milestone 49: Advanced Modulation Matrix [TODO]

### [Logic] [Modulation]
- **LFO Sync**: Synchronize LFO rates to global BPM (1/4, 1/8, etc.).
- **Matrix**: Allow routing LFOs to multiple targets simultaneously.
