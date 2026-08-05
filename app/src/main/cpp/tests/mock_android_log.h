#ifndef MOCK_ANDROID_LOG_H
#define MOCK_ANDROID_LOG_H

#define ANDROID_LOG_INFO 4
#define ANDROID_LOG_ERROR 6

#include <iostream>

#define __android_log_print(level, tag, fmt, ...) \
    std::cout << "[" << tag << "] " << level << ": " << fmt << std::endl

#endif // MOCK_ANDROID_LOG_H
