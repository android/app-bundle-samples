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

#include "TexturedTeapotRender.h"

/**
 * Texture Coordinators for 2D texture:
 *    they are declared in file model file teapot.inl with tiles
 *    for front and back teapot faces. If you do not want see
 *    the tiles but would like to see the stretched version, simply
 *    divide the texCoord by 2. Macro TILED_TEXTURE is for this purpose.
 *
 * teapot.inl file already included in TeapotRenderer.cpp, we directly
 * use! -- nice
 */
#define TILED_TEXTURE 0

extern float teapotTexCoords[];
constexpr int32_t kCoordElementCount = (TILED_TEXTURE ? 3 : 2);

/**
 * Constructor: all work is done inside Init() function.
 *              nothing to do here
 */
TexturedTeapotRender::TexturedTeapotRender()
    : textureIndex(1),
      maxIndex(3),
      renderPack("install_time_pack") {
  renderTextures.push_back("Textures/1.jpeg");
  renderTextures.push_back("Textures/1.jpeg");
  renderTextures.push_back("Textures/2.jpeg");
  renderTextures.push_back("Textures/3.jpeg");
  renderTextures.push_back("/Textures/4.jpeg");
  renderTextures.push_back("/Textures/5.jpeg");
  renderTextures.push_back("/Textures/6.jpeg");
  renderTextures.push_back("/Textures/7.jpeg");
  renderTextures.push_back("/Textures/8.jpeg");
  renderTextures.push_back("/Textures/9.jpeg");
}

/**
 * Destructor:
 *     let Unload() do the work, which should also trigger
 *     TeapotRenderer's Unload() function
 */
TexturedTeapotRender::~TexturedTeapotRender() {
  Unload();
};

/**
 * Init: Initialize the GL with needed data. We add on the things
 * needed for textures
 *  - load image data into generated glBuffers
 *  - configure samplerObj in fragment shader
 * @param assetMgr android assetManager from java side
 */

void TexturedTeapotRender::Init(android_app *app) {
  // initialize the basic things from TeapotRenderer, no change
  TeapotRenderer::Init();
  app_ = app;

  // do Texture related initializations...
  glGenBuffers(1, &texVbo_);
  assert(texVbo_ != GL_INVALID_VALUE);

  /*
   * Loading Texture coord directly from data declared in model file
   *   teapot.inl
   * which is 3 floats/vertex.
   */
  glBindBuffer(GL_ARRAY_BUFFER, texVbo_);

#if (TILED_TEXTURE)
  glBufferData(GL_ARRAY_BUFFER,
           kCoordElementCount * sizeof(float) * num_vertices_,
           teapotTexCoords, GL_STATIC_DRAW);
#else
  std::vector<float> coords;
  for (int32_t idx = 0; idx < num_vertices_; idx++) {
    coords.push_back(teapotTexCoords[3 * idx] / 2);
    coords.push_back(teapotTexCoords[3 * idx + 1] / 2);
  }
  glBufferData(GL_ARRAY_BUFFER,
               kCoordElementCount * sizeof(float) * num_vertices_,
               coords.data(), GL_STATIC_DRAW);
#endif
  glVertexAttribPointer(ATTRIB_UV, 2, GL_FLOAT, GL_FALSE,
                        kCoordElementCount * sizeof(float),
                        BUFFER_OFFSET(0));
  glEnableVertexAttribArray(ATTRIB_UV);

  glBindBuffer(GL_ARRAY_BUFFER, 0);

  int index = textureIndex;
  bool isUnderApk = true;
  if (renderPack.compare("on_demand_pack") == 0) {
    index += 3;
    isUnderApk = false;
  } else if (renderPack.compare("fast_follow_pack") == 0) {
    index += 6;
    isUnderApk = false;
  }
  renderTextures[0] = renderTextures[index];
  textureIndex++;
  if (textureIndex > maxIndex) {
    textureIndex = 1;
  }
  renderInfo = "Texture::" + renderPack + "/" + renderTextures[0];
  texObj_ =
      Texture::Create(renderTextures[0], app_->activity->assetManager, renderPack, isUnderApk);
  assert(texObj_);

  std::vector<std::string> samplers;
  std::vector<GLint> units;
  texObj_->GetActiveSamplerInfo(samplers, units);
  for (size_t idx = 0; idx < samplers.size(); idx++) {
    GLint sampler = glGetUniformLocation(shader_param_.program_,
                                         samplers[idx].c_str());
    glUniform1i(sampler, units[idx]);
  }

  texObj_->Activate();
}

/**
 * Render() function:
 *   enable states for rendering and reader a frame.
 *   For Texture, simply inform GL to stream texture coord from _texVbo
 */
void TexturedTeapotRender::Render() {
  TeapotRenderer::Render();
  UpdateButton();
}

/**
 * Unload()
 *    clean-up function. May get called from destructor too
 */
void TexturedTeapotRender::Unload() {
  TeapotRenderer::Unload();
  if (texVbo_ != GL_INVALID_VALUE) {
    glDeleteBuffers(1, &texVbo_);
    texVbo_ = GL_INVALID_VALUE;
  }
  if (texObj_) {
    Texture::Delete(texObj_);
    texObj_ = nullptr;
  }
}

std::string TexturedTeapotRender::GetRenderInfo() {
  return renderInfo;
}

void TexturedTeapotRender::UpdateButton() {
  JNIEnv *jni;
  app_->activity->vm->AttachCurrentThread(&jni, NULL);

  // Default class retrieval
  jclass clazz = jni->GetObjectClass(app_->activity->clazz);
  jmethodID methodID = jni->GetMethodID(clazz, "updateButtons", "()I");
  jint val = jni->CallIntMethod(app_->activity->clazz, methodID);
  int buttonCode = (int) val;
  app_->activity->vm->DetachCurrentThread();
  switch (buttonCode) {
    case 0:
      break;
    case 1:SelectAssetPack(app_, "install_time_pack");
      break;
    case 2:SelectAssetPack(app_, "on_demand_pack");
      break;
    case 3:SelectAssetPack(app_, "fast_follow_pack");
      break;
    case 4:RequestInfo(app_);
      break;
    case 5:RequestDownload(app_);
      PrintDownloadState(app_);
      break;
    case 6:PauseDownload(app_);
      break;
    case 7:ResumeDownload(app_);
      break;
    case 8:PrintLocation(app_);
      break;
    case 9:ShowCellularDataConfirmation(app_);
      break;
    default:LOGW("Wrong button code");
      break;
  }

  if (GetDownloadState() == ASSET_PACK_DOWNLOAD_COMPLETED
      || strcmp(GetCurrentPackName(), "install_time_pack") == 0) {
    renderPack = GetCurrentPackName();
  }

  return;
}

