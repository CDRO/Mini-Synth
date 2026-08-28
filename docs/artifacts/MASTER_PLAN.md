# Mini-Synth Master Implementation Plan

High-performance Android synthesizer. C++ (Oboe) for audio, Kotlin for UI. Strict adherence to Caveman Rules for technical specs.

> [!CAUTION]
> **STRICT PR REQUIREMENT**: Direct merging to `main` is FORBIDDEN. Every feature MUST follow the 10-cycle review loop defined in [CONTRIBUTING.md](CONTRIBUTING.md).

## Core Architectural Requirements

### Audio & Performance
- **Performance**: C++ (Oboe/AAudio) for low-latency generation.
- **Polyphony**: 16 voices with `tanh` soft-clipping and Unison.
- **Stereo**: True interleaved path with spatial routing.

### Logic & Features
- **Oscillators**: Morphing and Wavetables with Phase Distortion.
- **Sequencer**: 64-step grid with real-time capture and export.
- **Sound Board**: Customizable pad grid with sampling.

---

## Milestone 1-43: Completed core series through Phase Distortion. [DONE]

---

## Milestone 44: UI Polish & Functional Repair [DONE]
## Milestone 45: UI Interaction & Pad Config Repair [DONE]

---

## Milestone 46: Phase Distortion Refinement [DONE]

### [Logic] [Optimization]
- **Performance**: Sine LUT optimization for phase warping.
- **Visuals**: Live Phase Distortion transfer function visualizer.

---

## Milestone 47: Virtual Device Audio & Performance [TODO]

### [Quality] [Performance]
- **Analysis**: Diagnose audio output dropouts and latency in Android Virtual Devices (AVD).
- **Optimization**: Implement a low-overhead audio path and buffer tuning to ensure glitch-free playback in emulated environments.

### [UI] [Fix]
- **Overlaps**: Fix Playmode/BPM and Bank/Hide button overlapping on various screen sizes.
