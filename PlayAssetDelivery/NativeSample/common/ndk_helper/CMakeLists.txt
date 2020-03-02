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

# build native_app_glue as a static lib
cmake_minimum_required(VERSION 3.4.1)

include(AndroidNdkModules)
android_ndk_import_module_native_app_glue()

add_library(NdkHelper
  STATIC
    gestureDetector.cpp
    gl3stub.cpp
    GLContext.cpp
    interpolator.cpp
    JNIHelper.cpp
    perfMonitor.cpp
    sensorManager.cpp
    shader.cpp
    tapCamera.cpp
    vecmath.cpp
)
set_target_properties(NdkHelper
  PROPERTIES
    CXX_STANDARD 11
    CXX_STANDARD_REQUIRED YES
    CXX_EXTENSIONS NO
    INTERFACE_INCLUDE_DIRECTORIES $<TARGET_PROPERTY:NdkHelper,INCLUDE_DIRECTORIES>
)
target_include_directories(NdkHelper
  PUBLIC
    ${CMAKE_CURRENT_SOURCE_DIR}
)

target_link_libraries(NdkHelper
  PUBLIC
    native_app_glue
    GLESv2
    EGL
    log
    android
    atomic
)
