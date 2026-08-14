# Contributing to Mini-Synth

Thank you for your interest in improving Mini-Synth! To maintain the stability and audio quality of the project, we follow a **Rigid Iterative Workflow**. All contributors MUST adhere to this process.

## 🛠 The 10-Step Review Loop

Every feature or major bug fix must be developed through exactly 10 iterative cycles. This ensures that every line of code is verified, compiled, and tested multiple times before merging.

1.  **Initialization**: Create a GitHub Milestone and a linked Issue for the feature.
2.  **Branching**: Create a local feature branch (e.g., `feature/description`) and an associated Pull Request (Draft).
3.  **Baseline Verification**: Build the project to confirm a stable starting point.
4.  **Review Cycles (1-10)**: 
    *   Implement a specific sub-component of the feature.
    *   Compile the code to ensure no syntax or linker errors.
    *   Commit changes with the loop number (e.g., `Review loop 3/10: [details]`).
    *   Push to the remote branch immediately.
    *   Increment the loop counter in `.ai_state.json`.
5.  **Finalization**: Once the 10th cycle is complete, run the full test suite (JVM and Android connected tests).
6.  **Merge**: Merge the PR into `main` using the **Squash and Merge** strategy.
7.  **Cleanup**: Delete the feature branch locally and remotely.

## 💻 Coding Standards

### C++ (Audio Engine)
*   **Performance First**: No memory allocations or locks in the real-time audio callback (`onAudioReady`).
*   **Atomics**: Use `std::atomic` for parameters shared between the UI thread and the audio thread.
*   **Precision**: Use `float` literals (e.g., `1.0f`) and `PI_F` constants to avoid implicit double conversions.

### Kotlin (UI)
*   **View Binding**: Use ViewBinding for all layout interactions.
*   **Coroutines**: Perform all I/O or background tasks (like project saving or audio export) using `lifecycleScope` or `viewModelScope`.
*   **Localization**: All user-facing strings must reside in `strings.xml`.

## 🧪 Testing Requirements

*   All new logic must be accompanied by unit tests in `app/src/test/java`.
*   Audio output changes must be verified via `renderStereoSampleForTest` in the `SoundOutputTest`.
*   Layout regressions must be checked via `LayoutRatioTest`.

---

By submitting a Pull Request, you agree that your contributions will be licensed under the project's MIT License.
