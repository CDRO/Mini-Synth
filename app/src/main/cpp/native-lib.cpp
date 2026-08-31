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
Java_ch_schmidlins_mini_1synth_audio_SynthManager_noteOn(JNIEnv *env, jobject thiz, jint midi_note, jfloat velocity, jint track_id) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->noteOn(midi_note, velocity, track_id);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_noteOff(JNIEnv *env, jobject thiz, jint midi_note) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->noteOff(midi_note);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_padNoteOn(JNIEnv *env, jobject thiz, jint pad_index, jfloat velocity) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->padNoteOn(pad_index, velocity);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_padNoteOff(JNIEnv *env, jobject thiz, jint pad_index) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->padNoteOff(pad_index);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setPadLooping(JNIEnv *env, jobject thiz, jint pad_index, jboolean looping) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setPadLooping(pad_index, looping == JNI_TRUE);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_startPadSampling(JNIEnv *env, jobject thiz, jint pad_index) {
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
    if (engine) engine->setPolyphonic(is_polyphonic == JNI_TRUE);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackWaveform(JNIEnv *env, jobject thiz, jint track, jint waveform_index) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackWaveform(track, static_cast<Waveform>(waveform_index));
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setOctaveShift(JNIEnv *env, jobject thiz, jint shift) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setOctaveShift(shift);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackAttack(JNIEnv *env, jobject thiz, jint track, jfloat seconds) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackAttack(track, seconds);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackDecay(JNIEnv *env, jobject thiz, jint track, jfloat seconds) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackDecay(track, seconds);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackSustain(JNIEnv *env, jobject thiz, jint track, jfloat level) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackSustain(track, level);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackRelease(JNIEnv *env, jobject thiz, jint track, jfloat seconds) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackRelease(track, seconds);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setMasterVolume(JNIEnv *env, jobject thiz, jfloat volume) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setMasterVolume(volume);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackVolume(JNIEnv *env, jobject thiz, jint track, jfloat volume) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackVolume(track, volume);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackPanning(JNIEnv *env, jobject thiz, jint track, jfloat panning) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackPanning(track, panning);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackUnison(JNIEnv *env, jobject thiz, jint track, jint count, jfloat detune, jfloat spread) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackUnison(track, count, detune, spread);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackMorph(JNIEnv *env, jobject thiz, jint track, jfloat morph) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackMorph(track, morph);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackPhaseDistortion(JNIEnv *env, jobject thiz, jint track, jfloat pd) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackPhaseDistortion(track, pd);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setWavetable(JNIEnv *env, jobject thiz, jfloatArray data) {
    jsize len = env->GetArrayLength(data);
    float* nativeData = env->GetFloatArrayElements(data, nullptr);
    {
        std::lock_guard<std::mutex> lock(engineMutex);
        if (engine) engine->setWavetable(nativeData, len);
    }
    env->ReleaseFloatArrayElements(data, nativeData, JNI_ABORT);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setPadPanning(JNIEnv *env, jobject thiz, jint pad_index, jfloat panning) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setPadPanning(pad_index, panning);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackLfoRate(JNIEnv *env, jobject thiz, jint track, jfloat frequency) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackLfoRate(track, frequency);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackLfoDepth(JNIEnv *env, jobject thiz, jint track, jfloat depth) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackLfoDepth(track, depth);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackLfoWaveform(JNIEnv *env, jobject thiz, jint track, jint waveform_index) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackLfoWaveform(track, static_cast<Waveform>(waveform_index));
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackLfoTarget(JNIEnv *env, jobject thiz, jint track, jint target_index) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackLfoTarget(track, static_cast<LfoTarget>(target_index));
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackLfoSync(JNIEnv *env, jobject thiz, jint track, jboolean enabled, jfloat beats_per_cycle) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackLfoSync(track, enabled == JNI_TRUE, beats_per_cycle);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackLfoMatrixAmount(JNIEnv *env, jobject thiz, jint track, jint target_index, jfloat amount) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackLfoMatrixAmount(track, target_index, amount);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setAftertouchTarget(JNIEnv *env, jobject thiz, jint target_index) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setAftertouchTarget(static_cast<LfoTarget>(target_index));
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackFilterCutoff(JNIEnv *env, jobject thiz, jint track, jfloat frequency) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackFilterCutoff(track, frequency);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackFilterResonance(JNIEnv *env, jobject thiz, jint track, jfloat resonance) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackFilterResonance(track, resonance);
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
    const char* nativePath = env->GetStringUTFChars(path, nullptr);
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->startRecording(std::string(nativePath));
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
    if (engine) return engine->isBeatStarted() ? JNI_TRUE : JNI_FALSE;
    return JNI_FALSE;
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

extern "C" JNIEXPORT jint JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_getFramesPerBurst(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) return engine->getFramesPerBurst();
    return 0;
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_checkAndApplyBufferSize(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->checkAndApplyBufferSize();
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setAutoLatencyEnabled(JNIEnv *env, jobject thiz, jboolean enabled) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setAutoLatencyEnabled(enabled == JNI_TRUE);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setSequencerPlaying(JNIEnv *env, jobject thiz, jboolean playing) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setSequencerPlaying(playing == JNI_TRUE);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_isSequencerPlaying(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) return engine->isSequencerPlaying() ? JNI_TRUE : JNI_FALSE;
    return JNI_FALSE;
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

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setSequencerNote(JNIEnv *env, jobject thiz, jint track, jint step, jint note, jboolean active) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setSequencerNote(track, step, note, active == JNI_TRUE);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_isSequencerNoteActive(JNIEnv *env, jobject thiz, jint track, jint step, jint note) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) return engine->isSequencerNoteActive(track, step, note) ? JNI_TRUE : JNI_FALSE;
    return JNI_FALSE;
}

extern "C" JNIEXPORT jintArray JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_getSequencerActiveNotes(JNIEnv *env, jobject thiz, jint track, jint step) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (!engine) return nullptr;
    std::vector<int> notes;
    engine->getSequencerActiveNotes(track, step, notes);
    jintArray result = env->NewIntArray(notes.size());
    if (notes.size() > 0) env->SetIntArrayRegion(result, 0, notes.size(), notes.data());
    return result;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_isSequencerStepActive(JNIEnv *env, jobject thiz, jint track, jint step) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) return engine->isSequencerStepActive(track, step) ? JNI_TRUE : JNI_FALSE;
    return JNI_FALSE;
}

extern "C" JNIEXPORT jint JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_recordSequencerNote(JNIEnv *env, jobject thiz, jint track, jint note) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) return engine->recordSequencerNote(track, note);
    return 0;
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setSequencerNumSteps(JNIEnv *env, jobject thiz, jint steps) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setSequencerNumSteps(steps);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_handleRealTimeNoteOn(JNIEnv *env, jobject thiz, jint track, jint note) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->handleRealTimeNoteOn(track, note);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_handleRealTimeNoteOff(JNIEnv *env, jobject thiz, jint track, jint note) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->handleRealTimeNoteOff(track, note);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_clearSequencer(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->clearSequencer();
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_clearSequencerTrack(JNIEnv *env, jobject thiz, jint track) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->clearSequencerTrack(track);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_stepRecordNote(JNIEnv *env, jobject thiz, jint track, jint note) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->stepRecordNote(track, note);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_stepRecordRest(JNIEnv *env, jobject thiz, jint track) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->stepRecordRest(track);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_stepRecordHold(JNIEnv *env, jobject thiz, jint track) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->stepRecordHold(track);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_stepRecordBack(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->stepRecordBack();
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setSequencerStepDuration(JNIEnv *env, jobject thiz, jfloat division) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setSequencerStepDuration(division);
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
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->startAutomatedSampling(pad_index, duration);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackArpMode(JNIEnv *env, jobject thiz, jint track, jint mode_index) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackArpMode(track, static_cast<ArpMode>(mode_index));
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackArpDivision(JNIEnv *env, jobject thiz, jint track, jfloat division) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackArpDivision(track, division);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackArpOctaves(JNIEnv *env, jobject thiz, jint track, jint octaves) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackArpOctaves(track, octaves);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackChordMode(JNIEnv *env, jobject thiz, jint track, jint mode_index) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackChordMode(track, static_cast<ChordMode>(mode_index));
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setTrackChordInversion(JNIEnv *env, jobject thiz, jint track, jint inversion) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setTrackChordInversion(track, inversion);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_setPadPlaybackParams(JNIEnv *env, jobject thiz, jint pad_index, jint start, jint end, jboolean reverse) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->setPadPlaybackParams(pad_index, start, end, reverse);
}

extern "C" JNIEXPORT void JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_normalizePad(JNIEnv *env, jobject thiz, jint pad_index) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (engine) engine->normalizePad(pad_index);
}

extern "C" JNIEXPORT jint JNICALL
Java_ch_schmidlins_mini_1synth_audio_SynthManager_renderStereoSampleForTest(JNIEnv *env, jobject thiz, jfloatArray buffer) {
    std::lock_guard<std::mutex> lock(engineMutex);
    if (!engine) return 0;
    float left = 0, right = 0;
    engine->renderStereoSampleForTest(left, right);
    float samples[2] = {left, right};
    env->SetFloatArrayRegion(buffer, 0, 2, samples);
    return 2;
}
