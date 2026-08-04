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

---

## Development & Review Workflow

1. **Branching**: `feature/*` or `fix/*`. Sequential development only.
2. **Feature Initialization**:
    - Create a **GitHub Milestone** for the feature.
    - Create a **GitHub Issue** with label `enhancement`, linked to the milestone.
    - **Issue Content**: The issue body MUST contain the full technical checklist.
3. **Implementation**: Code and test changes.
4. **Artifact Maintenance**:
    - Unique artifacts per feature: `[feature_name]_task.artifact.md`, `[feature_name]_review.artifact.md`, `[feature_name]_walkthrough.artifact.md`.
    - Preserve previous artifacts.
5. **Automated Testing**: Unit (GTest/JUnit) and Functional (Espresso) required. Display success output.
6. **Integration**: Push and `gh pr create`.
    - **PR Description**: Include `Closes #enhancement_issue_id`.
7. **Merge Message Review Loop**: Draft and iterate twice.
8. **Code Review Cycles (5 Cycles)**:
    - Perform a cycle:
        - Identify **2 self-reviews** on the current state.
        - Create **2 GitHub Issues** with label `review`, linked to the milestone.
        - Apply fixes, commit with `Fixes #review_issue_id`, and **push**.
        - Explicitly close the review issue using `gh issue close #review_issue_id`.
    - Repeat until **10 total review issues** are resolved.
9. **Merge**: Squash and Merge via `gh pr merge`.
10. **Cleanup**: Delete branch and close milestone.

---

## Completed Milestones
- **Milestones 1-12**: Core Synthesis, Filter, ADSR, LFO, Viz, Recording, Metronome, Sequencer, Pad Sampling, Workspace Layout, Sample Persistence.
- **Milestone 13**: Layout Squashing, Workspace Refinement, Help Mode, Demo Mode [DONE].
- **Milestone 14**: Advanced Pattern Export and Management [DONE].
- **Milestone 15**: Test Performance & Stability [DONE].

---

## Roadmap

### Milestone 16: Keyboard Interaction Refinement (Gestures) [NEXT]
- **Objective**: Implement vertical and horizontal touch gestures for real-time expressive control.
- **UX [Horizontal]**: Slide left/right on keys for **Pitch Bend**.
    - Range: +/- 2 semitones.
- **UX [Vertical Mod]**: Map vertical position (if not holding) to **Mod Wheel** (e.g., LFO depth or Filter Cutoff).
- **Engine**:
    - Implement thread-safe parameter interpolation in C++ to prevent "stepping" sounds during rapid slides.
    - Add `setPitchBend` and `setModulation` to JNI bridge.

### Milestone 17: Project & Set Management
- **Logic**: Full state capture of all 256 pads, patterns, and synth parameters.
- **UI**: Project Browser for managing large sets of sounds.
