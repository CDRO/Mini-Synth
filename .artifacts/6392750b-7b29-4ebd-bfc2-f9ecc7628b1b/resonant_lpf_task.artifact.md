# Task: Resonant Low-Pass Filter (LPF)

Implementing a 2-pole resonant low-pass filter (State Variable Filter) per voice to enable classic synth sound shaping.

## Checklist
- `[/]` **Native Engine Implementation**
    - `[ ]` Implement `Filter.h/cpp` (State Variable Filter logic)
    - `[ ]` Integrate `Filter` into `Voice.h/cpp`
    - `[ ]` Expose Filter parameters (Cutoff, Resonance) in `VoiceManager`
    - `[ ]` Update `native-lib.cpp` (JNI bridge for LPF)
- `[ ]` **Kotlin & UI Integration**
    - `[ ]` Update `SynthManager.kt`
    - `[ ]` Update `content_main.xml` (Add Filter sliders)
    - `[ ]` Update `MainActivity.kt` (Bind Filter UI)
- `[ ]` **Testing & Validation**
    - `[ ]` Unit test for `Filter` frequency response (C++)
    - `[ ]` Instrumented test for filter parameter updates (Kotlin)
    - `[ ]` **Regression**: Run all previous tests (Envelope, LFO, UI, Engine)
- `[ ]` **Workflow & Review**
    - `[ ]` Push branch to GitHub
    - `[ ]` Create Pull Request with technical "Why/Tests/Value" description
    - `[ ]` Review Cycle 1-5 (10 comments + fixes, separate comments)
    - `[ ]` Merge Message Review Loop (2 iterations)
    - `[ ]` Create `resonant_lpf_walkthrough.artifact.md`
    - `[ ]` Squash and Merge to `main`

## Evidence of Success
- `[ ]` `./gradlew connectedCheck` Output
