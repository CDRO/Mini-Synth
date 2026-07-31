# Task: Metronome & BPM Control

Implementing a sample-accurate native metronome and BPM control system.

## Checklist

- `[ ]` **Native Implementation (C++)**
    - `[ ]` Add BPM and Metronome state to `AudioEngine.h`
    - `[ ]` Implement timing and tick generation in `AudioEngine.cpp`
    - `[ ]` Expose BPM/Metronome via `native-lib.cpp`
- `[ ]` **UI & Kotlin Integration**
    - `[ ]` Update `SynthManager.kt` JNI bridge
    - `[ ]` Add BPM controls and Metronome toggle to `content_main.xml`
    - `[ ]` Bind UI to engine in `MainActivity.kt`
- `[ ]` **Testing & Validation**
    - `[ ]` Unit test for BPM-to-samples calculation
    - `[ ]` Manual check of audio/visual sync
- `[ ]` **Workflow & Review (MANDATORY)**
    - `[ ]` Create branch `feature/metronome`
    - `[ ]` Create Pull Request via `gh`
    - `[ ]` **Review Cycle 1**: 1 comment + fix + push
    - `[ ]` **Review Cycle 2**: 1 comment + fix + push
    - `[ ]` **Review Cycle 3**: 1 comment + fix + push
    - `[ ]` **Review Cycle 4**: 1 comment + fix + push
    - `[ ]` **Review Cycle 5**: 1 comment + fix + push
    - `[ ]` **Review Cycle 6**: 1 comment + fix + push
    - `[ ]` **Review Cycle 7**: 1 comment + fix + push
    - `[ ]` **Review Cycle 8**: 1 comment + fix + push
    - `[ ]` **Review Cycle 9**: 1 comment + fix + push
    - `[ ]` **Review Cycle 10**: 1 comment + fix + push
    - `[ ]` Merge Message Review Loop (2 iterations)
    - `[ ]` Create `metronome_walkthrough.artifact.md`
    - `[ ]` Squash and Merge to `main`
