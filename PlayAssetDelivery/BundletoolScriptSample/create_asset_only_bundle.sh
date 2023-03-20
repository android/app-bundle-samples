#!/bin/bash
#
# Copyright 2023 Google LLC
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
# -------------------------------------------------------------------------------------
#
# Creates an asset-only bundle with the given asset packs.
# Each asset pack should be a .zip with a manifest/AndroidManifest.xml file and an assets folder.
# Specify:
#  - The path for the android SDK (typically ~/Android/Sdk)
#  - The path to a deploy jar of bundletool
#  - The asset-only bundle version tag and app versions
#  - The list of asset packs to add
#  - The output file
#  - the keystore information to sign the output bundle with your Play Console upload key

USAGE='''
.../create_asset_only_bundle.sh
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
'''

for i in "$@"
do
case $i in
    --android-sdk=*)
    ANDROID_SDK="${i#*=}"
    shift # past argument=value
    ;;
    -b=*|--bundletool=*)
    BUNDLETOOL="${i#*=}"
    shift # past argument=value
    ;;
    --packdir=*)
    PACKDIR="${i#*=}"
    shift # past argument=value
    ;;
    -p=*|--packs=*)
    PACKS="${i#*=}"
    shift # past argument=value
    ;;
    -o=*|--output=*)
    OUTPUT="${i#*=}"
    shift # past argument=value
    ;;
    --app-versions=*)
    APP_VERSIONS="${i#*=}"
    shift # past argument=value
    ;;
    --version-tag=*)
    VERSION_TAG="${i#*=}"
    shift # past argument=value
    ;;
    --tmpdir=*)
    TMPDIR="${i#*=}"
    shift # past argument=value
    ;;
    --ks=*)
    KEYSTORE="${i#*=}"
    shift # past argument=value
    ;;
    --key=*)
    KEY="${i#*=}"
    shift # past argument=value
    ;;
    --ks_password=*)
    KEY_PASSWORD="${i#*=}"
    shift # past argument=value
    ;;
    *)
        echo "$USAGE"
        exit 1
    ;;
esac
done

if [[ -z "$ANDROID_SDK" ]] \
  || [[ -z "$BUNDLETOOL" ]] \
  || [[ -z "$PACKDIR" ]] \
  || [[ -z "$PACKS" ]] \
  || [[ -z "$OUTPUT" ]] \
  || [[ -z "$APP_VERSIONS" ]] \
  || [[ -z "$VERSION_TAG" ]] \
  || [[ -z "$TMPDIR" ]];
then
    echo "$USAGE"
    exit 1
fi

WD="$TMPDIR/assetonly_tmp"
mkdir -p "${WD}"

PACK_LIST=$(echo "$PACKS" | tr -s ',' ' ')

BUNDLE_CONFIG='{
  "asset_modules_config": {
    "app_version": ['${APP_VERSIONS}'],
    "asset_version_tag": "'${VERSION_TAG}'"
  },
  "type": "ASSET_ONLY"
}'

echo "Preparing packs..."

for PACK in $PACK_LIST
do
    PACKNAME=$(basename -- "$PACK")
    PACKNAME=${PACKNAME%.*}
    unzip -q "${PACKDIR}/${PACK}.zip" -d "$WD/$PACKNAME"
    mv "$WD/$PACKNAME/manifest/AndroidManifest.xml" manifest.xml
    "$ANDROID_SDK/build-tools/28.0.3/aapt2" link \
        --proto-format \
        --output-to-dir \
        -o "$WD/$PACKNAME/manifest" \
        --manifest manifest.xml \
        -I "${ANDROID_SDK}/platforms/android-28/android.jar"
    rm "$WD/$PACKNAME/manifest/resources.pb"
    rm manifest.xml
done

cd "$WD"

for MODULE in *
do
    cd "$MODULE"
    zip -q -r -0 "../$MODULE.zip" *
    cd ..
done

MODULES=$(ls *.zip)
# Replacing spaces and line breaks with comma.
MODULES=$(echo "$MODULES" | tr -s ' \n' ',')
# Removing trailing comma if present.
MODULES=${MODULES%,}

echo "Preparing BundleConfig..."
BUNDLE_CONFIG_FILE="$WD/BundleConfig.json"
echo "${BUNDLE_CONFIG}" > "$BUNDLE_CONFIG_FILE"

echo "Running 'bundletool build-bundle --modules=$MODULES --output=$OUTPUT" \
"--config=$BUNDLE_CONFIG_FILE'"
java -jar "$BUNDLETOOL" build-bundle \
  --modules="$MODULES" \
  --output="$OUTPUT" \
  --config="$BUNDLE_CONFIG_FILE"

cd ..
rm -rf assetonly_tmp

if [[ ! -z "$KEYSTORE" ]]; then
  echo "Signing"
  jarsigner -keystore "$KEYSTORE" \
      -storepass "$KEY_PASSWORD" \
      "$OUTPUT" \
      "$KEY"
fi

echo "Done"