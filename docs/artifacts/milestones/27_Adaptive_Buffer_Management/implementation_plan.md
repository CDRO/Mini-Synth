# Implementation Plan - Milestone 27: Adaptive Buffer Management

Dynamically adjust the audio engine's buffer size to balance between low latency and audio stability (no crackling).

## User Review Required

> [!IMPORTANT]
> **Adaptive Logic**: The engine will monitor xRun (underrun) counts. If underruns occur, the buffer size will be increased by one burst size. If the engine remains stable for a significant period (e.g., 5 seconds), it will attempt to decrease the buffer size to minimize latency.

## Proposed Changes

### [Audio Engine]

#### [MODIFY] [AudioEngine.h](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.h)
- Add tracking for xRun counts.
- Add methods to get/set buffer size in frames.

#### [MODIFY] [AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- Implement `getBufferStatus` to retrieve current xRun count from Oboe.
- Add a periodic check (every few seconds) to evaluate if buffer size can be optimized.
- Use `mStream->setBufferSizeInFrames()` dynamically.

### [JNI & UI]

#### [MODIFY] [native-lib.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/native-lib.cpp)
- Expose buffer status (size, xRuns) to JNI.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Add a "Latency Status" indicator showing current buffer size and underruns.

## Verification Plan

### Automated Tests
- **Native Tests**: Mock Oboe stream and verify that `setBufferSizeInFrames` is called when simulated xRuns occur.

### Manual Verification
- Run the app on a high-load scenario (many voices + FX).
- Observe if the buffer size increases automatically when audio crackling starts.
- Verify that buffer size recovers to a smaller value after the load decreases.
