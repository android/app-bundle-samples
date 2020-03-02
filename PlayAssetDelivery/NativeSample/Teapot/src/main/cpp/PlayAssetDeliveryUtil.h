/*
 * Copyright (C) 2020 The Android Open Source Project
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
 *
 */
#ifndef __ASSET__UTIL_H__
#define __ASSET__UTIL_H__

#include <string>
#include <vector>
#include <android_native_app_glue.h>
#include <android/asset_manager.h>
#include <play/asset_pack.h>

uint8_t *AssetReadTextureFile(AAssetManager *assetManager,
                              std::string &assetName, std::string &packName, bool isUnderApk,
                              int *imgWidth, int *imgHeight, int *channelCount);

void SelectAssetPack(struct android_app *app, const char *pack_name);
char *GetCurrentPackName();

void InitAssetManager(struct android_app *app);
void DestroyAssetManager(struct android_app *app);
void RequestInfo(struct android_app *app);
void RequestDownload(struct android_app *app);
void PauseDownload(struct android_app *app);
void ResumeDownload(struct android_app *app);
void PrintLocation(struct android_app *app);
AssetPackDownloadStatus GetDownloadState();
AssetPackDownloadStatus PrintDownloadState(struct android_app *app);
void ShowCellularDataConfirmation(struct android_app *app);
void LogHeader(struct android_app *app, const char *str);

#endif // __ASSET__UTIL_H__
