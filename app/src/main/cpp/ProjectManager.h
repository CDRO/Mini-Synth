#ifndef MINI_SYNTH_PROJECTMANAGER_H
#define MINI_SYNTH_PROJECTMANAGER_H

#include <string>
#include <vector>
#include "VoiceManager.h"
#include "MidiSequencer.h"
#include "Track.h"

class ProjectManager {
public:
    static bool saveProject(const std::string& directory,
                           const std::array<Track, 4>& tracks,
                           const MidiSequencer& sequencer,
                           const std::vector<std::vector<float>>& padBuffers,
                           const std::vector<float>& padPannings,
                           float bpm);

    static bool loadProject(const std::string& directory,
                           std::array<Track, 4>& outTracks,
                           MidiSequencer& outSequencer,
                           std::vector<std::vector<float>>& outPadBuffers,
                           std::vector<float>& outPadPannings,
                           float& outBpm);
};

#endif //MINI_SYNTH_PROJECTMANAGER_H
