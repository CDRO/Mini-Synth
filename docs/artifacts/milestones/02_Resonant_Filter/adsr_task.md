# Task: ADSR Envelope

Implementing the ADSR (Attack, Decay, Sustain, Release) envelope for sound shaping and integrating it into the UI and audio engine.

## Checklist
- `[/]` **Native Engine Implementation**
    - `[ ]` Implement `Envelope.h/cpp` (ADSR logic)
    - `[ ]` Integrate `Envelope` into `Voice.h/cpp`
    - `[ ]` Expose ADSR parameters in `VoiceManager.h/cpp`
    - `[ ]` Update `native-lib.cpp` (JNI bridge for ADSR)
- `[ ]` **Kotlin & UI Integration**
    - `[ ]` Update `SynthManager.kt`
    - `[ ]` Update `content_main.xml` (Add ADSR sliders)
    - `[ ]` Update `MainActivity.kt` (Bind sliders to engine)
- `[ ]` **Testing & Validation**
    - `[ ]` Unit test for `Envelope` stage logic (C++)
    - `[ ]` Instrumented test for slider-to-engine binding (Kotlin)
- `[ ]` **Workflow & Review**
    - `[ ]` Push branch to GitHub
    - `[ ]` Create Pull Request via `gh`
    - `[ ]` Review Cycle 1 (2 comments + fixes)
    - `[ ]` Review Cycle 2 (2 comments + fixes)
    - `[ ]` Review Cycle 3 (2 comments + fixes)
    - `[ ]` Review Cycle 4 (2 comments + fixes)
    - `[ ]` Review Cycle 5 (2 comments + fixes)
    - `[ ]` Create `adsr_envelope_walkthrough.artifact.md`
    - `[ ]` Squash and Merge to `main`

## Evidence of Success
- `[ ]` `./gradlew connectedCheck` Output (with envelope tests)
