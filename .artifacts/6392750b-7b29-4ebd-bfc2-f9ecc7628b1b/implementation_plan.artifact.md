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

---

## Development & Review Workflow

1. **Branching**: `feature/*` or `fix/*`. Sequential development only.
2. **Feature Initialization**:
    - Create a **GitHub Milestone** for the feature.
    - Create a **GitHub Issue** with label `enhancement`, linked to the milestone.
    - **Issue Content**: The issue body MUST contain the full technical checklist.
3. **Implementation**: Code and test changes.
4. **Artifact Maintenance**: Unique artifacts per feature.
5. **Automated Testing**:
    - **Fast Path**: Local JVM Unit tests + Robolectric for UI.
    - **Hardware Path**: Instrumented tests for JNI/Audio.
6. **Integration**: Push and `gh pr create`.
7. **Code Review Cycles (5 Cycles)**: 10 total review issues resolved and closed.
8. **Merge**: Squash and Merge via `gh pr merge`.
9. **Cleanup**: Delete branch and close milestone.

---

## Completed Milestones
- **Milestones 1-12**: Core Synthesis, Filter, ADSR, LFO, Viz, Recording, Metronome, Sequencer, Pad Sampling, Workspace Layout, Sample Persistence.
- **Milestone 13**: Layout Squashing, Workspace Refinement, Help Mode, Demo Mode [DONE].
- **Milestone 14**: Advanced Pattern Export and Management [DONE].

---

## Roadmap

### Milestone 15: Test Performance & Stability [IN PROGRESS]
- **Objective**: Speed up test suite and fix failing UI tests.
- **Robolectric Migration**:
    - Move `LayoutRatioTest.kt`, `WorkspaceRefinementTest.kt`, `WorkspaceUiTest.kt`, `RecordingUiTest.kt`, `SequencerUiTest.kt` to `src/test`.
    - Move `PatternPersistenceTest.kt` to `src/test`.
- **UI Fixes**:
    - Disable animations on emulator via Gradle/Shell.
    - Fix `AppNotIdleException` by disabling `beatPoller` in testing mode.
    - Adjust `LayoutRatioTest` expectations for DP-to-pixel rounding.

### Milestone 16: Keyboard Interaction Refinement (Gestures)
- **UX**: Slide left/right on keys for Pitch Bend or Mod Wheel mapping.

### Milestone 17: Project & Set Management
- **Logic**: Full state capture of all pads, patterns, and synth parameters.
