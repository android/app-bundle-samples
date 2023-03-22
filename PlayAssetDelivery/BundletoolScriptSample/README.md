# Asset Pack packaging scripts

## Setup

1.  Set up virtualenv (Optional, recommended)

    ```
    $ virtualenv -p python3 venv
    $ . ./venv/bin/activate
    ```

2.  Install required packages

    ```
    (venv) $ pip install -r requirements.txt
    ```

## Usage

### Generating Asset Packs

```
$ generate_asset_pack.py \
   --packagename APP_PACKAGE_NAME \
   --deliverymode [install-time, fast-follow, on-demand] \
   --assetsdir ASSETS_DIR \
   --outdir OUTPUT_DIR \
   --assetpackname MY_PACK
```

### Adding Asset Packs into an App Bundle

```
$ add_packs.py \
   --androidsdk ANDROID_SDK_PATH \
   --sdkver SDK_VERSION \
   --buildtoolsver BUILD_TOOLS_VERSION \
   --bundletool BUNDLETOOL_JAR \
   --inputbundle APP.AAB \
   --packdir PACK_DIR \
   --packnames MY_PACK_1.ZIP,MY_PACK_2.ZIP \
   --output OUTPUT_APP.AAB \
   [--overwrite] \
   [--striptcfsuffixes] \
   [--prelollipop] \
   [--compressinstalltimeassets] \
```

Please note that the last flag (compressinstalltimeassets) is an EAP feature, and
availability is restricted to allowed titles only.

#### Making texture targeted asset packs

Generate your asset files in multiple formats (ASTC, ETC2, ETC1, etc...), and
put them in directories suffixed by `#tcf_xxx` (see values for `xxx` below). For
example:

```
my_asset_pack
├── some_common_file
├── some_common_directory
│   └── ....
├── textures
│   └── some_file
├── textures#tcf_astc
│   └── some_file
├── textures#tcf_dxt1
│   └── some_file
├── textures#tcf_etc2
│   └── some_file
└── textures#tcf_pvrtc
    └── some_file
```

Generate your asset packs as usual (with `generate_asset_pack.py`) and,
optionally, generate your AAB with the `--striptcfsuffixes` option. The Android
App Bundle that will be built when using this asset pack will have **a variant
of the asset pack for each texture format**.

If you specified `--striptcfsuffixes`, the `#tcf_xxx` suffixes will be removed
from the folder names in each variant of the asset packs. This means that a
folder named `my_asset_pack/textures#tcf_etc2` will be renamed to
`my_asset_pack/textures` in the ETC2 asset pack.

You can build the apks with bundletool and inspect the content to verify this:

```bash
bundletool build-apks --bundle=out/appbundle_with_tcf_asset_packs.aab --output=out/appbundle_with_tcf_asset_packs.apks
zipinfo out/appbundle_with_tcf_asset_packs.apks
# Note the variants for my_asset_pack:
# -rw----     1.0 fat     6469 bx stor 70-Jan-01 01:00 asset-slices/my_asset_pack-other_tcf.apk
# -rw----     1.0 fat     6448 bx stor 70-Jan-01 01:00 asset-slices/my_asset_pack-astc.apk
# -rw----     1.0 fat     6461 bx stor 70-Jan-01 01:00 asset-slices/my_asset_pack-etc2.apk
# -rw----     1.0 fat     6467 bx stor 70-Jan-01 01:00 asset-slices/my_asset_pack-dxt1.apk
# -rw----     1.0 fat     6619 bx stor 70-Jan-01 01:00 asset-slices/my_asset_pack-master.apk
# -rw----     1.0 fat     6473 bx stor 70-Jan-01 01:00 asset-slices/my_asset_pack-pvrtc.apk
# ...
```

Each asset pack variant contains only the common directories, and the
directories for one texture format.

The targeting is done as such:

-   All supported formats are: `astc, pvrtc, s3tc, dxt1, latc, atc, 3dc, etc2,
    etc1, paletted`.

    -   Google Play delivers the first format, in this order, that is supported
        by the device.

-   If a folder has no suffix and has sibling(s) with the same base name but
    suffixed (like `textures` in this example), it will be considered as a
    "fallback". In this directory, place the default format of your texture
    assets.

    -   If none of the texture formats in the App Bundle are supported by the
        device, Google Play delivers the "fallback" asset pack.

### Example

```
$ generate_asset_pack.py \
  --packagename com.karahan.ibrahim.dynamic.assetpacksapp \
  --assetpackname myassets \
  --deliverymode on-demand \
  --assetsdir ~/assets/myassets/ \
  --outdir ~/assets/asset_packs
```

```
$ add_packs.py \
  --androidsdk ~/Android/Sdk \
  --sdkver 28 \
  --buildtoolsver 28.0.3 \
  --bundletool ~/bundletool-all-0.10.3.jar \
  --inputbundle ~/src/app/release/app.aab \
  --packdir ~/assets/asset_packs \
  --packnames=myassets.zip \
  --output ~/aug.aab
```

### Generating Asset Only Bundles

To generate an Android App Bundle containing only some asset packs, and no code, you should follow these steps:

* build each asset pack individually with `generate_asset_pack.py`, like above
* run `create_asset_only_bundle.sh` to package them into a bundle

```
$ sh create_asset_only_bundle.sh
    --android-sdk=${HOME}/Android/Sdk
    --bundletool=/google/bin/releases/bundletool/public/bundletool-all.jar
    --output=output_directory/output_bundle.aab
    --packdir=dir/containing/my/packs/
    --packs=assetpack1,assetpack2
    --tmpdir=/tmp/my-assetonly-tmp-dir/
    --app-versions=10,12
    --version-tag=mynewassets
    [--ks=dir/to/keystore]
    [--key=your-key]
    [--ks_password=your-pw]
```

## Misc

### Python Source File for App Bundle Config Proto

The file `config_pb2.py` is generated from `config.proto` in
[Bundletool source](https://github.com/google/bundletool/blob/master/src/main/proto/config.proto)
using protobuf compiler (`protoc`). The compiler is installed together with the
protobuf C++ runtime which is available on
[GitHub](https://github.com/protocolbuffers/protobuf).

It can be installed on Debian-like systems with: `$ sudo apt install
protobuf-compiler` and on macOS with [Macports](https://www.macports.org/) with:
`$ sudo /opt/local/bin/port install protobuf3-cpp`

Use the following command to regenerate `config_pb2.py` if required: `$ protoc
--python_out . config.proto`