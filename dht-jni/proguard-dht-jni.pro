# dht-jni — JNI 与 R8/ProGuard 混淆保留（须与 dht-jni-core/proguard-dht-jni.pro 保持一致）
# Android: consumerProguardFiles 或 -include 本文件；core / 聚合 jar 均含 META-INF/proguard/dht-jni.pro

-keepclasseswithmembernames class cn.lmcw.dht.DhtCrawlerJni {
    native <methods>;
}
-keep class cn.lmcw.dht.DhtCrawlerJni {
    <init>();
}

-keepclassmembers class cn.lmcw.dht.model.DHTOptions {
    <fields>;
}
-keep class cn.lmcw.dht.model.DHTOptions {
    <init>();
}

-keepclassmembers class cn.lmcw.dht.model.FileInfo {
    <fields>;
    <init>(java.lang.String, long);
}
-keep class cn.lmcw.dht.model.FileInfo {
    <init>(java.lang.String, long);
}

-keepclassmembers class cn.lmcw.dht.model.TorrentInfo {
    <fields>;
    <init>(java.lang.String, java.lang.String, java.lang.String, long, java.util.List, long, java.util.List, long);
}
-keep class cn.lmcw.dht.model.TorrentInfo {
    <init>(java.lang.String, java.lang.String, java.lang.String, long, java.util.List, long, java.util.List, long);
}

-keep class cn.lmcw.dht.DhtListener {
    public <methods>;
}
-keep class * implements cn.lmcw.dht.DhtListener {
    public <methods>;
}

-keep class cn.lmcw.dht.DhtCrawler {
    public <methods>;
}
-keep class cn.lmcw.dht.DhtCrawlerNative {
    public <methods>;
}
-keep class cn.lmcw.dht.NativeLoader {
    public <methods>;
}
