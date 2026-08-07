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

extern "C" JNIEXPORT jboolean JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_isEngineRunning(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) return engine->isRunning() ? JNI_TRUE : JNI_FALSE;
    return JNI_FALSE;
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
Java_ch_schmidlins_mini_1synth_audio_SynthManager_padNoteOn(JNIEnv *env, jobject thiz, jint pad_index, jfloat velocity) {
    if (pad_index < 0 || pad_index >= 256) return;
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) {
        // __android_log_print(ANDROID_LOG_DEBUG, "JNI", "Pad Note On: %d", pad_index);
        engine->padNoteOn(pad_index, velocity);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_padNoteOff(JNIEnv *env, jobject thiz, jint pad_index) {
    if (pad_index < 0 || pad_index >= 256) return;
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->padNoteOff(pad_index);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setPadLooping(JNIEnv *env, jobject thiz, jint pad_index, jboolean looping) {
    if (pad_index < 0 || pad_index >= 256) return;
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setPadLooping(pad_index, looping == JNI_TRUE);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_startPadSampling(JNIEnv *env, jobject thiz, jint pad_index) {
    if (pad_index < 0 || pad_index >= 256) return;
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->startPadSampling(pad_index);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_stopPadSampling(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->stopPadSampling();
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_loadFactorySample(JNIEnv *env, jobject thiz, jint pad_index, jint sample_id) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->loadFactorySample(pad_index, sample_id);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_savePadSample(JNIEnv *env, jobject thiz, jint pad_index, jstring path) {
    const char *nativePath = env->GetStringUTFChars(path, nullptr);
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->savePadSample(pad_index, nativePath);
    env->ReleaseStringUTFChars(path, nativePath);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_loadPadSample(JNIEnv *env, jobject thiz, jint pad_index, jstring path) {
    const char *nativePath = env->GetStringUTFChars(path, nullptr);
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->loadPadSample(pad_index, nativePath);
    env->ReleaseStringUTFChars(path, nativePath);
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
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setAftertouchTarget(JNIEnv *env, jobject thiz, jint target_index) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setAftertouchTarget(static_cast<LfoTarget>(target_index));
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

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setPitchBend(JNIEnv *env, jobject thiz, jfloat semitones) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setPitchBend(semitones);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setModulation(JNIEnv *env, jobject thiz, jfloat amount) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setModulation(amount);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setAftertouch(JNIEnv *env, jobject thiz, jint midi_note, jfloat amount) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setAftertouch(midi_note, amount);
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

extern "C" JNIEXPORT jint JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_getFftData(JNIEnv *env, jobject thiz, jfloatArray buffer) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (!engine) return 0;

    jsize len = env->GetArrayLength(buffer);
    float* nativeBuffer = env->GetFloatArrayElements(buffer, nullptr);

    int32_t count = engine->getFftData(nativeBuffer, len);

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
Java_ch_schmidlins_mini_1synth_audio_SynthManager_renderPatternToFile(JNIEnv *env, jobject thiz, jstring path) {
    const char *nativePath = env->GetStringUTFChars(path, nullptr);
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->renderPatternToFile(std::string(nativePath));
    env->ReleaseStringUTFChars(path, nativePath);
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

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setSequencerPlaying(JNIEnv *env, jobject thiz, jboolean playing) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setSequencerPlaying(playing == JNI_TRUE);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setSequencerRecording(JNIEnv *env, jobject thiz, jboolean recording) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setSequencerRecording(recording == JNI_TRUE);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_isSequencerRecording(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) return engine->isSequencerRecording() ? JNI_TRUE : JNI_FALSE;
    return JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_isSequencerPlaying(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) return engine->isSequencerPlaying() ? JNI_TRUE : JNI_FALSE;
    return JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setSequencerNote(JNIEnv *env, jobject thiz, jint step, jint note, jboolean active) {
    if (step < 0 || step >= 16 || note < 0 || note >= 128) return;
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setSequencerNote(step, note, active == JNI_TRUE);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_isSequencerNoteActive(JNIEnv *env, jobject thiz, jint step, jint note) {
    if (step < 0 || step >= 16 || note < 0 || note >= 128) return JNI_FALSE;
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) return engine->isSequencerNoteActive(step, note) ? JNI_TRUE : JNI_FALSE;
    return JNI_FALSE;
}

extern "C" JNIEXPORT jintArray JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_getSequencerActiveNotes(JNIEnv *env, jobject thiz, jint step) {
    if (step < 0 || step >= 16) return nullptr;
    std::lock_guard<std::mutex> lock(engineMutex);
    if (!engine) return nullptr;

    std::vector<int> notes;
    engine->getSequencerActiveNotes(step, notes);

    jintArray result = env->NewIntArray(notes.size());
    if (notes.size() > 0) {
        env->SetIntArrayRegion(result, 0, notes.size(), notes.data());
    }
    return result;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_isSequencerStepActive(JNIEnv *env, jobject thiz, jint step) {
    if (step < 0 || step >= 16) return JNI_FALSE;
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) return engine->isSequencerStepActive(step) ? JNI_TRUE : JNI_FALSE;
    return JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_clearSequencer(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->clearSequencer();
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setInputQuantize(JNIEnv *env, jobject thiz, jboolean enabled) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setInputQuantize(enabled == JNI_TRUE);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setOverdub(JNIEnv *env, jobject thiz, jboolean enabled) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setOverdub(enabled == JNI_TRUE);
}

extern "C" JNIEXPORT jint JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_recordSequencerNote(JNIEnv *env, jobject thiz, jint note) {
    if (note < 0 || note >= 128) return 0;
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) return engine->recordSequencerNote(note);
    return 0;
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_handleRealTimeNoteOn(JNIEnv *env, jobject thiz, jint note) {
    if (note < 0 || note >= 128) return;
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->handleRealTimeNoteOn(note);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_handleRealTimeNoteOff(JNIEnv *env, jobject thiz, jint note) {
    if (note < 0 || note >= 128) return;
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->handleRealTimeNoteOff(note);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setSequencerStepDuration(JNIEnv *env, jobject thiz, jfloat division) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setSequencerStepDuration(division);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setSequencerNumSteps(JNIEnv *env, jobject thiz, jint steps) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setSequencerNumSteps(steps);
}

extern "C" JNIEXPORT jint JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_getSequencerCurrentStep(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) return engine->getSequencerCurrentStep();
    return 0;
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_saveProject(JNIEnv *env, jobject thiz, jstring directory) {
    const char *nativeDir = env->GetStringUTFChars(directory, nullptr);
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->saveProject(std::string(nativeDir));
    env->ReleaseStringUTFChars(directory, nativeDir);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_loadProject(JNIEnv *env, jobject thiz, jstring directory) {
    const char *nativeDir = env->GetStringUTFChars(directory, nullptr);
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->loadProject(std::string(nativeDir));
    env->ReleaseStringUTFChars(directory, nativeDir);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_processMidi(JNIEnv *env, jobject thiz, jbyteArray data, jint length) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) {
        jbyte* bytes = env->GetByteArrayElements(data, nullptr);
        engine->processExternalMidi(reinterpret_cast<const uint8_t*>(bytes), length);
        env->ReleaseByteArrayElements(data, bytes, JNI_ABORT);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setDelayTime(JNIEnv *env, jobject thiz, jfloat seconds) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setDelayTime(seconds);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setDelayFeedback(JNIEnv *env, jobject thiz, jfloat feedback) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setDelayFeedback(feedback);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setDelayMix(JNIEnv *env, jobject thiz, jfloat mix) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setDelayMix(mix);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setReverbSize(JNIEnv *env, jobject thiz, jfloat size) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setReverbSize(size);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setReverbDamping(JNIEnv *env, jobject thiz, jfloat damping) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setReverbDamping(damping);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setReverbMix(JNIEnv *env, jobject thiz, jfloat mix) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setReverbMix(mix);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_startAutomatedSampling(JNIEnv *env, jobject thiz, jint pad_index, jfloat duration) {
    if (pad_index < 0 || pad_index >= 256) return;
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->startAutomatedSampling(pad_index, duration);
}

extern "C" JNIEXPORT jint JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_getXRunCount(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) return engine->getXRunCount();
    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_getBufferSize(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) return engine->getBufferSize();
    return 0;
}
