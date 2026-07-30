# Task: Oboe Setup and Native Audio Engine Base

Setting up the NDK environment, Oboe library integration, and basic C++ engine skeleton.

## Checklist
- `[x]` **Infrastructure Setup**
    - `[x]` Create C++ directory structure (`app/src/main/cpp`)
    - `[x]` Configure `app/build.gradle.kts` for NDK and CMake
    - `[x]` Add Oboe dependency to `build.gradle.kts`
    - `[x]` Create `CMakeLists.txt`
- `[x]` **Native Implementation**
    - `[x]` Implement `native-lib.cpp` (JNI Bridge)
    - `[x]` Implement `AudioEngine.cpp/h` (Oboe stream init)
- `[x]` **Kotlin Integration**
    - `[x]` Create `SynthManager.kt` wrapper
- `[/]` **Testing & Validation**
    - `[ ]` Create JNI connectivity unit test
    - `[ ]` Verify build success
- `[ ]` **Workflow & Review**
    - `[ ]` Push branch to GitHub
    - `[ ]` Create Pull Request via `gh`
    - `[ ]` Review Phase 1 (2 reviews + fixes)
    - `[ ]` Review Phase 2 (2 reviews + fixes)
    - `[ ]` Squash and Merge to `main`
