# Task: UI Keyboard and Pads

Implementing the custom UI component that acts as both a piano keyboard and a sound board (4x4 pads), with backlighting and interaction logic.

## Checklist
- `[/]` **UI Component Development**
    - `[ ]` Create `KeyboardPadView.kt` (Custom View)
    - `[ ]` Implement Keyboard drawing (1 octave, C to C)
    - `[ ]` Implement Pad Grid drawing (4x4)
    - `[ ]` Implement Toggle logic (Keys vs. Pads)
- `[ ]` **Interaction & Feedback**
    - `[ ]` Handle touch events for note triggering
    - `[ ]` Implement backlighting colors (Yellow for touch, Red for record, Blue for play)
    - `[ ]` Integrate with `SynthManager` for real-time audio
- `[ ]` **Activity Integration**
    - `[ ]` Update `content_main.xml` to include the new view
    - `[ ]` Configure `MainActivity.kt` for landscape and UI control
- `[ ]` **Testing & Validation**
    - `[ ]` Functional test for UI mode switching
    - `[ ]` Manual verification of backlighting on device
- `[ ]` **Workflow & Review**
    - `[ ]` Push branch to GitHub
    - `[ ]` Create Pull Request via `gh`
    - `[ ]` Review Phase 1 (`ui_keyboard_pads_review.artifact.md`)
    - `[ ]` Review Phase 2
    - `[ ]` Create `ui_keyboard_pads_walkthrough.artifact.md`
    - `[ ]` Squash and Merge to `main`
