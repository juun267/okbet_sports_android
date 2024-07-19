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

-keepclassmembers public class * implements androidx.viewbinding.ViewBinding {
    public static * inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
    public static * inflate(android.view.LayoutInflater);
    public static * bind(android.view.View);
}

-keepclassmembers public class androidx.lifecycle.LiveData {
    private int mVersion;
}

-keepclassmembers public class androidx.lifecycle.ViewModel {
    private volatile boolean mCleared;
    final void clear();
}


#====== GSYVideoPlayer ============ start
-keep class com.shuyu.gsyvideoplayer.video.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.video.**
-keep class com.shuyu.gsyvideoplayer.video.base.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.video.base.**
-keep class com.shuyu.gsyvideoplayer.utils.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.utils.**
-keep class com.shuyu.gsyvideoplayer.player.** {*;}
-dontwarn com.shuyu.gsyvideoplayer.player.**
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
-keep class androidx.media3.** {*;}
-keep interface androidx.media3.**

-keep class com.shuyu.alipay.** {*;}
-keep interface com.shuyu.alipay.**

-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, java.lang.Boolean);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
#====== GSYVideoPlayer ============ end

#====== EventBus ============ start
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
#====== EventBus ============ end

-keepclasseswithmembers class com.google.android.material.bottomnavigation.BottomNavigationItemView {*;}
-keepclasseswithmembers class com.google.android.material.bottomnavigation.BottomNavigationView {*;}
-keepclasseswithmembers class com.google.android.material.bottomnavigation.BottomNavigationMenuView {*;}

-keepclassmembers class androidx.fragment.app.DialogFragment {  <fields>; }

# 所有的类都不混淆（不得已，不要使用）
#-keep class * { *; }

-keepclassmembers @org.cxct.sportlottery.common.proguards.KeepMembers class * { *; }
-keepclassmembers class * { @org.cxct.sportlottery.common.proguards.KeepMembers <fields>; }
-keepclassmembers class * { @org.cxct.sportlottery.common.proguards.KeepMembers <methods>; }

-keepclassmembers class * extends org.cxct.sportlottery.network.common.BaseResult { *;}
-keepclassmembers class org.cxct.sportlottery.net.ApiResult { <fields>;}
-keepclassmembers class * extends org.cxct.sportlottery.net.ApiResult { <fields>;}

-keepclassmembers class * extends org.cxct.sportlottery.ui.chat.BaseEntity { <fields>;}

#kotlin  与Moshi反序列化有关（@kotlin.Metadata涉及太广，应尽量降低keep范围）
#-keepclasseswithmembers @kotlin.Metadata class org.cxct.sportlottery.network.** { *; }
#-keepclasseswithmembers @kotlin.Metadata class org.cxct.sportlottery.ui.** { *; }

-dontoptimize
-dontpreverify

-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }
-keep class * extends cn.jpush.android.service.JPushMessageReceiver { *; }
-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }

#protobuf
-keepclassmembers public class * extends com.google.protobuf.** {*;}
-keep class androidx.lifecycle.LiveData { *; }

-dontwarn cn.magicwindow.**
-keep class cn.magicwindow.** {*;}

-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }

