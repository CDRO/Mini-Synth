# Mini-Synth Master Implementation Plan

High-performance Android synthesizer. C++ (Oboe) for audio, Kotlin for UI. Strict adherence to Caveman Rules for technical specs.

> [!CAUTION]
> **STRICT PR REQUIREMENT**: Direct merging to `main` is FORBIDDEN. Every feature MUST go through `gh pr create` and follow the 10-cycle review loop defined in [DEVELOPMENT_WORKFLOW.md](guides/DEVELOPMENT_WORKFLOW.md).

## Core Architectural Requirements

### Audio & Performance
- **Performance**: C++ (Oboe/AAudio) for all sound generation and mixing. Low latency target < 10ms.
- **Threading**: Audio thread real-time priority. **No locks/allocations in callback**.
- **Polyphony**: 16 simultaneous voices. Additive mixing. Output normalization.
- **Backends**: Oboe handles fallback between AAudio and OpenSL ES automatically.

### Logic & Features
- **Configurability**: Toggle between Polyphonic and Monophonic modes.
- **Oscillators**: Sine, Square, Saw, Triangle support.
- **Keyboard**: 13-key fixed range (C to C). Support for ±4 octave internal shift.
- **Sound Board**: Toggleable 4x4 pad grid mode.

### Design & UX (FL Studio Aesthetic)
- **Theme**: Dark, high-contrast "Stealth Synth" look.
- **Orientation**: Locked Landscape. Single screen layout.
- **Feedback**: Backlit keys/pads.
    - `Acid Green` (#C0FF00): Touch input.
    - `Electric Blue` (#00A3FF): Playback state.
    - `Vibrant Red` (#FF3B30): Recording state.

## Technical Specifications (Caveman Mode)

### [Logic] [Oscillator]
- **Math**:
    - Sine: `sin(phase)`
    - Square: `phase < PI ? 1 : -1`
    - Saw: `(phase / PI) - 1`
    - Triangle: `2 * abs((phase / PI) - 1) - 1`
- **Control**: `trigger(midi, velocity)`, `release(midi)`.

### [Logic] [Range]
- **Calculation**: `effective_midi = keyboard_midi + (octave_shift * 12)`.
- **Constraint**: Clamp result to valid MIDI range [0, 127].

### [Interface] [JNI Bridge]
- **Bridge**: Minimal overhead. No heavy objects passed.
- **Methods**: `startAudio()`, `stopAudio()`, `setNote()`, `releaseNote()`, `setPolyphony()`, `setWaveform()`, `setOctaveShift()`.

---

## Milestone 1: Resonant Low-Pass Filter [DONE]

### [Logic] [Filter]
- **Type**: 2-pole Resonant Low-Pass Filter.
- **Cutoff**: 20Hz to 20,000Hz (Exponential mapping).
- **Resonance**: 0.0 to 1.0 (Q factor).
- **Implementation**: Per-voice filtering in the audio thread.

### [UI] [FilterControls]
- **Sliders**: Cutoff Frequency and Resonance.
- **Labels**: Show Hz and Q values.

---

## Milestone 2: Preset Management [DONE]

### [Logic] [Presets]
- **Storage**: Jetpack DataStore with JSON serialization.
- **Save**: Capture all current engine parameters (Osc, ADSR, LFO, LPF) and write to `presets.json`.
- **Load**: Read JSON and batch-apply parameters to JNI engine.

---

## Milestone 3: Visualization & Recording [DONE]

### [Logic] [Recording & Viz]
- **Audio Tap**: Implement a thread-safe capturing mechanism in `AudioEngine`.
- **Recording**: Real-time PCM capture to a background thread.
- **Encoding**: Integrate **LAME** (C) for high-quality MP3 encoding via NDK.
- **Visualization**: Expose real-time PCM buffers for UI rendering.

### [UI] [Visualizer]
- **VisualizerView**: Real-time oscilloscope display above the keyboard.

---

## Milestone 4: Metronome & BPM Control [DONE]

### [Logic] [Metronome]
- **Engine**: Sample-accurate native tick generation.
- **BPM**: Dynamic control from 40 to 240 BPM.
- **Visuals**: Synced beat indicator LED in the control bar.

---

## Milestone 17: Native Unit Testing [DONE]
## Milestone 18: Project & Set Management [DONE]
## Milestone 19: MIDI Device Support [DONE]

## Milestone 20: Built-in Effects [DONE]
## Milestone 21: Automated Demo & Sampling [DONE]
## Milestone 23: MIDI Loop Recording [DONE]
## Milestone 24: Polyphonic Aftertouch Simulation [DONE]
## Milestone 25: FFT Frequency Visualization [DONE]
## Milestone 26: Professional Demo Experience & Automated Sampling [DONE]

---

## Milestone 27: Adaptive Buffer Management [DONE]

### [Logic] [Performance]
- **Buffer Scaling**: Dynamically adjust Oboe's `bufferSize` based on xRun counts.
- **Latency Balancing**: Minimize latency while preventing crackling under load.

## Milestone 28: Integrated Demo & Automated Sampling (Enhanced) [DONE]

### [Logic] [UX]
- **Automated Tour**: Programmatic sequence covering Oscillators, Filters, FX, and Sequencer.
- **Self-Sampling**: Demonstrate automated note-to-pad sampling logic.
- **Discovery Mode**: Integrated help overlays triggered by the demo script.

---

## Milestone 29: Keyboard Sample Creation [DONE]

### [Logic] [Sequencing]
- **Melody Recording**: Step-by-step entry for keyboard performance.
- **Quantization**: Alignment of notes to specified grid (1/16, 1/8, etc.).
- **Looping**: Persistent playback of recorded melody loops.

---

## Milestone 30: Pad Holding & Expressive Gestures [DONE]

### [Logic] [Performance]
- **Sustain Gestures**: Swipe between pads to maintain note duration.
- **Modulation**: Integration of vertical/horizontal gestures for real-time pad expressive control.

---

## Milestone 31: Panel Reorganization & Hierarchy [DONE]

### [UI] [Hierarchy]
- **Overflow Menus**: Logical grouping of secondary sequencer actions to prevent horizontal clipping.
- **Vertical Stacking**: Redesign of the Pad Customization section for better readability.

## Milestone 32: Header Refinement & Engine Status [DONE]

### [UI] [Header]
- **Status Dashboard**: Unified MIDI and Latency display.
- **Modularization**: Refactor header XML into reusable components using `include`.

## Milestone 33: Design System & Accessibility [DONE]

### [UI] [Standardization]
- **Localization**: Zero hardcoded strings in layout files.
- **Scaling**: Responsive text and container definitions to handle diverse aspect ratios.

---

## Milestone 34: Pad UX & Audio Stability Refinement [TODO]

### [Logic] [Performance]
- **Mixer Normalization**: Implement soft-clipping in the audio thread to handle polyphonic peaks.
- **Config Mode**: Discrete toggle for pad configuration to allow long-press sustaining.

---

## Future Features & Roadmap

### [Feature] [Sampling & Sequencing]
- **Keyboard Sample Creation**:
    - Select step duration (1/16 to 1/1 notes).
    - Step-by-step recording of melodies.
    - Loop playback of recorded sequences.
- **Pad Sampling**:
    - Capture keyboard performance directly to a pad.
    - **Pad Holding**: Swipe from one pad to another in sampling mode to define note duration (hold).
- **Sample Mapping**:
    - Additional column on the left of the pad grid for mapping external or recorded samples.

### [UI] [Pad Customization]
- **Dynamic Grid**: Default 4x4, expandable to 16 columns. Scrollable interface for large grids.
- **Config Visibility**: Option to hide parameter controls to maximize pad space.
- **Color Configuration**: Per-pad color assignment for organization and visual feedback.
