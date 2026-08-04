# Implementation Plan - Milestone 12: Sample Persistence

Ensuring user creativity is preserved by persisting all recorded and mapped pad samples to the device's internal storage.

## User Review Required

> [!IMPORTANT]
> - All pad samples will be saved as raw binary PCM files in the app's private data directory.
> - A new `samples.json` (or extension to `SynthPreset`) will track which file is mapped to which pad.
> - On startup, the `AudioEngine` will automatically load these files into the native buffers.
> - This may increase app startup time slightly depending on the number of samples.

## Proposed Changes

### Native Engine (C++)

#### [MODIFY] [AudioEngine.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- Add JNI method `savePadSample(int padIndex, String path)`.
- Add JNI method `loadPadSample(int padIndex, String path)`.
- Use standard C++ `fstream` to write/read the `mPadBuffers[padIndex]` vector to disk.

### UI & Logic (Kotlin)

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- After a pad recording is finalized, automatically save it to a unique file (e.g., `pad_0.bin`).
- On app `onStart`, iterate through the samples directory and load them into the native engine.

#### [MODIFY] [SynthPreset.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/audio/SynthPreset.kt)
- Add a mapping of `padIndex` to `samplePath`.

## Verification Plan

### Automated Tests
- **Unit Test**: Verify that saving a buffer and reloading it results in identical PCM data.
- **Instrumented Test**: Verify that samples persist across app process death.

### Manual Verification
- Record a sample to Pad 0.
- Kill the app from the task switcher.
- Restart the app.
- Press Pad 0 and verify the sound is restored.
