# Task: Voice Manager and Oscillators

Implementing the core synthesis logic: 16-voice polyphony, mono/poly toggling, and basic waveforms (Sine, Square, Saw, Triangle).

## Checklist
- `[x]` **Native Implementation**
    - `[x]` Implement `Oscillator.h/cpp` (Waveform math)
    - `[x]` Implement `Voice.h/cpp` (Individual voice state)
    - `[x]` Implement `VoiceManager.h/cpp` (Voice allocation and mixing)
- `[x]` **Audio Engine Integration**
    - `[x]` Connect `VoiceManager` to `AudioEngine` callback
- `[x]` **JNI & Kotlin Bridge**
    - `[x]` Update JNI methods for note triggering and mode toggle
    - `[x]` Update `SynthManager.kt`
- `[x]` **Testing & Validation**
    - `[x]` Unit tests for `Oscillator` math
    - `[x]` Unit tests for `VoiceManager` allocation logic
- `[x]` **Workflow & Review**
    - `[x]` Push branch to GitHub
    - `[x]` Create Pull Request via `gh`
    - `[x]` Review Phase 1 (2 reviews + fixes)
    - `[x]` Review Phase 2 (2 reviews + fixes)
    - `[x]` Squash and Merge to `main`
