# Task: Preset Management

Implementing Preset saving/loading using Jetpack DataStore and Kotlinx Serialization.

## Checklist
- `[x]` **Dependency Setup**
    - `[x]` Update `libs.versions.toml`
    - `[x]` Update `app/build.gradle.kts`
- `[x]` **Data Model & Logic**
    - `[x]` Create `SynthPreset.kt`
    - `[x]` Create `PresetRepository.kt` (DataStore logic)
- `[x]` **UI Integration**
    - `[x]` Add Save/Load buttons to `content_main.xml`
    - `[x]` Implement Save/Load dialogs in `MainActivity.kt`
    - `[x]` Bind Preset loading to `SynthManager` and UI sliders
- `[/]` **Testing & Validation**
    - `[x]` Unit test for JSON serialization
    - `[ ]` Instrumented test for DataStore persistence
    - `[ ]` **Regression**: Run previous tests (Filter, LFO, ADSR)
- `[ ]` **Workflow & Review**
    - `[ ]` Push branch `feature/preset-management`
    - `[ ]` Create Pull Request
    - `[ ]` Review Cycle 1-5 (10 comments + fixes)
    - `[ ]` Merge Message Review Loop (2 iterations)
    - `[ ]` Squash and Merge to `main`
