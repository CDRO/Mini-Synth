# Mini-Synth Master Implementation Plan

High-performance Android synthesizer. C++ (Oboe) for audio, Kotlin for UI. Strict adherence to Caveman Rules for technical specs.

## Core Architectural Requirements
- **Performance**: C++ (Oboe/AAudio) for sound generation and mixing. Low latency < 10ms.
- **Threading**: Audio thread real-time priority. **No locks/allocations in callback**.
- **Polyphony**: 16 simultaneous voices. Additive mixing. Output normalization.
- **Configurability**: Toggle between Polyphonic and Monophonic modes.
- **UI**: Landscape orientation. Single screen.
- **Keyboard**: 13-key fixed range (C to C). Support for ±4 octave internal shift.
- **Sound Board**: Toggleable 4x4 pad grid mode.
- **Feedback**: Backlit keys/pads. `Yellow` (Touch), `Red` (Record), `Blue` (Playback).

## Technical Specifications (Caveman Mode)

### [Logic] [Oscillator]
- **Waveforms**: Sine, Square, Saw, Triangle.
- **Math**:
    - Sine: `sin(phase)`
    - Square: `phase < PI ? 1 : -1`
    - Saw: `(phase / PI) - 1`
    - Triangle: `2 * abs((phase / PI) - 1) - 1`
- **Control**: `trigger(midi, velocity)`, `release(midi)`.

### [Logic] [Range]
- **Octave Shift**: Internal logic supports +/- 4 octaves.
- **Calculation**: `effective_midi = keyboard_midi + (octave_shift * 12)`.

### [Interface] [JNI Bridge]
- **JNI Methods**: `startAudio()`, `stopAudio()`, `setNote(int midi, float velocity)`, `releaseNote(int midi)`, `setPolyphony(boolean)`, `setOctaveShift(int)`.

## Development & Review Workflow
1. **Branching**: New git branch for each step. Only one feature in development at a time.
2. **Implementation**: Code and test changes.
3. **Artifact Maintenance**:
    - Unique set of artifacts per feature: `[feature_name]_task.artifact.md`, `[feature_name]_review.artifact.md`, `[feature_name]_walkthrough.artifact.md`.
    - Do not overwrite previous feature artifacts.
4. **Automated Testing**:
    - **Unit Tests**: Mandatory for algorithms, C++ math, and business logic (GTest/JUnit).
    - **Functional Tests**: Mandatory for UI interactions and integration (Espresso/UI Automator).
    - **Regressions**: New features must not break existing tests. Fixes required before merge.
    - **Evidence**: Test success output must be displayed in conversation.
5. **Commit**: Meaningful messages. Author: `Gemini <gemini@google.com>`.
6. **Integration**: Push to GitHub and `gh pr create`.
    - **GH CLI Path**: `C:\Program Files\GitHub CLI\gh.exe`.
7. **Review Rounds (1-5)**:
    - Create a self-review.
    - Post as a **separate comment** on the GitHub PR using `gh pr comment`.
    - Apply fix and commit.
    - Repeat 5 times.
8. **Merge**: Squash and Merge via `gh pr merge`. Author: `Gemini <gemini@google.com>`.

---

## Current Feature: Comprehensive Testing Catch-up

### [Testing] [C++]
- **Framework**: GTest (or equivalent native test harness).
- **Oscillator Tests**: Verify Sine, Square, Saw, Triangle values at key phases (0, PI/2, PI, 1.5PI).
- **Voice Manager Tests**: Verify 16-voice allocation, round-robin stealing, and mono/poly switching logic.

### [Testing] [Kotlin]
- **Framework**: JUnit 4 / Espresso.
- **SynthManager Tests**: Verify JNI connectivity and parameter passing.
- **UI Tests**: Verify `KeyboardPadView` mode switching (Keys <-> Pads) and coordinate-to-midi mapping.

## Proposed Changes

### [C++ Unit Tests]
#### [NEW] [OscillatorTests.cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/test/cpp/OscillatorTests.cpp)
Math verification for all waveforms.

#### [NEW] [VoiceManagerTests.cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/test/cpp/VoiceManagerTests.cpp)
Allocation and mixing logic verification.

### [Kotlin Tests]
#### [NEW] [SynthManagerTest.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/test/java/ch/schmidlins/mini_synth/audio/SynthManagerTest.kt)
Unit tests for the JNI wrapper.

#### [NEW] [KeyboardViewTest.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/androidTest/java/ch/schmidlins/mini_synth/ui/KeyboardViewTest.kt)
Espresso tests for UI interaction and mode toggling.

## Verification Plan
### Automated
- Run `./gradlew test` (Local Unit Tests).
- Run `./gradlew connectedCheck` (Instrumented Tests).
- Display all `PASSED` summaries.
