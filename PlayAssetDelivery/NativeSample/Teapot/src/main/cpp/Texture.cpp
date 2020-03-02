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

#include "Texture.h"
#include "PlayAssetDeliveryUtil.h"
#include <GLES3/gl32.h>
#define STB_IMAGE_IMPLEMENTATION
#include <third_party/stb/stb_image.h>
#define MODULE_NAME "Teapot::Texture"
#include "android_debug.h"

class Texture2d : public Texture {
 protected:
  GLuint texId_ = GL_INVALID_VALUE;
  bool activated_ = false;
 public:
  virtual ~Texture2d();
  // Implement just one texture
  Texture2d(std::string &texFile,
            AAssetManager *assetManager,
            std::string &packName,
            bool isUnderApk);

  virtual bool GetActiveSamplerInfo(std::vector<std::string> &names,
                                    std::vector<GLint> &units);
  virtual bool Activate(void);
  virtual GLuint GetTexType();
  virtual GLuint GetTexId();
};

/**
 * Capability debug string
 */
static const std::string
    supportedTextureTypes = "GL_TEXTURE_2D(0x0DE1)";

/**
 * Interface implementations
 */
Texture::Texture() {}
Texture::~Texture() {}
/**
 * Create Texture Object
 * @param texFiles holds the texture file name under APK's assets
 * @param assetManager is used to open texture files inside assets
 * @param isUnderApk is used to determine the method for open texture file
 */
Texture *Texture::Create(std::string &texFile,
                         AAssetManager *assetManager,
                         std::string &packName,
                         bool isUnderApk) {
  return dynamic_cast<Texture *>(new Texture2d(texFile, assetManager, packName, isUnderApk));
}

void Texture::Delete(Texture *obj) {
  if (obj == nullptr) {
    ASSERT(false, "NULL pointer to Texture::Delete() function");
    return;
  }

  Texture2d *d2Instance = dynamic_cast<Texture2d *>(obj);
  if (d2Instance) {
    delete d2Instance;
  } else {
    ASSERT(false, "Unknown obj type to %s", __FUNCTION__);
  }
}

/**
 * Texture2D implementation
 */
Texture2d::Texture2d(std::string &texFile,
                     AAssetManager *assetManager,
                     std::string &packName,
                     bool isUnderApk) {
  if (!assetManager) {
    LOGE("AssetManager to Texture2D() could not be null!!!");
    assert(false);
    return;
  }

  int32_t imgWidth, imgHeight, channelCount;
  std::string texName(texFile);

  glGenTextures(1, &texId_);
  glBindTexture(GL_TEXTURE_2D, texId_);

  if (texId_ == GL_INVALID_VALUE) {
    assert(false);
    return;
  }

  // tga/bmp files are saved as vertical mirror images ( at least more than half ).
  stbi_set_flip_vertically_on_load(1);
  uint8_t *imageBits;

  imageBits =
      AssetReadTextureFile(assetManager,
                           texName,
                           packName,
                           isUnderApk,
                           &imgWidth,
                           &imgHeight,
                           &channelCount);

  glTexImage2D(GL_TEXTURE_2D, 0,  // mip level
               GL_RGBA,
               imgWidth, imgHeight,
               0,                // border color
               GL_RGBA, GL_UNSIGNED_BYTE, imageBits);

  glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
  glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

  glActiveTexture(GL_TEXTURE0);

  stbi_image_free(imageBits);
}

Texture2d::~Texture2d() {
  if (texId_ != GL_INVALID_VALUE) {
    glDeleteTextures(1, &texId_);
    texId_ = GL_INVALID_VALUE;
  }
  activated_ = false;
}

/**
  Return used sampler names and units
      so application could configure shader's sampler uniform(s).
  Just used one sampler at unit 0 with "samplerObj" as its name.
 */

bool Texture2d::GetActiveSamplerInfo(std::vector<std::string> &names,
                                     std::vector<GLint> &units) {
  names.clear();
  names.push_back(std::string("samplerObj"));
  units.clear();
  units.push_back(0);

  return true;
}

bool Texture2d::Activate(void) {
  glBindTexture(texId_, GL_TEXTURE0);
  glActiveTexture(GL_TEXTURE0 + 0);
  activated_ = true;
  return true;
}

GLuint Texture2d::GetTexType() {
  return GL_TEXTURE_2D;
}

GLuint Texture2d::GetTexId() {
  return texId_;
}
