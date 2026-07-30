# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# ================================
# Retrofit
# ================================
# https://square.github.io/retrofit/
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn javax.annotation.**

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# ================================
# Moshi
# ================================
# https://github.com/square/moshi
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}

-keep @com.squareup.moshi.JsonQualifier @interface *

# Keep data classes for Moshi
-keep class com.jetpack.compose.github.github.cruise.domain.model.** { *; }
-keep class com.jetpack.compose.github.github.cruise.data.network.model.** { *; }

# Moshi JsonAdapter
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}

-keepnames @com.squareup.moshi.JsonClass class *

# ================================
# OkHttp
# ================================
# https://square.github.io/okhttp/
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ================================
# Kotlin Coroutines
# ================================
# https://github.com/Kotlin/kotlinx.coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ================================
# Hilt (Dagger)
# ================================
# https://dagger.dev/hilt/
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

-keepclasseswithmembers class * {
    @dagger.hilt.* <methods>;
}

-keepclasseswithmembers class * {
    @dagger.* <fields>;
}

# Keep Hilt generated classes
-keep class **_HiltModules { *; }
-keep class **_HiltModules$** { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }
-keep class dagger.hilt.** { *; }
-keep class **_Impl { *; }

# ================================
# Jetpack Compose
# ================================
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }

# Keep Composable functions
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# ================================
# Timber Logging
# ================================
-dontwarn org.jetbrains.annotations.**

# ================================
# Coil Image Loading
# ================================
-keep class coil.** { *; }
-keep interface coil.** { *; }

# ================================
# General Android
# ================================
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep custom exceptions for better crash reports
-keep public class * extends java.lang.Exception

# ================================
# Kotlin
# ================================
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ================================
# DataStore
# ================================
-keep class androidx.datastore.*.** { *; }

# ================================
# JUnit & Test Libraries (exclude from release)
# ================================
-dontwarn org.junit.**
-dontwarn org.hamcrest.**
-dontwarn org.mockito.**
-dontwarn org.robolectric.**
-dontwarn junit.**
-dontwarn org.apiguardian.**
-dontwarn io.mockk.**

# Exclude test classes from release builds
-dontwarn **.*Test
-dontwarn **.*Test$*
-dontwarn **.*Spec
-dontwarn **.*Spec$*