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
- **Controls**: Mode Toggle (Mono/Poly), Waveform Selector, Octave Shift (+/-), Master Volume.

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

#### [MODIFY] [MainActivity.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
Lock orientation to landscape. Connect UI to `SynthManager`.

## Verification Plan
### Automated
- C++ Unit Tests (GTest) for oscillator math and mixing.
- JNI connectivity check.

### Manual
- Latency measurement (audio loopback).
- Polyphony stress test (16 voices active).
- Octave shift verification.
