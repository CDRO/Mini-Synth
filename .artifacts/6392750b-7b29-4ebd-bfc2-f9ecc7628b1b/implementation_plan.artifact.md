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
7. **Review Rounds (1-5)**:
    - 5 separate comments on GitHub PR via `gh pr comment`.
    - Fixes and re-commits between rounds.
8. **Merge**: Squash and Merge via `gh pr merge`. Author: `Gemini <gemini@google.com>`.

---

## Current Feature: Virtual Device Fix & Dark Theme Implementation

### [Infrastructure] [Compatibility]
- **Issue**: `minSdk 36` prevents installation on common virtual devices.
- **Fix**: Lower `minSdk` to 28 (Android 9.0). Maintain `targetSdk` at latest stable.

### [UI] [Dark Theme]
- **Colors**: Implement palette from [design_guide.artifact.md](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/.artifacts/6392750b-7b29-4ebd-bfc2-f9ecc7628b1b/design_guide.artifact.md).
- **Styles**: Update `themes.xml` for full dark mode.
- **Custom View**: Update `KeyboardPadView` paints (Key colors, Acid Green touch feedback).

## Proposed Changes

### [Android Build]
#### [MODIFY] [build.gradle.kts](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/build.gradle.kts)
Lower `minSdk` to 28. Set `compileSdk` and `targetSdk` to 35 (latest stable).

### [Resources]
#### [MODIFY] [colors.xml](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/res/values/colors.xml)
Define FL Studio palette.

#### [MODIFY] [themes.xml](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/res/values/themes.xml)
Apply Dark theme.

### [UI Code]
#### [MODIFY] [KeyboardPadView.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt)
Update paints with new HEX codes. Change Touch backlight to Acid Green.

## Verification Plan
### Automated
- Instrumented Test: Verify app starts on API 28+ emulator.
- UI Test: Verify component visibility in dark mode.

### Manual
- Visual check: Does it look like FL Studio?
- Multi-touch verification on virtual device.
