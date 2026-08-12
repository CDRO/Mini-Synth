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

## Milestone 1-35: Completed core features, effects, and visualization. [DONE]

---

## Milestone 36: UI Stability, Pad UX & Sequencer Repair [DONE]

### [UI] [Stability]
- **Layout Repair**: Fix clipping in the top-right header controls across diverse aspect ratios.
- **Visual Restoration**: Re-align FFT color gradients to restore high-amplitude red alerts.

### [UI] [UX]
- **Pad EDIT Mode**: Decouple pad configuration from musical performance via a dedicated toggle.
- **Sustain Support**: Allow long-press note sustaining on pads without interrupting performance.

### [Logic] [Training]
- **Recording Fix**: Audit and repair the sequencer real-time recording path.
- **Guided Onboarding**: Upgrade the integrated demo to include an auto-scrolling training sequence for loop management.

---

## Milestone 37: Recording Export & Sample Sharing [TODO]

### [Logic] [Export]
- **Offline Renderer**: Implement a dedicated path for non-real-time high-speed WAV/MP3 generation.
- **Share Intent**: Deep integration with Android system sharing for exported files.

---

## Future Features & Roadmap
