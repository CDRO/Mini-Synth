#include <jni.h>
#include <string>
#include "AudioEngine.h"

static AudioEngine *engine = nullptr;

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_startEngine(JNIEnv *env, jobject thiz) {
    if (engine == nullptr) {
        engine = new AudioEngine();
    }
    engine->start();
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_stopEngine(JNIEnv *env, jobject thiz) {
    if (engine != nullptr) {
        engine->stop();
        delete engine;
        engine = nullptr;
    }
}
