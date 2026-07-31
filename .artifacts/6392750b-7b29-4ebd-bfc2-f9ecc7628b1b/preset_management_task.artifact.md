# Task: Preset Management

Implementing Preset saving/loading using Jetpack DataStore and Kotlinx Serialization.

## Checklist
- `[ ]` **Dependency Setup**
    - `[ ]` Update `libs.versions.toml`
    - `[ ]` Update `app/build.gradle.kts`
- `[ ]` **Data Model & Logic**
    - `[ ]` Create `SynthPreset.kt`
    - `[ ]` Create `PresetRepository.kt` (DataStore logic)
- `[ ]` **UI Integration**
    - `[ ]` Add Save/Load buttons to `content_main.xml`
    - `[ ]` Implement Save/Load dialogs in `MainActivity.kt`
    - `[ ]` Bind Preset loading to `SynthManager` and UI sliders
- `[ ]` **Testing & Validation**
    - `[ ]` Unit test for JSON serialization
    - `[ ]` Instrumented test for DataStore persistence
    - `[ ]` **Regression**: Run previous tests (Filter, LFO, ADSR)
- `[ ]` **Workflow & Review**
    - `[ ]` Push branch `feature/preset-management`
    - `[ ]` Create Pull Request
    - `[ ]` Review Cycle 1-5 (10 comments + fixes)
    - `[ ]` Merge Message Review Loop (2 iterations)
    - `[ ]` Squash and Merge to `main`
