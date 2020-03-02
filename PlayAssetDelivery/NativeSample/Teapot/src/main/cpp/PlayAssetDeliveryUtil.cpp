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
#include <algorithm>
#include <jni.h>
#include <third_party/stb/stb_image.h>
#include "PlayAssetDeliveryUtil.h"
#include "android_debug.h"

static char *selected_asset_pack = nullptr;

char *GetCurrentPackName() {
  return selected_asset_pack;
}

/**
 * Log info on top of screen
 */
void LogHeader(struct android_app *app, const char *str) {
  JNIEnv *jni;
  app->activity->vm->AttachCurrentThread(&jni, NULL);

  jclass clazz = jni->GetObjectClass(app->activity->clazz);
  jmethodID methodID = jni->GetMethodID(clazz, "logHeader", "([C)V");
  int len = strlen(str);
  jcharArray j_str_array = jni->NewCharArray(len + 1);
  jchar *j_str = new jchar[len];
  for (int i = 0; i <= len; i++) {
    j_str[i] = (jchar) str[i];
  }

  jni->SetCharArrayRegion(j_str_array, 0, len, j_str);
  jni->CallVoidMethod(app->activity->clazz, methodID, j_str_array);
  app->activity->vm->DetachCurrentThread();
  delete[] j_str;
}

/**
 * Log info on bottom of screen
 */
void LogInfo(struct android_app *app, const char *str) {
  JNIEnv *jni;
  app->activity->vm->AttachCurrentThread(&jni, NULL);

  jclass clazz = jni->GetObjectClass(app->activity->clazz);
  jmethodID methodID = jni->GetMethodID(clazz, "logInfo", "([C)V");
  int len = strlen(str);
  jcharArray j_str_array = jni->NewCharArray(len + 1);
  jchar *j_str = new jchar[len];
  for (int i = 0; i <= len; i++) {
    j_str[i] = (jchar) str[i];
  }

  jni->SetCharArrayRegion(j_str_array, 0, len, j_str);
  jni->CallVoidMethod(app->activity->clazz, methodID, j_str_array);
  app->activity->vm->DetachCurrentThread();
  delete[] j_str;
}

/**
 * Load Texture file into uint8_t*
 * @param assetManager is used to open texture files inside assets
 * @param assetName holds the asset name in assets folder of inside asset packs
 * @param packName holds the asset pack name
 * @param isUnderApk is used to determine the method for open texture file
 */
uint8_t *AssetReadTextureFile(AAssetManager *assetManager,
                              std::string &assetName, std::string &packName, bool isUnderApk,
                              int *imgWidth, int *imgHeight, int *channelCount) {
  if (!assetName.length())
    return nullptr;

  stbi_set_flip_vertically_on_load(1);

  if (isUnderApk) {  //install_time_pack
    AAsset
        *assetDescriptor = AAssetManager_open(assetManager, assetName.c_str(), AASSET_MODE_BUFFER);
    ASSERT(assetDescriptor, "%s does not exist in %s",
           assetName.c_str(), __FUNCTION__);
    size_t fileLength = AAsset_getLength(assetDescriptor);
    std::vector<uint8_t> buf(fileLength);
    AAsset_read(assetDescriptor, buf.data(), buf.size());

    return stbi_load_from_memory(
        buf.data(), buf.size(),
        imgWidth, imgHeight, channelCount, 4);

  } else {    //on_demand_pack & fast_follow_pack
    AssetPackLocation *location;
    AssetPackManager_getAssetPackLocation(packName.c_str(), &location);
    std::string path = std::string(AssetPackLocation_getAssetsPath(location));
    path += assetName;
    FILE *file = fopen(path.c_str(), "rb");
    assert(file != nullptr);
    return stbi_load_from_file(file, imgWidth, imgHeight, channelCount, 4);
  }
}

/**
 * Select the asset pack name.
 * Other function calls are all based on the selected pack name here.
 */
void SelectAssetPack(struct android_app *app, const char *pack_name) {
  char log[100] = "";
  sprintf(log, "Selected Asset Pack: %s", pack_name);
  LogInfo(app, log);
  selected_asset_pack = const_cast<char *>(pack_name);
}

/**
 * Init the AssetManager
 * Make sure to call this function first before others.
 */
void InitAssetManager(struct android_app *app) {
  AssetPackErrorCode error_code = AssetPackManager_init(app->activity->vm, app->activity->clazz);
  char log[100] = "";
  sprintf(log, "Finished initialize error_code=%d", error_code);
  LogInfo(app, log);
}

void DestroyAssetManager(struct android_app *app) {
  AssetPackManager_destroy();
  LogInfo(app, "Destroy AssetPackManager");
}

void RequestInfo(struct android_app *app) {
  std::vector<const char *> packs = {selected_asset_pack};
  AssetPackErrorCode error_code = AssetPackManager_requestInfo(&packs[0], packs.size());
  char log[100] = "";
  sprintf(log, "Finished Request pack info error_code=%d", error_code);
  LogInfo(app, log);
}

void RequestDownload(struct android_app *app) {
  std::vector<const char *> packs = {selected_asset_pack};
  AssetPackErrorCode error_code = AssetPackManager_requestDownload(&packs[0], packs.size());
  char log[100] = "";
  sprintf(log, "Finished Request download pack error_code=%d", error_code);
  LogInfo(app, log);

  ShowCellularDataConfirmation(app);
}

void PauseDownload(struct android_app *app) {
  AssetPackErrorCode error_code = AssetPackManager_onPause();
  char log[100] = "";
  sprintf(log, "Finished PauseDownload error_code=%d", error_code);
  LogInfo(app, log);
}

void ResumeDownload(struct android_app *app) {
  AssetPackErrorCode error_code = AssetPackManager_onResume();
  char log[100] = "";
  sprintf(log, "Finished ResumeDownload error_code=%d", error_code);
  LogInfo(app, log);
}

/**
 * Print the selected asset pack's location
 * Path can only be get for on_demand & fast_follow packs.
 * For install_time packs, storage_method will return ASSET_PACK_STORAGE_APK
 */
void PrintLocation(struct android_app *app) {
  AssetPackLocation *location;
  AssetPackErrorCode error_code =
      AssetPackManager_getAssetPackLocation(selected_asset_pack, &location);
  if (error_code == ASSET_PACK_NO_ERROR) {
    AssetPackStorageMethod storage_method =
        AssetPackLocation_getStorageMethod(location);
    const char *assets_path = AssetPackLocation_getAssetsPath(location);
    char log[1000] = "";
    sprintf(log, "PrintLocation, error_code=%d Location: pack=%s storage=%d path=%s",
            error_code, selected_asset_pack, storage_method, assets_path);
    LogInfo(app, log);
    AssetPackLocation_destroy(location);
  }
}

AssetPackDownloadStatus GetDownloadState() {
  AssetPackDownloadState *state;
  AssetPackManager_getDownloadState(selected_asset_pack, &state);
  AssetPackDownloadStatus status = AssetPackDownloadState_getStatus(state);
  AssetPackDownloadState_destroy(state);
  return status;
}

AssetPackDownloadStatus PrintDownloadState(struct android_app *app) {
  AssetPackDownloadState *state;
  AssetPackErrorCode error_code =
      AssetPackManager_getDownloadState(selected_asset_pack, &state);

  char log[1000] = "";
  sprintf(log,
          "DownloadState, error_code=%d pack=%s status=%d download=%llu total=%llu",
          error_code,
          selected_asset_pack,
          AssetPackDownloadState_getStatus(state),
          (long long) AssetPackDownloadState_getBytesDownloaded(state),
          (long long) AssetPackDownloadState_getTotalBytesToDownload(state));
  LogInfo(app, log);
  AssetPackDownloadStatus status = AssetPackDownloadState_getStatus(state);
  AssetPackDownloadState_destroy(state);
  return status;
}

/**
 *Shows a confirmation dialog to resume all Asset Pack downloads that are
 *currently in the ASSET_PACK_WAITING_FOR_WIFI state. If the user agrees to
 *the dialog prompt, Asset Packs are downloaded over cellular data.
 */
void ShowCellularDataConfirmation(struct android_app *app) {
  AssetPackErrorCode error_code1 =
      AssetPackManager_showCellularDataConfirmation(app->activity->clazz);
  ShowCellularDataConfirmationStatus status;
  AssetPackErrorCode error_code2 =
      AssetPackManager_getShowCellularDataConfirmationStatus(&status);
  char log[1000] = "";
  sprintf(log,
          "ShowCellularDataConfirmation, error_code=%d; Cellular data confirmation status=%d, error_code=%d",
          error_code1,
          status,
          error_code2);
  LogInfo(app, log);
}