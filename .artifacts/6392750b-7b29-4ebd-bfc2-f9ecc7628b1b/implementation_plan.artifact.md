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

## Open Questions
- Specific waveforms needed for MVP? (Sine/Square/Saw/Triangle recommended).
- Preferred number of simultaneous voices for polyphony? (e.g., 8 or 16).
- Keyboard range: Fixed vs. Scrollable?

## Technical Specifications (Caveman Mode)

### [Core] [AudioEngine]
- **API**: Oboe (C++).
- **Latency**: < 10ms target.
- **SampleRate**: 48kHz (device optimal).
- **Format**: Float32.
- **Polyphony**: Configurable `isPolyphonic` flag.
- **Voices**: `VoiceManager` class handles oscillator lifecycle.
- **Mixing**: Additive mixing of active voices. Normalize to avoid clipping.

### [Logic] [Oscillator]
- **Source**: C++ implementation.
- **Math**: Sine (`sin`), Square (`sign`), Saw (`phase`), Triangle (`abs`).
- **Control**: `setFrequency`, `setVolume`, `triggerNote`, `releaseNote`.

### [Interface] [JNI Bridge]
- **JNI Methods**: `startAudio()`, `stopAudio()`, `setNote(int midi, float velocity)`, `releaseNote(int midi)`, `setPolyphony(boolean)`.
- **Threading**: Audio thread must never block. No allocations in callback.

### [UI] [SynthesizerLayout]
- **Orientation**: Horizontal (Landscape).
- **View**: Custom Keyboard View (Kotlin).
- **Controls**: Mode Toggle (Mono/Poly), Waveform Selector, Master Volume.

## Proposed Changes

### [Audio Engine (C++)]
#### [NEW] [native-lib.cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/native-lib.cpp)
Main JNI entry point.

#### [NEW] [AudioEngine.cpp/h](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
Oboe stream management.

#### [NEW] [VoiceManager.cpp/h](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/VoiceManager.cpp)
Poly/Mono logic and mixing.

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
- C++ Unit Tests (GTest) for oscillator math.
- JNI connectivity check.

### Manual
- Latency measurement (audio loopback).
- Polyphony stress test (play many notes).
- Mode toggle verification.
