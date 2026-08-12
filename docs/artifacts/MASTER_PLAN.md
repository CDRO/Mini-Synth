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

## Milestone 1-30: Core Synthesis, Effects, & Gestures [DONE]

---

## Milestone 31: Dynamic Grid & Pad Customization [DONE]

### [UI] [Pad Customization]
- **Config Visibility**: Option to hide parameter controls to maximize pad space.
- **Dynamic Grid**: Default 4x4, expandable to 4x8 or 4x16.
- **Color Configuration**: Per-pad color assignment for organization and visual feedback.

---

## Milestone 32: Header Refinement & Engine Status [DONE]

### [UI] [Header]
- **Status Dashboard**: Unified MIDI and Latency display.
- **Modularization**: Refactor header XML into reusable components using `include`.

## Milestone 33: Design System & Accessibility [DONE]

### [UI] [Standardization]
- **Localization**: Zero hardcoded strings in layout files.
- **Scaling**: Responsive text and container definitions to handle diverse aspect ratios.

## Milestone 34: Pad UX & Audio Stability Refinement [DONE]

### [Logic] [Performance]
- **Mixer Normalization**: Implement soft-clipping in the audio thread to handle polyphonic peaks.
- **Config Mode**: Discrete toggle for pad configuration to allow long-press sustaining.

---

## Milestone 35: Performance Visualization & FFT Polish [DONE]

### [UI] [Visualization]
- **High-Resolution FFT**: Implement a log-scaled frequency analyzer for more accurate bass representation.
- **Peak Tracking**: Visual markers for peak amplitudes across the frequency spectrum.

---

## Milestone 36: UI Stability & Visualizer Restoration [TODO]

### [UI] [Stability]
- **Layout Repair**: Fix clipping in the top-right header controls across diverse aspect ratios.
- **Visual Restoration**: Re-align FFT color gradients to restore high-amplitude red alerts.

## Milestone 37: Pad Configuration & Interaction [TODO]

### [UI] [UX]
- **Pad EDIT Mode**: Decouple pad configuration from musical performance via a dedicated toggle.
- **Sustain Support**: Allow long-press note sustaining on pads without interrupting performance.

## Milestone 38: Sequencer Logic & Educational Onboarding [TODO]

### [Logic] [Training]
- **Recording Fix**: Audit and repair the sequencer real-time recording path.
- **Guided Onboarding**: Upgrade the integrated demo to include an auto-scrolling training sequence for loop management.

---

## Future Features & Roadmap
