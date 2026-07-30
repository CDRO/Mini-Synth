cmake_minimum_required(VERSION 3.22.1)
project("mini_synth")

find_package(oboe REQUIRED CONFIG)

add_library(mini_synth SHARED
        native-lib.cpp
        AudioEngine.cpp
        Oscillator.cpp
        Voice.cpp
        VoiceManager.cpp
)

target_link_libraries(mini_synth
        oboe::oboe
        log
)
