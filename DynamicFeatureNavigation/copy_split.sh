./gradlew bundleDebug
rm build/app-debug.apks
bundletool build-apks --bundle=`pwd`/app/build/outputs/bundle/debug/app-debug.aab --output=build/app-debug.apks
bundletool install-apks --apks=build/app-debug.apks
rm -r build/apks
unzip build/app-debug.apks -d build/apks
adb shell "run-as $1 sh -c \"rm -rf /data/data/$1/files/splitcompat\""
adb shell "run-as $1 sh -c \"mkdir -p /data/data/$1/files/splits\""
for file in build/apks/splits/*;
do FILENAME=`basename $file` && cat $file | adb shell "run-as $1 sh -c \"cat > /data/data/$1/files/splits/$FILENAME\"";
done
