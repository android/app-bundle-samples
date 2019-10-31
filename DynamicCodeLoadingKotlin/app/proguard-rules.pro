# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable
#-keepattributes *Annotation*
#

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


-keep class com.google.android.samples.storage.StorageFeatureImpl {
    com.google.android.samples.storage.StorageFeatureImpl$Provider Provider;
}

-keep class com.google.android.samples.storage.StorageFeatureImpl$Provider {
    *;
}

# There was a bug, but it's gone now. Uncomment on earlier R8 versions (?)
#-keep interface com.google.android.samples.dynamiccodeloading.StorageFeature {
#    *;
#}

-keep class kotlin.Metadata {
    *;
}