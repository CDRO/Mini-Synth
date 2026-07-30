# Task: UI Keyboard and Pads

Implementing the custom UI component that acts as both a piano keyboard and a sound board (4x4 pads), with backlighting and interaction logic.

## Checklist
- `[x]` **UI Component Development**
    - `[x]` Create `KeyboardPadView.kt` (Custom View)
    - `[x]` Implement Keyboard drawing (1 octave, C to C)
    - `[x]` Implement Pad Grid drawing (4x4)
    - `[x]` Implement Toggle logic (Keys vs. Pads)
- `[x]` **Interaction & Feedback**
    - `[x]` Handle touch events for note triggering
    - `[x]` Implement backlighting colors (Yellow for touch, Red for record, Blue for play)
    - `[x]` Integrate with `SynthManager` for real-time audio
- `[x]` **Activity Integration**
    - `[x]` Update `content_main.xml` to include the new view
    - `[x]` Configure `MainActivity.kt` for landscape and UI control
- `[x]` **Testing & Validation**
    - `[x]` Functional test for UI mode switching
    - `[x]` Manual verification of backlighting on device
- `[x]` **Workflow & Review**
    - `[x]` Push branch to GitHub
    - `[x]` Create Pull Request via `gh`
    - `[x]` Review Phase 1 (`ui_keyboard_pads_review.artifact.md`)
    - `[x]` Review Phase 2
    - `[x]` Create `ui_keyboard_pads_walkthrough.artifact.md`
    - `[x]` Squash and Merge to `main`
