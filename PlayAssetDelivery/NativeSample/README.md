Teapots
======
This sample uses the new Play Asset Delivery Native Library to demonstrate how to use
AssetPackManager for managing game asset from Google Play.
The sample shows the three way of handle asset delivery through Google Play:
    install time, on demand and fast follow.
Refer to Play Asset Delivery documentation for more details.(https://developer.android.com/guide/playcore/asset-delivery/integrate-native)

Pre-requisites
--------------
- Android Studio 4.0+ with [NDK](https://developer.android.com/ndk/) bundle.
- Bundletool 0.13.0+ (https://github.com/google/bundletool/releases)
- play-core-native-sdk (https://developer.android.com/guide/playcore#native)


Getting Started
---------------
* Download and unzip "play-core-native-sdk" to $project/NativeSample folder
* Build an Android App Bundle
  
  ```
  $ ./gradlew Teapot:bundleRelease
  ...
  ...
  $ ls Teapot/build/outputs/bundle/release
  Teapot-release.aab
  ```

* Local Testing

    ```
    bundletool build-apks --bundle=Teapot/build/outputs/bundle/release/Teapot-release.aab --output=output.apks --local-testing
    (e.g. java -jar ./bundletool-all-0.13.0.jar build-apks --bundle=Teapot/build/outputs/bundle/release/Teapot-release.aab --output=output.apks --local-testing)

    bundletool install-apks --apks=output.apks
    ```

* Sign the final app bundle
  
  ```
  $ jarsigner -keystore KEYSTORE Teapot-release.aab KEY_ALIAS
  ```

* Publish Teapot-release.aab on Play Console.

After launch the sample, double tap the right bottom corner to switch textures.
The first 3 textures are in the upfront packs.
For on-demand pack, select On Demand Pack-->Request pack info--->Request Download
    After download is finished, the texture will be switched to textures from on-demand pack.
Same for fast-follow pack.
    If the sample is downloading from Play, fast follow progress can be seen after open the app.


License
-------
Copyright 2020 Google, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
