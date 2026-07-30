#include <jni.h>
#include <memory>
#include <mutex>
#include "AudioEngine.h"

static std::unique_ptr<AudioEngine> engine;
static std::mutex engineMutex;

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_startEngine(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (!engine) {
        engine = std::make_unique<AudioEngine>();
    }
    engine->start();
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_stopEngine(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) {
        engine->stop();
        engine.reset();
    }
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_noteOn(JNIEnv *env, jobject thiz, jint midi_note, jfloat velocity) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->noteOn(midi_note, velocity);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_noteOff(JNIEnv *env, jobject thiz, jint midi_note) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->noteOff(midi_note);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setPolyphonic(JNIEnv *env, jobject thiz, jboolean is_polyphonic) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setPolyphonic(is_polyphonic);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setWaveform(JNIEnv *env, jobject thiz, jint waveform_index) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setWaveform(static_cast<Waveform>(waveform_index));
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setOctaveShift(JNIEnv *env, jobject thiz, jint shift) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setOctaveShift(shift);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setAttack(JNIEnv *env, jobject thiz, jfloat seconds) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setAttack(seconds);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setDecay(JNIEnv *env, jobject thiz, jfloat seconds) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setDecay(seconds);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setSustain(JNIEnv *env, jobject thiz, jfloat level) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setSustain(level);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setRelease(JNIEnv *env, jobject thiz, jfloat seconds) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setRelease(seconds);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setMasterVolume(JNIEnv *env, jobject thiz, jfloat volume) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setMasterVolume(volume);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setLfoRate(JNIEnv *env, jobject thiz, jfloat frequency) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setLfoRate(frequency);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setLfoDepth(JNIEnv *env, jobject thiz, jfloat depth) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setLfoDepth(depth);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setLfoWaveform(JNIEnv *env, jobject thiz, jint waveform_index) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setLfoWaveform(static_cast<Waveform>(waveform_index));
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setLfoTarget(JNIEnv *env, jobject thiz, jint target_index) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setLfoTarget(static_cast<LfoTarget>(target_index));
}

extern "C" JNIEXPORT jfloat JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_renderSampleForTest(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) return engine->renderSampleForTest();
    return 0.0f;
}
