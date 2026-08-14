# Mini-Synth Master Implementation Plan

High-performance Android synthesizer. C++ (Oboe) for audio, Kotlin for UI. Strict adherence to Caveman Rules for technical specs.

> [!CAUTION]
> **STRICT PR REQUIREMENT**: Direct merging to `main` is FORBIDDEN. Every feature MUST follow the 10-cycle review loop defined in [CONTRIBUTING.md](CONTRIBUTING.md).

## Core Architectural Requirements

### Audio & Performance
- **Performance**: C++ (Oboe/AAudio) for sound generation. Low latency < 10ms.
- **Polyphony**: 16 voices. Additive mixing with `tanh` soft-clipping.
- **Stereo**: True interleaved stereo path.

### Logic & Features
- **Oscillators**: Geometric waveforms with **Waveform Morphing** and **Wavetables**.
- **Keyboard**: 13-key range with ±4 octave shift and gestures.
- **Sound Board**: Customizable pad grid with sampling and spatial panning.

---

## Milestone 1-38: Completed core series. [DONE]

---

## Milestone 39: Stereo Engine & Spatial Routing [DONE]
## Milestone 40: Unison & Voice Layering [DONE]
## Milestone 41: Waveform Morphing & Signed Releases [DONE]

---

## Milestone 42: Help Coverage & Global Localization [DONE]

### [UI] [Help]
- **100% Coverage**: Verified help labels for every knob, button, and slider in the app.
- **Discovery Mode**: Integrated localized help toasts across all synthesis and effects modules.

### [UI] [Localization]
- **Multi-Language**: Infrastructure and baseline translations for 30+ European and East Asian locales.
- **Auto-Selection**: Automated language switching based on device system settings.

---

## Milestone 43: LFO Expansion & Phase Distortion [TODO]

### [Logic] [Synthesis]
- **Phase Distortion**: Casio CZ-style timbre shaping.
- **Custom LFO**: User-definable LFO waveforms.
