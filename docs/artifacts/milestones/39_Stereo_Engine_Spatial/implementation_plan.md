# Implementation Plan - Milestone 39: Stereo Engine & Spatial Routing

Enhance the soundstage of Mini-Synth by transitioning the audio pipeline from mono to stereo and adding panning controls.

## User Review Required

> [!WARNING]
> **Performance Overhead**: Transitioning to stereo will double the sample processing in the audio callback. We will monitor the CPU usage closely on low-end devices during the review loop.

## Proposed Changes

### [Audio Engine]

#### [MODIFY] [AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- Update `Oboe` stream builder to `setChannelCount(oboe::ChannelCount::Stereo)`.
- Update `onAudioReady` loop to interleave Left and Right samples.

#### [MODIFY] [VoiceManager.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/VoiceManager.cpp)
- Add `float mPanning` parameter to `EngineParams`.
- Update `nextSample()` to return a `std::pair<float, float>` or fill a stereo buffer.
- Implement Equal Power Panning logic.

### [UI / Kotlin]

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
- Add a "Pan" slider to the parameter container.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Bind the Pan slider to the JNI `setPanning(value)` method.

## Verification Plan

### Manual Verification
- Use headphones.
- Move the Pan slider Left and Right. Verify the sound follows the position.
- Check that effects (Delay/Reverb) now feel wider or correctly follow the panned source.
