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

# Jetpack Compose rules
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep composable functions and their parameters
-keep class * extends androidx.compose.runtime.ComposableSingletons* {
    *;
}

# Keep Composable annotation and related classes
-keep @androidx.compose.runtime.Composable class *
-keep @androidx.compose.runtime.Composable class ** {
    *;
}

# Keep Compose runtime classes
-keep class androidx.compose.runtime.** { *; }

# Kotlin Serialization rules
-keepattributes Annotation, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keepnames class kotlinx.serialization.json.** { *; }
-keepnames class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# Keep data class constructors and properties for serialization
-keep class io.github.jdanders.dropcount.** {
    <init>(...);
    *;
}

# Coroutines rules
-keep class kotlin.coroutines.Continuation { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# DataStore rules
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# Keep ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
    *;
}

# Keep lifecycle-related classes
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# Keep core Android classes
-keep class androidx.core.** { *; }
-dontwarn androidx.core.**

# Keep activity classes
-keep class androidx.activity.** { *; }
-dontwarn androidx.activity.**