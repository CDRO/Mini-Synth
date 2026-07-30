# Task: Visual Sequencer Indicators

Implementing the visual feedback system for recording and playback states on the keyboard and pads.

## Checklist
- `[/]` **UI Implementation**
    - `[ ]` Update `KeyboardPadView.kt` to handle multiple concurrent backlight states per note.
    - `[ ]` Prioritize `Touch` (Yellow) > `Record` (Red) > `Play` (Blue).
- `[ ]` **Sequencer Mock Integration**
    - `[ ]` Add "Rec" and "Play" mock buttons to the control bar.
    - `[ ]` Implement logic to cycle note backlights to verify colors.
- `[ ]` **Testing & Validation**
    - `[ ]` Espresso test for backlight priority (e.g. check color when both Play and Touch are active).
- `[ ]` **Workflow & Review**
    - `[ ]` Push branch to GitHub
    - `[ ]` Create PR with **PAINFUL CAVEMAN DETAIL**.
    - `[ ]` Review Cycles 1-5 (10 comments + fixes).
    - `[ ]` Create `visual_sequencer_walkthrough.artifact.md`.
    - `[ ]` Squash and Merge to `main`.

## Evidence of Success
- `[ ]` `./gradlew connectedCheck` Output (Backlight priority tests)
