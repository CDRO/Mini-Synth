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

# Testing
if (ANDROID_PLATFORM)
    # No standalone tests on Android usually, they run through JNI
else()
    # Host side tests could go here
endif()
