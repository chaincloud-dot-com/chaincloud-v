# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/local/opt/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepattributes EnclosingMethod

-keepclassmembers class ** {
    public void onEvent*(**);
}
-dontwarn org.springframework.**

-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**
-dontwarn rx.**
-dontwarn retrofit.**
-dontwarn okio.**
-dontwarn com.chaincloud.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

-keep class sun.misc.Unsafe { *; }

-keep class com.chaincloud.** { *; }

-keep class com.joanzapata.** { *; }

-keep class com.rey.material.** { *; }
-dontwarn com.rey.material.**

-keep class org.spongycastle.asn1.** { *; }
-keep class org.spongycastle.crypto.** { *; }
-keep class org.spongycastle.math.** { *; }
-keep class org.spongycastle.util.** { *; }

-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }

-dontwarn javax.mail.**
-dontwarn javax.naming.Context
-dontwarn javax.naming.InitialContext

-keepclasseswithmembernames class * {
    native <methods>;
}
