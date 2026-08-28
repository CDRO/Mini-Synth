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

## Milestone 46: Phase Distortion Refinement [TODO]

### [Logic] [Optimization]
- **Math**: Optimize PD mapping with lookup tables or SIMD.
- **Visuals**: Add a "Warped Phase" visualization to the top header.
