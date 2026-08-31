#include "ProjectManager.h"
#include <fstream>
#include <nlohmann/json.hpp>
#include <android/log.h>

using json = nlohmann::json;

#define TAG "ProjectManager"

bool ProjectManager::saveProject(const std::string& directory,
                                 const std::array<Track, 4>& tracks,
                                 const MidiSequencer& sequencer,
                                 const std::vector<std::vector<float>>& padBuffers,
                                 const std::vector<float>& padPannings,
                                 float bpm) {
    json j;
    j["version"] = 2; // Incremented for multi-track
    j["bpm"] = bpm;

    for (int t = 0; t < 4; ++t) {
        const auto& params = tracks[t].params;
        json tj;
        tj["engine"]["waveform"] = static_cast<int>(params.waveform);
        tj["engine"]["attack"] = params.attack;
        tj["engine"]["decay"] = params.decay;
        tj["engine"]["sustain"] = params.sustain;
        tj["engine"]["release"] = params.release;
        tj["engine"]["masterVolume"] = params.masterVolume;
        tj["engine"]["lfoRate"] = params.lfoRate;
        tj["engine"]["lfoDepth"] = params.lfoDepth;
        tj["engine"]["lfoWaveform"] = static_cast<int>(params.lfoWaveform);
        tj["engine"]["lfoMatrix"] = { params.lfoMatrix[0], params.lfoMatrix[1], params.lfoMatrix[2], params.lfoMatrix[3] };
        tj["engine"]["lfoSync"] = params.lfoSync;
        tj["engine"]["lfoSyncDivision"] = params.lfoSyncDivision;
        tj["engine"]["filterCutoff"] = params.filterCutoff;
        tj["engine"]["filterResonance"] = params.filterResonance;
        tj["engine"]["panning"] = params.panning;
        tj["engine"]["unisonCount"] = params.unisonCount;
        tj["engine"]["unisonDetune"] = params.unisonDetune;
        tj["engine"]["unisonSpread"] = params.unisonSpread;
        tj["engine"]["morph"] = params.morph;
        tj["engine"]["phaseDistortion"] = params.phaseDistortion;

        // Sequencer grid for this track
        tj["sequencer"]["stepDivision"] = sequencer.getStepDivision();
        for (int s = 0; s < 16; ++s) {
            std::vector<int> activeNotes;
            for (int n = 0; n < 128; ++n) {
                if (sequencer.getNote(t, s, n)) activeNotes.push_back(n);
            }
            tj["sequencer"]["steps"][s] = activeNotes;
        }
        j["tracks"].push_back(tj);
    }

    // Pads (Shared across tracks for now)
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
                                 std::array<Track, 4>& outTracks,
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

    outSequencer.clear();
    if (j.contains("tracks")) {
        for (int t = 0; t < 4 && t < j["tracks"].size(); ++t) {
            auto tj = j["tracks"][t];
            auto e = tj["engine"];
            auto& params = outTracks[t].params;
            params.waveform = static_cast<Waveform>(e["waveform"]);
            params.attack = e["attack"];
            params.decay = e["decay"];
            params.sustain = e["sustain"];
            params.release = e["release"];
            params.lfoRate = e["lfoRate"];
            params.lfoDepth = e["lfoDepth"];
            params.lfoWaveform = static_cast<Waveform>(e["lfoWaveform"]);
            if (e.contains("lfoMatrix")) {
                for (int i = 0; i < 4; ++i) params.lfoMatrix[i] = e["lfoMatrix"][i];
            } else {
                for (int i = 0; i < 4; ++i) params.lfoMatrix[i] = 0.0f;
                params.lfoMatrix[0] = 1.0f; // Default Pitch
            }
            params.lfoSync = e.value("lfoSync", false);
            params.lfoSyncDivision = e.value("lfoSyncDivision", 1.0f);
            params.filterCutoff = e["filterCutoff"];
            params.filterResonance = e["filterResonance"];
            params.panning = e.value("panning", 0.0f);
            params.unisonCount = e.value("unisonCount", 1);
            params.unisonDetune = e.value("unisonDetune", 0.0f);
            params.morph = e.value("morph", 0.0f);
            params.phaseDistortion = e.value("phaseDistortion", 0.0f);

            auto steps = tj["sequencer"]["steps"];
            for (int s = 0; s < 16; ++s) {
                if (steps.size() > s) {
                    for (int note : steps[s]) {
                        outSequencer.setNote(t, s, note, true);
                    }
                }
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
            if (padData.contains("panning")) outPadPannings[padIndex] = padData["panning"];
            if (padData.contains("file")) {
                std::string padPath = directory + "/" + padData["file"].get<std::string>();
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
