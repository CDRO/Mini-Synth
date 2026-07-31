# Mini-Synth Master Implementation Plan

High-performance Android synthesizer. C++ (Oboe) for audio, Kotlin for UI. Strict adherence to Caveman Rules for technical specs.

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

## Development & Review Workflow

> [!IMPORTANT]
> **MANDATORY WORKFLOW**: This section must NEVER be removed. Every feature implementation AND bugfix MUST follow these steps without exception.

1. **Branching**: New git branch per feature or bugfix (`feature/*` or `fix/*`). Sequential development only.
2. **Implementation**: Code and test changes.
3. **Artifact Maintenance**:
    - Unique artifacts per feature: `[feature_name]_task.artifact.md`, `[feature_name]_review.artifact.md`, `[feature_name]_walkthrough.artifact.md`.
    - Preserve previous artifacts.
4. **Automated Testing**:
    - **Unit Tests**: Algorithms, math, and business logic (GTest/JUnit).
    - **Functional Tests**: UI interaction and integration (Espresso).
    - **Evidence**: Display test results in conversation.
5. **Commit**: Meaningful messages. Author: `Gemini <gemini@google.com>`.
6. **Integration**: Push and `gh pr create`. (GH CLI: `C:\Program Files\GitHub CLI\gh.exe`).
    - **PR Description Requirements**:
        - **Why**: Explain the reason for the change and the problem it solves.
        - **Tests**: Detail new tests added and the verification logic.
        - **Value**: Explicitly state the additional value expected from this merge.
        - **No File List**: Do not list modified files (VCS handles this).
7. **Merge Message Review Loop**:
    - Draft the merge message.
    - Perform at least **2 iterations** of self-review and adaptation on the message.
    - Ensure each iteration **increases quality and technical value**.
8. **Code Review Cycles (1-10)**:
    - Perform a cycle:
        - Identify **1 self-review** on the code current state.
        - **Requirement Verification**: Explicitly review that the changes correctly and completely implement the requested feature requirements.
        - Post the review as a **comment** on the GitHub PR via `gh pr comment`.
        - Apply fixes to the code based on the review.
        - Commit and **push** changes to the branch.
    - Proceed to the next review cycle only after pushing the fixes.
    - Repeat until **10 total review cycles** are completed.
9. **Merge**: Squash and Merge via `gh pr merge` using the reviewed Merge Message. Author: `Gemini <gemini@google.com>`.

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

## Milestone 2: Preset Management

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

## Future Features & Roadmap

### [UI] [Visualization]
- **Waveform Visualizer**: Real-time oscilloscope-style display of the master output.

### [Logic] [Timing & Sequencing]
- **Metronome**: Audio-visual click track for synchronized recording.
- **BPM Control**: Dedicated buttons to increment/decrement tempo.

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
