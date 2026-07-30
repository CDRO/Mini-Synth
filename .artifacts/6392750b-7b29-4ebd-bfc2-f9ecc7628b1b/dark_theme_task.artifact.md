# Task: Dark Theme and Device Compatibility

Implementing the FL Studio-inspired dark aesthetic and adjusting SDK versions for virtual device compatibility.

## Checklist
- `[/]` **Compatibility Setup**
    - `[ ]` Update `minSdk` to 28 and `compileSdk`/`targetSdk` to 35 in `build.gradle.kts`
- `[ ]` **Design Implementation**
    - `[ ]` Define Acid Green, Electric Blue, and Charcoal in `colors.xml`
    - `[ ]` Update `themes.xml` for full dark mode
    - `[ ]` Update `KeyboardPadView.kt` paints (Key colors, Acid Green feedback)
- `[ ]` **Testing & Validation**
    - `[ ]` Create `ThemeVisibilityTest.kt` (Espresso)
    - `[ ]` Verify successful run on API 28+ virtual device
- `[ ]` **Workflow & Review**
    - `[ ]` Push branch to GitHub
    - `[ ]` Create Pull Request via `gh`
    - `[ ]` Review Phase 1-5 (5 separate comments + fixes)
    - `[ ]` Create `dark_theme_walkthrough.artifact.md`
    - `[ ]` Squash and Merge to `main`

## Evidence of Success
- `[ ]` `./gradlew connectedCheck` Output
