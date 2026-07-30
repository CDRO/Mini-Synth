# Walkthrough: Voice Manager and Oscillators

Implemented the core synthesis engine supporting 16-voice polyphony, monophonic mode, and multiple waveforms.

## Changes Made

### Synthesis Engine (C++)
- **Oscillators**: Implemented [Oscillator.h/cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/Oscillator.cpp) with optimized `float` math for Sine, Square, Saw, and Triangle waves.
- **Voice Management**: Created [Voice.h/cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/Voice.cpp) and [VoiceManager.h/cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/VoiceManager.cpp) to handle:
    - 16-voice additive mixing with normalization.
    - Round-robin voice stealing for natural note transitions.
    - Configurable Polyphonic/Monophonic modes.
    - Duplicate note detection to prevent phasing.
- **Octave Shift**: Integrated octave shift logic (±4 octaves) into the JNI bridge and engine.

### JNI & Kotlin
- Expanded [native-lib.cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/native-lib.cpp) with methods for `noteOn`, `noteOff`, `setPolyphonic`, `setWaveform`, and `setOctaveShift`.
- Updated [SynthManager.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/audio/SynthManager.kt) to expose these controls to the UI layer.

### Testing
- Created an instrumented test [SynthManagerTest.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/androidTest/java/ch/schmidlins/mini_synth/audio/SynthManagerTest.kt) to verify the JNI bridge and engine lifecycle on a device.

## Engineering Reviews Applied

### Phase 1
- **Optimization**: Switched inner-loop math to `float` primitives for mobile performance.
- **Logic**: Implemented round-robin stealing to replace simple index-0 stealing.

### Phase 2
- **Robustness**: Added sample rate validation to prevent division-by-zero.
- **Precision**: Added duplicate note checks in `noteOn` to retrigger existing voices.

## Verification Results
- **Build**: Successfully compiled for all ABIs.
- **Unit Tests**: Instrumented tests pass (engine lifecycle and basic JNI calls).
