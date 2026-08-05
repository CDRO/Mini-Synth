# Mini-Synth Development Workflow

To ensure stability, performance, and adherence to the "Caveman" engineering standards, all contributors (Human and AI) MUST follow this workflow.

## 1. Feature Lifecycle

> [!IMPORTANT]
> **MANDATORY STEPS**: Every feature implementation and bugfix must follow this sequential path.

1.  **Branching**: New git branch per feature or bugfix (`feature/*` or `fix/*`).
2.  **Implementation**: Concurrent code and test updates.
3.  **Local Verification**:
    - **Unit Tests**: Algorithms, math, and business logic (GTest/JUnit).
    - **Functional Tests**: UI interaction and integration (Espresso).
    - `[MANDATORY]` Run `./gradlew :app:testDebugUnitTest`
    - `[MANDATORY]` Run `./gradlew :app:connectedDebugAndroidTest`
4.  **Integration [STOP - PR REQUIRED]**:
    - Push branch to origin.
    - **MANDATORY**: Create a Pull Request via `gh pr create`.
    - **DO NOT MERGE** until 10 review cycles are complete.
    - **PR Description Requirements**:
        - **Why**: Explain the technical reason for the change.
        - **Tests**: Detail new tests and verification logic.
        - **Value**: State the specific value (performance, fix, feature) added.
5.  **Code Review Cycles (1-10)**:
    - Perform a cycle:
        - Identify **1 self-review** item.
        - **Requirement Verification**: Explicitly check if changes meet ALL technical specs in the Master Plan.
        - Post a comment on the PR via `gh pr comment`.
        - Apply fixes, commit with issue reference (e.g., `(fixes #81)`), and **push**.
    - Repeat until **10 total review cycles** are completed.
6.  **Merge Message Review Loop**:
    - Draft the merge message.
    - Perform **2 iterations** of self-review. Each iteration must increase quality and technical detail.
7.  **Merge**: Squash and Merge via `gh pr merge` using the reviewed Merge Message.
8.  **Cleanup**: Delete local and remote branches immediately.

## 2. Artifact Maintenance

Every feature milestone must have its own set of artifacts stored in `docs/artifacts/milestones/[id]_[name]/`:
- `task.md`: Checklist of implementation steps.
- `walkthrough.md`: Technical summary of changes and results.

## 3. Engineering Safety

- **Keyboard/Layout**: `KeyboardPadView` is sensitive to layout timing. Always verify that new UI changes don't break safe initialization.
- **Audio thread**: NO locks or allocations in the native `onAudioReady` callback. Use `LockFreeQueue` for all data tapping.
- **Diagnostics**: Use `adb logcat` to monitor for `IndexOutOfBoundsException` or Oboe engine restarts during manual testing.
