# Mini-Synth Master Implementation Plan

High-performance Android synthesizer. C++ (Oboe) for audio, Kotlin for UI. Strict adherence to Caveman Rules for technical specs.

> [!CAUTION]
> **STRICT PR REQUIREMENT**: Direct merging to `main` is FORBIDDEN. Every feature MUST go through `gh pr create` and follow the 10-cycle review loop defined in [DEVELOPMENT_WORKFLOW.md](guides/DEVELOPMENT_WORKFLOW.md).

## Core Architectural Requirements

### Audio & Performance
- **Performance**: C++ (Oboe/AAudio) for all sound generation and mixing. Low latency target < 10ms.
- **Polyphony**: 16 simultaneous voices. Additive mixing with `tanh` soft-clipping.
- **Backends**: Oboe handles fallback between AAudio and OpenSL ES automatically.

### Logic & Features
- **Configurability**: Toggle between Polyphonic and Monophonic modes.
- **Oscillators**: Sine, Square, Saw, Triangle support.
- **Keyboard**: 13-key fixed range (C to C). ±4 octave internal shift.
- **Sound Board**: Customizable pad grid (up to 16x16) with Bank management.

### Design & UX (FL Studio Aesthetic)
- **Theme**: Dark, high-contrast "Stealth Synth" look.
- **Feedback**: Backlit keys/pads for Touch, Playback, and Recording.

---

## Milestone 1-35: Core synthesis, effects, and visualizers. [DONE]

---

## Milestone 36: UI Stability, Pad UX & Sequencer Repair [DONE]
## Milestone 37: Recording Export & Sample Sharing [DONE]
## Milestone 38: Educational Demo & Sequencer Training [DONE]

---

## Milestone 39: Stereo Engine & Spatial Routing [TODO]

### [Logic] [Audio]
- **Stereo Migration**: Transition the Oboe stream and mixer to 2-channel output.
- **Panning**: Implement a panning parameter per voice and pad.
- **Stereo FX**: Update Delay and Reverb to support stereo-width processing.

---

## Future Features & Roadmap

### [Feature] [Synthesis]
- **Unison & Detune**: Layering voices with pitch offsets for thicker sound.
- **Waveform Morphing**: Smooth blending between basic waveforms.

### [Feature] [Sampling & Sequencing]
- **Sample Mapping**: Dedicated UI for mapping external or recorded samples to pads.
- **Session Management**: Export full project bundles (audio + patterns).
