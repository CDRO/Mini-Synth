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
1. **Branching**: New git branch per feature. Sequential development only.
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
7. **Review Cycles (1-5)**:
    - Perform a cycle:
        - Identify **2 self-reviews** on the current state.
        - Post each as a **separate comment** on the GitHub PR via `gh pr comment`.
        - Apply fixes and commit changes.
    - Repeat the cycle **4 more times** (Total 5 cycles, 10 reviews).
8. **Merge**: Squash and Merge via `gh pr merge`. Author: `Gemini <gemini@google.com>`.

---

## Current Feature: ADSR Envelope & visual sequencers

### [Logic] [Envelope]
- **Type**: ADSR (Attack, Decay, Sustain, Release).
- **Control**: 4 parameters (0.0 to 1.0).
- **Math**: Multiplicative gain on voice output.
- **State**: `Idle`, `Attack`, `Decay`, `Sustain`, `Release`.
- **Slopes**: Linear ramps.

### [UI] [ModulationControls]
- **Controls**: 4 Sliders/Knobs for ADSR.
- **Theme**: Stealth dark aesthetic.

## Proposed Changes

### [Audio Engine (C++)]
#### [NEW] [Envelope.h/cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/Envelope.cpp)
Linear ramp logic for ADSR stages.

#### [MODIFY] [Voice.h/cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/Voice.cpp)
Integrate Envelope into sample generation.

#### [MODIFY] [VoiceManager.h/cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/VoiceManager.cpp)
Expose ADSR parameters.

### [UI (Kotlin)]
#### [MODIFY] [content_main.xml](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
Add ADSR control group.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
Bind ADSR UI to native engine.

## Verification Plan
### Automated
- **Unit Test (C++)**: Verify Envelope stage transitions (Idle -> Attack -> Decay -> Sustain -> Release -> Idle).
- **Unit Test (Kotlin)**: Verify JNI bridge for ADSR parameters.
- **Instrumented Test**: Verify slider interaction updates engine parameters.

### Manual
- Auditory check: Sustain and Release behavior.
- UI check: Slider responsiveness.
