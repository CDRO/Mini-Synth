# Task: Visualization & Recording

Implementing real-time oscilloscope visualization and MP3 recording using a lock-free native tap and LAME encoding.

## Checklist

- `[x]` **Native Infrastructure (C++)**
    - `[x]` Implement `LockFreeQueue.h` (SPSC queue)
    - `[x]` Update `AudioEngine.h/cpp` to tap output samples
    - `[x]` Implement `Mp3Encoder.h/cpp` (LAME wrapper)
    - `[x]` Update `CMakeLists.txt` for LAME integration
    - `[x]` Update `native-lib.cpp` (JNI bindings for Viz/Rec)
- `[x]` **UI & Kotlin Integration**
    - `[x]` Update `SynthManager.kt` JNI bridge
    - `[x]` Implement `VisualizerView.kt` (Oscilloscope drawing)
    - `[x]` Update `content_main.xml` with `VisualizerView`
    - `[x]` Update `MainActivity.kt` to bind recording and visualization
- `[ ]` **Testing & Validation**
    - `[ ]` Unit test for `LockFreeQueue` (C++)
    - `[ ]` Instrumented test for visualizer data polling
    - `[ ]` **Regression**: Verify Filter, LFO, and Presets still function correctly
- `[ ]` **Workflow & Review (MANDATORY)**
    - `[ ]` Create branch `feature/viz-recording`
    - `[ ]` Create Pull Request via `gh`
    - `[ ]` **Review Cycle 1**: 2 comments + fixes
    - `[ ]` **Review Cycle 2**: 2 comments + fixes
    - `[ ]` **Review Cycle 3**: 2 comments + fixes
    - `[ ]` **Review Cycle 4**: 2 comments + fixes
    - `[ ]` **Review Cycle 5**: 2 comments + fixes
    - `[ ]` Merge Message Review Loop (2 iterations)
    - `[ ]` Create `viz_recording_walkthrough.artifact.md`
    - `[ ]` Squash and Merge to `main`
