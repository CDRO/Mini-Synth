# Implementation Plan - Milestone 3: Visualization & Recording

Implement real-time waveform visualization (oscilloscope) and high-quality MP3 recording. This milestone integrates native audio tapping with Kotlin UI rendering and LAME encoding.

## User Review Required

> [!IMPORTANT]
> **LAME Source Integration**: The MP3 export requires the LAME library. I will configure the build system to expect LAME sources in `app/src/main/cpp/lame`. You will need to ensure the library files are present if they are not already in the project.
> **Permissions**: Recording to storage will require `WRITE_EXTERNAL_STORAGE` (for older APIs) or scoped storage handling on modern Android. I will use the app's internal or media-specific folders to avoid permission friction.

## Proposed Changes

### [Audio Engine] (C++)

#### [NEW] [LockFreeQueue.h](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/LockFreeQueue.h)
- Simple lock-free single-producer single-consumer queue for passing samples from the high-priority audio thread to the worker/UI threads.

#### [MODIFY] [AudioEngine.h](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.h)
- Add `LockFreeQueue<float> mVizQueue` (size ~4096).
- Add `LockFreeQueue<float> mRecordQueue` (size ~65536).
- Add methods: `getVisualizerData(float* buffer, int32_t size)`, `startRecording()`, `stopRecording()`.

#### [MODIFY] [AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- In `onAudioReady`: Push master samples into `mVizQueue` and `mRecordQueue` (if recording).

#### [NEW] [Mp3Encoder.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/Mp3Encoder.h)
- Wrapper around `libmp3lame`.
- Handles PCM (float) to MP3 conversion in a background thread.

#### [MODIFY] [native-lib.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/native-lib.cpp)
- Add JNI bindings for the new recording and visualization methods.

---

### [UI & Kotlin]

#### [NEW] [VisualizerView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/VisualizerView.kt)
- Custom View using `Canvas` to draw a live oscilloscope waveform.
- Uses a `Choreographer.FrameCallback` or `ScheduledExecutor` to poll data from the engine at 60fps.

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
- Add `VisualizerView` between the control bars and the keyboard.

#### [MODIFY] [SynthManager.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/audio/SynthManager.kt)
- Add external methods for visualizer data and recording control.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Bind the "Rec" button to the real recording engine.
- Update UI states during recording (backlights).

---

## Verification Plan

### Automated Tests
- **Unit Test (C++)**: `LockFreeQueueTest` to verify no data loss or locks.
- **Instrumented Test (Kotlin)**: `VisualizerDataTest` to ensure `SynthManager.getVisualizerData` returns valid waveform data during playback.

### Manual Verification
- **Visualizer Check**: Play different waveforms (Sine, Square) and verify the visualizer shows the correct shapes.
- **MP3 Export**: Record 10 seconds, locate the file, and verify it plays in an external media player with no artifacts.
