#include "ProjectManager.h"
#include <fstream>
#include <nlohmann/json.hpp>
#include <android/log.h>

using json = nlohmann::json;

#define TAG "ProjectManager"

bool ProjectManager::saveProject(const std::string& directory,
                                 const EngineParams& params,
                                 const MidiSequencer& sequencer,
                                 const std::vector<std::vector<float>>& padBuffers,
                                 const std::vector<float>& padPannings,
                                 float bpm) {
    json j;
    j["version"] = 1;
    j["bpm"] = bpm;

    // Engine Params
    j["engine"]["waveform"] = static_cast<int>(params.waveform);
    j["engine"]["attack"] = params.attack;
    j["engine"]["decay"] = params.decay;
    j["engine"]["sustain"] = params.sustain;
    j["engine"]["release"] = params.release;
    j["engine"]["masterVolume"] = params.masterVolume;
    j["engine"]["lfoRate"] = params.lfoRate;
    j["engine"]["lfoDepth"] = params.lfoDepth;
    j["engine"]["lfoWaveform"] = static_cast<int>(params.lfoWaveform);
    j["engine"]["lfoTarget"] = static_cast<int>(params.lfoTarget);
    j["engine"]["filterCutoff"] = params.filterCutoff;
    j["engine"]["filterResonance"] = params.filterResonance;
    j["engine"]["isPolyphonic"] = params.isPolyphonic;
    j["engine"]["panning"] = params.panning;
    j["engine"]["unisonCount"] = params.unisonCount;
    j["engine"]["unisonDetune"] = params.unisonDetune;
    j["engine"]["unisonSpread"] = params.unisonSpread;
    j["engine"]["morph"] = params.morph;

    // Sequencer
    j["sequencer"]["stepDivision"] = sequencer.getStepDivision();
    for (int s = 0; s < 16; ++s) {
        std::vector<int> activeNotes;
        for (int n = 0; n < 128; ++n) {
            if (sequencer.getNote(s, n)) activeNotes.push_back(n);
        }
        j["sequencer"]["steps"][s] = activeNotes;
    }

    // Pads
    for (size_t i = 0; i < padBuffers.size(); ++i) {
        if (!padBuffers[i].empty()) {
            std::string padPath = directory + "/pad_" + std::to_string(i) + ".raw";
            std::ofstream padFile(padPath, std::ios::binary);
            if (padFile.is_open()) {
                padFile.write(reinterpret_cast<const char*>(padBuffers[i].data()),
                              padBuffers[i].size() * sizeof(float));
                j["pads"][std::to_string(i)]["file"] = "pad_" + std::to_string(i) + ".raw";
            }
        }
        if (i < padPannings.size()) {
            j["pads"][std::to_string(i)]["panning"] = padPannings[i];
        }
    }

    std::string configPath = directory + "/project.json";
    std::ofstream configFile(configPath);
    if (!configFile.is_open()) return false;
    configFile << j.dump(4);

    return true;
}

bool ProjectManager::loadProject(const std::string& directory,
                                 EngineParams& outParams,
                                 MidiSequencer& outSequencer,
                                 std::vector<std::vector<float>>& outPadBuffers,
                                 std::vector<float>& outPadPannings,
                                 float& outBpm) {
    std::string configPath = directory + "/project.json";
    std::ifstream configFile(configPath);
    if (!configFile.is_open()) return false;

    json j;
    configFile >> j;

    if (j.contains("bpm")) outBpm = j["bpm"];

    // Engine
    auto e = j["engine"];
    outParams.waveform = static_cast<Waveform>(e["waveform"]);
    outParams.attack = e["attack"];
    outParams.decay = e["decay"];
    outParams.sustain = e["sustain"];
    outParams.release = e["release"];
    outParams.masterVolume = e["masterVolume"];
    outParams.lfoRate = e["lfoRate"];
    outParams.lfoDepth = e["lfoDepth"];
    outParams.lfoWaveform = static_cast<Waveform>(e["lfoWaveform"]);
    outParams.lfoTarget = static_cast<LfoTarget>(e["lfoTarget"]);
    outParams.filterCutoff = e["filterCutoff"];
    outParams.filterResonance = e["filterResonance"];
    outParams.isPolyphonic = e["isPolyphonic"];
    if (e.contains("panning")) outParams.panning = e["panning"];
    else outParams.panning = 0.0f;

    if (e.contains("unisonCount")) outParams.unisonCount = e["unisonCount"];
    else outParams.unisonCount = 1;

    if (e.contains("unisonDetune")) outParams.unisonDetune = e["unisonDetune"];
    else outParams.unisonDetune = 0.0f;

    if (e.contains("unisonSpread")) outParams.unisonSpread = e["unisonSpread"];
    else outParams.unisonSpread = 0.0f;

    if (e.contains("morph")) outParams.morph = e["morph"];
    else outParams.morph = 0.0f;

    // Sequencer
    outSequencer.clear();
    outSequencer.setStepDuration(j["sequencer"]["stepDivision"]);
    auto steps = j["sequencer"]["steps"];
    for (int s = 0; s < 16; ++s) {
        if (steps.size() > s) {
            for (int note : steps[s]) {
                outSequencer.setNote(s, note, true);
            }
        }
    }

    // Pads
    outPadBuffers.assign(256, std::vector<float>());
    outPadPannings.assign(256, 0.0f);
    if (j.contains("pads")) {
        for (auto it = j["pads"].begin(); it != j["pads"].end(); ++it) {
            int padIndex = std::stoi(it.key());
            auto padData = it.value();

            if (padData.contains("panning")) {
                outPadPannings[padIndex] = padData["panning"];
            }

            if (padData.contains("file")) {
                std::string padFileRelative = padData["file"];
                std::string padPath = directory + "/" + padFileRelative;

                std::ifstream padFile(padPath, std::ios::binary | std::ios::ate);
                if (padFile.is_open()) {
                    std::streamsize size = padFile.tellg();
                    padFile.seekg(0, std::ios::beg);
                    outPadBuffers[padIndex].resize(size / sizeof(float));
                    padFile.read(reinterpret_cast<char*>(outPadBuffers[padIndex].data()), size);
                }
            }
        }
    }

    return true;
}
