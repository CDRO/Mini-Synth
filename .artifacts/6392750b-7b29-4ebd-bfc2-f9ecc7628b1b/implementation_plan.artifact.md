# Mini-Synth Master Implementation Plan

High-performance Android synthesizer. C++ (Oboe) for audio, Kotlin for UI. Strict adherence to Caveman Rules for technical specs.

> [!CAUTION]
> **STRICT WORKFLOW REQUIREMENT**: Every feature MUST follow the GitHub-integrated workflow defined below.

## Core Architectural Requirements

### Audio & Performance
- **Performance**: C++ (Oboe/AAudio) sound generation. Target < 10ms latency.
- **Threading**: Audio thread real-time priority. **No locks/allocations in callback**.
- **Polyphony**: 16 simultaneous voices. Additive mixing. Output normalization.

### Design & UX (FL Studio Aesthetic)
- **Theme**: Dark, high-contrast. Charcoal background (#121212), Acid Green accents (#C0FF00).
- **Orientation**: Locked Landscape. Single screen layout.
- **Layout Percentages**:
    - Header (Visualizer/Metronome): Max 20% height.
    - Control/Workspace: Remaining 50%.
    - Input (Keyboard/Pads): Max 30% height.

## Technical Specifications (Caveman Mode)

### [Logic] [Oscillator]
- **Waveforms**: Sine, Square, Saw, Triangle.
- **Math**: Sine: `sin(phase)`, Square: `phase < PI ? 1 : -1`, Saw: `(phase / PI) - 1`, Triangle: `2 * abs((phase / PI) - 1) - 1`.

### [Logic] [Range]
- **Octave Shift**: +/- 4 octaves. `effective_midi = keyboard_midi + (octave_shift * 12)`. Clamp [0, 127].

---

## Development & Review Workflow

1. **Branching**: `feature/*` or `fix/*`. Sequential development only.
2. **Feature Initialization**:
    - Create a **GitHub Milestone** for the feature.
    - Create a **GitHub Issue** with label `enhancement`, linked to the milestone.
    - **Issue Content**: The issue body MUST contain the full technical checklist (copy from internal task artifact).
3. **Implementation**: Code and test changes.
4. **Artifact Maintenance**:
    - Unique artifacts per feature: `[feature_name]_task.artifact.md`, `[feature_name]_review.artifact.md`, `[feature_name]_walkthrough.artifact.md`.
    - Preserve previous artifacts.
5. **Automated Testing**: Unit (GTest/JUnit) and Functional (Espresso) required. Display success output.
6. **Integration**: Push and `gh pr create`.
    - **PR Description**:
        - Explain **Why** (problem/motivation).
        - Detail **Tests** (how it was verified).
        - State **Value** (what it adds to the app).
        - Include `Closes #enhancement_issue_id`.
7. **Merge Message Review Loop**:
    - Draft the merge message.
    - Perform at least **2 iterations** of self-review.
    - Ensure each iteration increases technical value and quality.
8. **Code Review Cycles (5 Cycles)**:
    - Perform a cycle:
        - Identify **2 self-reviews** on the current state.
        - **Requirement Verification**: Confirm implementation matches requested specs.
        - Create **2 GitHub Issues** with label `review`, linked to the milestone.
        - Apply fixes, commit with `Fixes #review_issue_id`, and **push**.
    - Repeat until **10 total review issues** are resolved.
9. **Merge**: Squash and Merge via `gh pr merge` using the reviewed Merge Message.
10. **Cleanup**: Delete branch and close milestone.

---

## Completed Milestones
- **Milestones 1-12**: Core Synthesis, Filter, ADSR, LFO, Viz, Recording, Metronome, Sequencer, Pad Sampling, Workspace Layout, Sample Persistence.

---

## Roadmap

### Milestone 13: Layout Squashing & Workspace Refinement [NEXT]
- **UI**:
    - Header (Viz/Metro): Constraint 20% height.
    - Input (Keys/Pads): Constraint 30% height.
    - Mini-fix: Remove stray character in bottom-right.
- **UX**:
    - Switch-to-Pads: Hide all config UI except "Keys" and Sample Browser.
    - Fullscreen Pads: Toggle between config and full-grid play mode.

### Milestone 14: Keyboard Interaction Refinement
- **Hold Gesture**: Slide up > 50% of key length to hold note; slide down to release.
- **Hidable Keyboard**: Button at top-right of keyboard (Down/Up arrow toggles).

### Milestone 15: Discovery & Help Mode
- **UX**: Question mark button enters help mode.
- **Interaction**: Keyboard hidden. Clicking UI elements shows interactive tooltips (Attack, Cutoff, etc.).

### Milestone 16: Demo Mode (Predefined Song)
- **Logic**: Automated playback of multi-part sequence using all synth features.

### Milestone 17: MP3 Export & Set Management
- **Logic**: Offline LAME rendering. Full set state persistence.
