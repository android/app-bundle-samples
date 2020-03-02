#
# Copyright (C) 2020 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

cmake_minimum_required(VERSION 3.6)
project(TexturedTeapotNativeActivity LANGUAGES C CXX)

# set up common compile options
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -Wall -Werror -fno-exceptions -frtti")

# Export ANativeActivity_onCreate() per https://github.com/android-ndk/ndk/issues/381.
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -u ANativeActivity_onCreate")


get_filename_component(PLAY_CORE_NATIVE_SDK_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../play-core-native-sdk ABSOLUTE)
include(${PLAY_CORE_NATIVE_SDK_DIR}/playcore.cmake)
add_playcore_static_library()

get_filename_component(commonDir ${CMAKE_CURRENT_SOURCE_DIR}/../../../../common ABSOLUTE)
if ((NOT EXISTS ${commonDir}/third_party/stb) OR
(NOT EXISTS ${commonDir}/third_party/stb/stb_image.h))
    execute_process(COMMAND git clone
            https://github.com/nothings/stb.git
            stb
            WORKING_DIRECTORY ${commonDir}/third_party)
endif ()

# build the ndk-helper library
get_filename_component(ndkHelperSrc ${commonDir}/ndk_helper ABSOLUTE)
add_subdirectory(${ndkHelperSrc}
        ${commonDir}/ndkHelperBin/${CMAKE_BUILD_TYPE}/${ANDROID_ABI})

# now build app's shared lib
add_library(${PROJECT_NAME}
        SHARED
        TeapotNativeActivity.cpp
        TeapotRenderer.cpp
        TexturedTeapotRender.cpp
        Texture.cpp
        PlayAssetDeliveryUtil.cpp
        )
set_target_properties(${PROJECT_NAME}
        PROPERTIES
        CXX_STANDARD 11
        CXX_STANDARD_REQUIRED YES
        CXX_EXTENSIONS NO
        )
target_include_directories(
        ${PROJECT_NAME} PRIVATE
        ${commonDir}
        ${PLAY_CORE_NATIVE_SDK_DIR}/include)

target_compile_options(${PROJECT_NAME} PRIVATE -Wno-unused-function)
target_link_libraries(${PROJECT_NAME} PRIVATE NdkHelper playcore)
