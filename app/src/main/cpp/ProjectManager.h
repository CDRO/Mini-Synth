#ifndef MINI_SYNTH_PROJECTMANAGER_H
#define MINI_SYNTH_PROJECTMANAGER_H

#include <string>
#include <vector>
#include "VoiceManager.h"
#include "MidiSequencer.h"

class ProjectManager {
public:
    static bool saveProject(const std::string& directory,
                           const EngineParams& engineParams,
                           const MidiSequencer& sequencer,
                           const std::vector<std::vector<float>>& padBuffers,
                           float bpm);

    static bool loadProject(const std::string& directory,
                           EngineParams& outEngineParams,
                           MidiSequencer& outSequencer,
                           std::vector<std::vector<float>>& outPadBuffers,
                           float& outBpm);
};

#endif //MINI_SYNTH_PROJECTMANAGER_H
