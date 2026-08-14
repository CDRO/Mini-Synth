# Mini-Synth Master Implementation Plan

High-performance Android synthesizer. C++ (Oboe) for audio, Kotlin for UI. Strict adherence to Caveman Rules for technical specs.

> [!CAUTION]
> **STRICT PR REQUIREMENT**: Direct merging to `main` is FORBIDDEN. Every feature MUST go through `gh pr create` and follow the 10-cycle review loop defined in [DEVELOPMENT_WORKFLOW.md](guides/DEVELOPMENT_WORKFLOW.md).

## Core Architectural Requirements

### Audio & Performance
- **Performance**: C++ (Oboe/AAudio) for all sound generation and mixing. Low latency target < 10ms.
- **Polyphony**: 16 simultaneous voices. Additive mixing with `tanh` soft-clipping.
- **Stereo**: True interleaved stereo path with Equal Power Panning.

### Logic & Features
- **Configurability**: Toggle between Polyphonic and Monophonic modes.
- **Oscillators**: Sine, Square, Saw, Triangle support.
- **Keyboard**: 13-key fixed range (C to C). ±4 octave internal shift.
- **Sound Board**: Customizable pad grid (up to 16x16) with Bank management.

### Design & UX (FL Studio Aesthetic)
- **Theme**: Dark, high-contrast "Stealth Synth" look.
- **Feedback**: Backlit keys/pads for Touch, Playback, and Recording.

---

## Milestone 1-38: Completed core features, effects, visualizers, and onboarding. [DONE]

---

## Milestone 39: Stereo Engine & Spatial Routing [DONE]

### [Logic] [Audio]
- **Stereo Migration**: Transitioned the Oboe stream and mixer to 2-channel output.
- **Panning**: Implemented Equal Power Panning per voice and pad.
- **Stereo FX**: Expanded Delay and Reverb to support stereo-width processing.

---

## Milestone 40: Unison & Voice Layering [DONE]

### [Logic] [Synthesis]
- **Unison Stacking**: Modified the Voice architecture to support multiple sub-oscillators per note.
- **Detune**: Added a "Detune" parameter to offset the pitch of unison voices.
- **Stereo Width**: Distributed unison voices across the stereo field automatically.

---

## Milestone 41: Waveform Morphing & Wavetables [TODO]

### [Logic] [Synthesis]
- **Waveform Morphing**: Smooth blending between basic waveforms using a dedicated morph parameter.
- **Wavetable Engine**: Implement support for high-resolution wavetables for complex timbres.

---

## Future Features & Roadmap

### [Feature] [Synthesis]
- **Waveform Morphing**: Smooth blending between basic waveforms.
- **Custom Oscillators**: Wavetable support for more complex timbres.

### [Feature] [Sampling & Sequencing]
- **Sample Mapping**: Dedicated UI for mapping external or recorded samples to pads.
- **Session Management**: Export full project bundles (audio + patterns).
