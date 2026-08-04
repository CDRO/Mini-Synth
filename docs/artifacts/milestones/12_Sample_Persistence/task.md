# Milestone 12: Sample Persistence

Implementing binary serialization and automatic reloading for pad samples.

## Checklist

### 1. Native Engine (C++)
- `[ ]` Add binary write logic to `AudioEngine.cpp`.
- `[ ]` Add binary read logic to `AudioEngine.cpp`.
- `[ ]` Expose JNI methods for file I/O.

### 2. UI & Logic (Kotlin)
- `[ ]` Update `SynthPreset` to include sample paths.
- `[ ]` Implement automatic sample saving after recording.
- `[ ]` Implement startup sample loader in `MainActivity`.

### 3. Verification & Quality
- `[ ]` **Unit Test**: `PersistenceIoTest.cpp`.
- `[ ]` **Regression**: Run all previous tests.

### 4. Workflow & Review [STRICT]
- `[ ]` Push branch `feature/sample-persistence`.
- `[ ]` **MANDATORY**: Create Pull Request via `gh pr create`.
- `[ ]` 10 Code Review Cycles.
- `[ ]` Merge Message Review.
- `[ ]` Squash and Merge to `main`.
