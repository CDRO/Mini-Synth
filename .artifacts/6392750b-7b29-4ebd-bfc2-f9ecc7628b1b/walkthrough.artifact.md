# Walkthrough: Oboe Setup & Native Engine

Completed the initial infrastructure for the mini synthesizer, integrating the Oboe audio library and setting up the JNI bridge.

## Changes Made

### Infrastructure
- Updated [libs.versions.toml](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/gradle/libs.versions.toml) to include **Oboe 1.10.0**.
- Modified [build.gradle.kts](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/build.gradle.kts) to enable **Prefab**, **CMake**, and add the native dependency.
- Created [CMakeLists.txt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/CMakeLists.txt) to link Oboe and build the `mini_synth` library.

### Native Audio Engine
- Implemented [AudioEngine.h/cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp) with low-latency stream initialization and a silence-generating callback.
- Added JNI bridge in [native-lib.cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/native-lib.cpp) with thread-safe engine management using `std::unique_ptr` and `std::mutex`.

### Kotlin Integration
- Created [SynthManager.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/audio/SynthManager.kt) to expose `startEngine()` and `stopEngine()` to the Android app.

## Engineering Reviews Applied

### Phase 1
- **Optimization**: Replaced manual silence loop with `std::fill`.
- **Safety**: Switched to `std::unique_ptr` and `std::lock_guard` for native engine lifecycle.

### Phase 2
- **Robustness**: Added error checking for `openStream` and `requestStart`.
- **State Guard**: Prevented redundant stream creation if a stream is already active.

## Verification Results
- **Build**: Successfully compiled for all ABIs (`assembleDebug`).
- **Oboe Integration**: Verified linking via CMake.

> [!TIP]
> Use a physical device for future testing to experience the low-latency benefits of Oboe. Emulators will have significant audio lag.
