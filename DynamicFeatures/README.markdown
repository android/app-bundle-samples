# PlayCore API sample

This sample demonstrates usage of the PlayCore API.

Read more at http://g.co/androidappbundle

## Introduction

The sample contains several modules.

`app` -> Contains the base application which always will be installed on device.

The `MainActivity` class demonstrates how to use the API to load and launch features.

The `BaseSplitActivity` abstract class implements the required `SplitCompat.Install()` call in the `attachBaseContext` method. This allow to launch an activity from a freshly downloaded dynamic module without having the restart the application.

`features/*` -> Contains features which can be downloaded on demand using the PlayCore API.
`instant/*` -> Contains instant enabled features which can be downloaded using the PlayCore API or via Url.

Each feature as some distinctly unique characteristics.

- `features/assets` -> Feature containing only assets.
- `features/kotlin` -> Feature written in Kotlin.
- `features/java` -> Feature written in Java.
- `features/maxSdk` -> Conditionally delivered feature based on max sdk version
- `features/native` -> Feature written in Kotlin using JNI.
- `instant/split` -> Instant Feature without an URL route. Loaded using SplitInstall API
- `instant/url` -> Instant Feature with a URL route

The `AndroidManifest` files in each feature show how to declare a feature module as part of a dynamic app. Any module with the instant attribute is instant enabled. In this sample these can be found in the `instant/` folder:

```
  <dist:module
    dist:instant="true"/>
```

## Screenshots

<img src="screenshots/main.png" width="30%" />


## Getting Started

Clone this repository, enter the top level directory and run <code>./gradlew tasks</code> to get an overview of all the tasks available for this project.

## Testing dynamic delivery

To test dynamic delivery with this sample, you can follow any of the below steps:
* Upload the aab to the Google Play Store's internal testing channel. Before uploading, make sure to change the `applicationId` in `app/build.gradle`.
* Locally test the dynamic delivery by using [FakeSplitInstallManager](https://developer.android.com/guide/app-bundle/test/testing-fakesplitinstallmanager) and [bundletool](https://developer.android.com/studio/command-line/bundletool>bundletool):
    1. Build a set of APKs: `bundletool build-apks --local-testing --bundle=<path_to_aab>  --output=<path_to_apks>`. _Make sure to include  --local-testing flag_
    2. Connect to the device/emulator
    3. Deploy app to the device: `bundletool install-apks --apks=<path_to_apks>`

## Running instrumentation tests

In order to run instrumentation tests, parallel builds have to be turned off at the moment.
This means you can run them via `./gradlew connectedAndroidTest --no-parallel`.

Tooling support for this is being worked on – currently it's not possible to run instrumentation tests for dynamic-feature modules from Android Studio directly. _Use the command line instead._

## Support

- Stack Overflow: http://stackoverflow.com/questions/tagged/android

If you've found an error *in this sample*, please file an issue:

https://github.com/android/app-bundle/issues

Patches are encouraged, and may be submitted by forking this project and submitting a pull request through GitHub.

