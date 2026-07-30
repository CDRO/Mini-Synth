#ifndef MINI_SYNTH_AUDIOENGINE_H
#define MINI_SYNTH_AUDIOENGINE_H

#include <oboe/Oboe.h>
#include "VoiceManager.h"

class AudioEngine : public oboe::AudioStreamDataCallback {
public:
    AudioEngine();
    ~AudioEngine();

    void start();
    void stop();

    void noteOn(int midiNote, float velocity) { mVoiceManager.noteOn(midiNote + (mOctaveShift * 12), velocity); }
    void noteOff(int midiNote) { mVoiceManager.noteOff(midiNote + (mOctaveShift * 12)); }
    void setPolyphonic(bool isPolyphonic) { mVoiceManager.setPolyphonic(isPolyphonic); }
    void setWaveform(Waveform waveform) { mVoiceManager.setWaveform(waveform); }
    void setOctaveShift(int shift) { mOctaveShift = shift; }

    float renderSample() { return mVoiceManager.nextSample(); }

    oboe::DataCallbackResult onAudioReady(
            oboe::AudioStream *audioStream,
            void *audioData,
            int32_t numFrames) override;

private:
    std::shared_ptr<oboe::AudioStream> mStream;
    VoiceManager mVoiceManager;
    int mOctaveShift = 0;
};

#endif //MINI_SYNTH_AUDIOENGINE_H
