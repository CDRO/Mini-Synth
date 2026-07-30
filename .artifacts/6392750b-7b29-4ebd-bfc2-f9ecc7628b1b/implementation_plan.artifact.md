# Mini-Synth Implementation Plan (V2)

Build low-latency polyphonic/monophonic synthesizer. Use C++ for audio, Kotlin for UI. Apply Caveman Rules to technical specs for maximum clarity.

## User Review Required
> [!IMPORTANT]
> **Performance Architecture**:
> - Audio Engine: **C++ (Oboe)**. AAudio/OpenSL ES wrapper.
> - Polyphony: C++ Voice Manager. Real-time mixing.
> - JNI: Minimal overhead bridge.

> [!WARNING]
> **NDK Setup**: requires CMake and Android NDK installed. Build time increases.

## Development & Review Workflow
All feature development must follow this strict pipeline:
1. **Branching**: New git branch for each step (e.g., `feature/oboe-setup`).
    - **Constraint**: Only one feature in development at a time.
2. **Implementation**: Code and test changes.
3. **Automated Testing**:
    - **Unit Tests**: Required for algorithms, C++ math, and business logic.
    - **Functional Tests**: Required for UI interactions and integration.
    - **Quality Gate**: Next feature starts ONLY when all tests pass. If new code breaks existing tests, must fix both before merge.
4. **Commit**: Meaningful messages. Author: `Gemini <gemini@google.com>`.
5. **Integration**: Push to GitHub and initiate a Merge Request (Pull Request).
    - **Tooling**: Use GitHub CLI (`gh`).
    - **Note**: `gh` is located at `C:\Program Files\GitHub CLI\gh.exe`.
6. **Review Phase 1**:
    - Create **two self-reviews** on the code.
    - Apply fixes, commit, and push updates.
7. **Review Phase 2**:
    - Create **two additional self-reviews**.
    - Apply final changes, commit, and push.
8. **Merge**: **Squash and Merge** into `main`.
    - Message: Meaningful summary of changes.
    - Author: `Gemini <gemini@google.com>`.

## Technical Specifications (Caveman Mode)

### [Core] [AudioEngine]
- **API**: Oboe (C++).
- **Latency**: < 10ms target.
- **SampleRate**: 48kHz (device optimal).
- **Format**: Float32.
- **Polyphony**: Configurable `isPolyphonic` flag.
- **Voices**: `VoiceManager` handles 16 voices.
- **Mixing**: Additive mixing. Normalize output.

### [Logic] [Oscillator]
- **Waveforms**: Sine, Square, Saw, Triangle.
- **Math**:
    - Sine: `sin(phase)`
    - Square: `phase < PI ? 1 : -1`
    - Saw: `(phase / PI) - 1`
    - Triangle: `2 * abs((phase / PI) - 1) - 1`
- **Control**: `trigger(midi, velocity)`, `release(midi)`.

### [Logic] [Range]
- **Octave Shift**: Internal logic supports +/- 4 octaves.
- **Calculation**: `effective_midi = keyboard_midi + (octave_shift * 12)`.

### [Interface] [JNI Bridge]
- **JNI Methods**: `startAudio()`, `stopAudio()`, `setNote(int midi, float velocity)`, `releaseNote(int midi)`, `setPolyphony(boolean)`, `setOctaveShift(int)`.
- **Threading**: Audio thread real-time priority. No locks/allocations.

### [UI] [SynthesizerLayout]
- **Orientation**: Horizontal (Landscape).
- **View**: Custom Keyboard View (Kotlin).
- **Keyboard**: Fixed 1-octave range (13 keys: C to C).
- **Sound Board Mode**: Toggle to 4x4 Grid (16 pads). Pads trigger configured notes.
- **Backlighting**: Visual feedback for keys/pads.
    - `Yellow`: User touch.
    - `Red`: Recording active on note.
    - `Blue`: Playback active on note.
- **Controls**: Mode Toggle (Mono/Poly), Input Toggle (Keys/Pads), Waveform Selector, Octave Shift (+/-), Master Volume.

## Proposed Changes

### [Audio Engine (C++)]
#### [NEW] [native-lib.cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/native-lib.cpp)
Main JNI entry point.

#### [NEW] [AudioEngine.cpp/h](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
Oboe stream management.

#### [NEW] [VoiceManager.cpp/h](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/VoiceManager.cpp)
16-voice mixing logic. Mono/Poly toggle.

#### [NEW] [Oscillator.cpp/h](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/Oscillator.cpp)
Waveform generation math.

### [Android Build]
#### [MODIFY] [build.gradle.kts](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/build.gradle.kts)
Enable NDK, CMake, and Oboe dependency.

#### [NEW] [CMakeLists.txt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/CMakeLists.txt)
Build script for native code.

### [UI (Kotlin)]
#### [NEW] [SynthManager.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/audio/SynthManager.kt)
Kotlin wrapper for JNI.

#### [NEW] [KeyboardPadView.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt)
Unified view for Keyboard and Sound Board with backlighting logic.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
Lock orientation to landscape. Connect UI to `SynthManager`.

## Verification Plan
### Automated
- C++ Unit Tests (GTest) for oscillator math and mixing.
- JNI connectivity check.

### Manual
- Latency measurement (audio loopback).
- Polyphony stress test (16 voices active).
- Toggle check: Keys vs Pads.
- Backlight verification: Touch vs Playback vs Recording.
