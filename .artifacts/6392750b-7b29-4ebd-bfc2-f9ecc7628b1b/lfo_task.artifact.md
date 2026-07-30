# Task: LFO Modulation

Implementing a Low-Frequency Oscillator (LFO) to modulate parameters like Pitch, Volume, and Filter Cutoff.

## Checklist
- `[/]` **Native Engine Implementation**
    - `[ ]` Implement `Lfo.h/cpp` (Waveforms: Sine, Tri, Squ, Saw)
    - `[ ]` Integrate `Lfo` into `Voice.h/cpp`
    - `[ ]` Implement modulation targets (Vibrato, Tremolo) in `Voice`
    - `[ ]` Expose LFO parameters (Rate, Depth, Target, Waveform) in `VoiceManager`
- `[ ]` **Kotlin & UI Integration**
    - `[ ]` Update `SynthManager.kt` JNI bridge
    - `[ ]` Update `content_main.xml` with LFO control module
    - `[ ]` Update `MainActivity.kt` to bind LFO UI
- `[ ]` **Testing & Validation**
    - `[ ]` Unit test for `Lfo` frequency and range (C++)
    - `[ ]` Instrumented test for modulation impact on samples
    - `[ ]` **Regression**: Run all previous tests (Envelope, UI, Engine)
- `[ ]` **Workflow & Review**
    - `[ ]` Push branch to GitHub
    - `[ ]` Create Pull Request with technical "Why/Tests/Value" description
    - `[ ]` Review Cycle 1-5 (10 comments + fixes, separate comments)
    - `[ ]` Merge Message Review Loop (2 iterations)
    - `[ ]` Create `lfo_walkthrough.artifact.md`
    - `[ ]` Squash and Merge to `main`

## Evidence of Success
- `[ ]` `./gradlew connectedCheck` Output
