# Implementation Plan - Native Build Fix: Step Record Back

Fix missing `stepRecordBack` declaration in `AudioEngine.h` to restore native build stability.

## User Review Required

> [!IMPORTANT]
> This is a surgical fix to resolve a build regression.

## Proposed Changes

### [Audio Engine]

#### [MODIFY] [AudioEngine.h](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.h)
- Add `stepRecordBack()` delegate method calling `mMidiSequencer.stepRecordBack()`.

## Verification Plan

### Manual Verification
- Run `.\gradlew.bat assembleDebug` and verify successful compilation of native code.
