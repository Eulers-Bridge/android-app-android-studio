# Add project specific ProGuard rules here.

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

## Local app requirements for debugging & correct functionality ##
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

-keep class com.eulersbridge.** { *; }
-dontwarn com.eulersbridge.**

-keep class android.support.v7.widget.SearchView { *; }

## AWS SDK ##
# Class names are needed in reflection
-keepnames class com.amazonaws.**
-keepnames class com.amazon.**
# Request handlers defined in request.handlers
-keep class com.amazonaws.services.**.*Handler

# Referenced but not required to run
-dontwarn com.fasterxml.jackson.**
-dontwarn okio.**
-dontwarn javax.annotation.**

## Glide ##
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

-keep class com.bumptech.glide.integration.okhttp3.OkHttpGlideModule

## OkHttp ##
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

## Retrofit ##
-dontwarn retrofit2.Platform$Java8

## Moshi ##
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonQualifier interface *

## BottomNavigationViewEx ##
-keep public class android.support.design.widget.BottomNavigationView { *; }
-keep public class android.support.design.internal.BottomNavigationMenuView { *; }
-keep public class android.support.design.internal.BottomNavigationPresenter { *; }
-keep public class android.support.design.internal.BottomNavigationItemView { *; }

# Required for Matisse, which supports both Glide and Picasso as image engines
-dontwarn com.squareup.picasso.**

## Android Support RenderScript ##
-keep class android.support.v8.renderscript.** { *; }

## Dagger ##
-dontwarn com.google.errorprone.annotations.**