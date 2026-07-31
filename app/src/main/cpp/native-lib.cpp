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

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setFilterCutoff(JNIEnv *env, jobject thiz, jfloat frequency) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setFilterCutoff(frequency);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setFilterResonance(JNIEnv *env, jobject thiz, jfloat resonance) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setFilterResonance(resonance);
}

extern "C" JNIEXPORT jfloat JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_renderSampleForTest(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) return engine->renderSampleForTest();
    return 0.0f;
}

extern "C" JNIEXPORT jint JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_getVisualizerData(JNIEnv *env, jobject thiz, jfloatArray buffer) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (!engine) return 0;

    jsize len = env->GetArrayLength(buffer);
    float* nativeBuffer = env->GetFloatArrayElements(buffer, nullptr);

    int32_t count = engine->getVisualizerData(nativeBuffer, len);

    env->ReleaseFloatArrayElements(buffer, nativeBuffer, 0);
    return count;
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_startRecording(JNIEnv *env, jobject thiz, jstring path) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (!engine) return;

    const char* nativePath = env->GetStringUTFChars(path, nullptr);
    engine->startRecording(std::string(nativePath));
    env->ReleaseStringUTFChars(path, nativePath);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_stopRecording(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->stopRecording();
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setBpm(JNIEnv *env, jobject thiz, jfloat bpm) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setBpm(bpm);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setMetronomeEnabled(JNIEnv *env, jobject thiz, jboolean enabled) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setMetronomeEnabled(enabled == JNI_TRUE);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_isBeatStarted(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) return engine->isBeatStarted();
    return JNI_FALSE;
}
