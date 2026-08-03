# Milestone 8: Keyboard Step-Recording

Implementing a recording mode where keyboard input is captured into the MIDI sequencer step-by-step.

## Checklist

### 1. JNI & Native (C++)
- `[x]` Add JNI method `recordNote(int note)` to `SynthManager`.
- `[x]` Implement logic in `MidiSequencer` to capture a note and advance to the next step.

### 2. UI & Integration (Kotlin)
- `[x]` Add "Rec Mode" toggle to the Sequencer module in `content_main.xml`.
- `[x]` Update `MainActivity.kt` to handle note recording when "Rec Mode" is ON.
- `[x]` Synchronize UI toggles with recorded notes.

### 3. Verification & Quality
- `[x]` **Unit Test**: `StepRecordingTest.kt` for step advance logic.
- `[x]` **Instrumented Test**: Verify recording UI feedback.
- `[x]` **Regression**: Run all previous stability and sequencer tests.

### 4. Workflow & Review
- `[ ]` Complete 10 review cycles.
- `[ ]` Squash and Merge to `main`.
