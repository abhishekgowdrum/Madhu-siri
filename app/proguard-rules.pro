# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\saipr\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# For Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# For Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# For Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
