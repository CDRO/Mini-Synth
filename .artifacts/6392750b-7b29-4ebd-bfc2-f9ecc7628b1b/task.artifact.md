# Task: Voice Manager and Oscillators

Implementing the core synthesis logic: 16-voice polyphony, mono/poly toggling, and basic waveforms (Sine, Square, Saw, Triangle).

## Checklist
- `[/]` **Native Implementation**
    - `[ ]` Implement `Oscillator.h/cpp` (Waveform math)
    - `[ ]` Implement `Voice.h/cpp` (Individual voice state)
    - `[ ]` Implement `VoiceManager.h/cpp` (Voice allocation and mixing)
- `[ ]` **Audio Engine Integration**
    - `[ ]` Connect `VoiceManager` to `AudioEngine` callback
- `[ ]` **JNI & Kotlin Bridge**
    - `[ ]` Update JNI methods for note triggering and mode toggle
    - `[ ]` Update `SynthManager.kt`
- `[ ]` **Testing & Validation**
    - `[ ]` Unit tests for `Oscillator` math
    - `[ ]` Unit tests for `VoiceManager` allocation logic
- `[ ]` **Workflow & Review**
    - `[ ]` Push branch to GitHub
    - `[ ]` Create Pull Request via `gh`
    - `[ ]` Review Phase 1 (2 reviews + fixes)
    - `[ ]` Review Phase 2 (2 reviews + fixes)
    - `[ ]` Squash and Merge to `main`
