/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef TEAPOTS_TEXTURE_H
#define TEAPOTS_TEXTURE_H

#include <EGL/egl.h>
#include <GLES/gl.h>
#include <android/asset_manager.h>
#include <string>
#include <vector>

/**
 *  class Texture
 *    adding texture into teapot
 *     - oad image in assets/Textures
 *     - enable texture units
 *     - report samplers needed inside shader
 *  Functionality wise:
 *     - one texture
 *     - one sampler
 *     - texture unit 0, sampler unit 0
 */
class Texture {
 protected:
  Texture();
  virtual ~Texture();

 public:
/**
 * Create Texture Object
 * @param texFiles holds the texture file name under APK's assets
 * @param assetManager is used to open texture files inside assets
 * @param isUnderApk is used to determine the method for open texture file
 */
  static Texture *Create(std::string &texFile,
                         AAssetManager *assetManager,
                         std::string &packName,
                         bool isUnderApk);
  static void Delete(Texture *obj);

  virtual bool GetActiveSamplerInfo(std::vector<std::string> &names,
                                    std::vector<GLint> &units) = 0;
  virtual bool Activate(void) = 0;
  virtual GLuint GetTexType() = 0;
  virtual GLuint GetTexId() = 0;

};
#endif //TEAPOTS_TEXTURE_H
