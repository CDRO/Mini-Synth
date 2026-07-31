# Mini-Synth Development Workflow

To ensure stability and prevent regressions, all developers must follow this workflow.

## 1. Local Development
- Implement features or fixes.
- **MANDATORY**: Run all unit tests before committing:
  ```bash
  ./gradlew :app:testDebugUnitTest
  ```
- **MANDATORY**: Run all Android instrumentation tests:
  ```bash
  ./gradlew :app:connectedDebugAndroidTest
  ```

## 2. Pull Request Process
- Before pushing to the remote repository, ensure all tests pass locally.
- When creating a PR, a CI pipeline (if available) will run the same tests.
- After a review cycle, if changes are requested, re-run ALL tests after applying fixes.

## 3. Merging
- Do not merge a PR if any tests are failing.
- If a merge conflict occurs, resolve it and re-run all tests before final merge.

> [!IMPORTANT]
> The `KeyboardPadView` is sensitive to layout timing. Always verify that new UI changes don't break the keyboard's ability to initialize safely.

> [!TIP]
> Use `adb logcat` to monitor for `IndexOutOfBoundsException` during manual testing, as these can sometimes be swallowed by the UI thread without an immediate visible crash (though they usually cause a fatal exception).
