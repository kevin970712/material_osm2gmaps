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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# =========== Material Components Rules ===========
# Keep attributes required by Material Components themes.
-keep class com.google.android.material.** { *; }
-keep interface com.google.android.material.** { *; }
-keep public class * extends com.google.android.material.**
-keep public class * extends androidx.appcompat.app.AppCompatActivity

# Keep specific classes that might be used via reflection by Material Components.
-keep class com.google.android.material.theme.overlay.MaterialThemeOverlay

# Keep annotation classes
-keepattributes *Annotation*


# =========== Gson Rules (for data serialization) ===========
# This is crucial for classes like LocationInfo.
-keep class net.retiolus.osm2gmaps.utils.maps.** { *; }
-keepclassmembers class net.retiolus.osm2gmaps.utils.maps.** { *; }

# General Gson rules.
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class com.google.gson.Gson { *; }
-keep class com.google.gson.annotations.** { *; }


# =========== Other potential rules ===========
# If you use coroutines, it's good to keep them.
-keep class kotlinx.coroutines.** { *; }