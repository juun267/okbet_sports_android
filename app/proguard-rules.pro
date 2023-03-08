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
-keep class com.google.android.gms.common.ConnectionResult {
    int SUCCESS;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep public class com.android.installreferrer.**{ *; }

-keep class com.appsflyer.** { *; }
-keep public class com.miui.referrer.** {*;}
-keep class com.qiniu.qmedia.** {*;}
-keep class com.qiniu.qplayer2ext.** {*;}

#====== 网易滑块验证 ============ start
#-keepattributes *Annotation*
#-keep public class com.netease.nis.captcha.**{*;}
#
#-keep public class android.webkit.**
#
#-keepattributes SetJavaScriptEnabled
#-keepattributes JavascriptInterface
#
#-keepclassmembers class * {
#    @android.webkit.JavascriptInterface <methods>;
#}
#====== 网易滑块验证 ============ end

-keep public class org.cxct.sportlottery.databinding.* implements androidx.viewbinding.ViewBinding {
    public static * inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
    public static * inflate(android.view.LayoutInflater);
}

#====== GSYVideoPlayer ============ start
-keep class com.shuyu.gsyvideoplayer.video.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.video.**
-keep class com.shuyu.gsyvideoplayer.video.base.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.video.base.**
-keep class com.shuyu.gsyvideoplayer.utils.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.utils.**
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
-keep class com.google.android.exoplayer2.** {*;}
-keep interface com.google.android.exoplayer2.**

-keep class com.shuyu.alipay.** {*;}
-keep interface com.shuyu.alipay.**
#====== GSYVideoPlayer ============ end

#====== EventBus ============ start
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# If using AsyncExecutord, keep required constructor of default event used.
# Adjust the class name if a custom failure event type is used.
-keepclassmembers class org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
#====== EventBus ============ end
