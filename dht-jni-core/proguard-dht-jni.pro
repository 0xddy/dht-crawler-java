# dht-jni — JNI names must match dht-crawler native (types.rs, exports.rs, callbacks.rs)
# Android: add to app proguard or consumerProguardFiles; jar also ships META-INF/proguard/dht-jni.pro

# DhtCrawlerJni native symbols Java_cn_lmcw_dht_DhtCrawlerJni_*
-keepclasseswithmembernames class cn.lmcw.dht.DhtCrawlerJni {
    native <methods>;
}
-keep class cn.lmcw.dht.DhtCrawlerJni {
    <init>();
}

# Rust GetField on DHTOptions
-keepclassmembers class cn.lmcw.dht.model.DHTOptions {
    <fields>;
}
-keep class cn.lmcw.dht.model.DHTOptions {
    <init>();
}

# Rust NewObject FileInfo(String, long)
-keepclassmembers class cn.lmcw.dht.model.FileInfo {
    <fields>;
    <init>(java.lang.String, long);
}
-keep class cn.lmcw.dht.model.FileInfo {
    <init>(java.lang.String, long);
}

# Rust NewObject TorrentInfo
-keepclassmembers class cn.lmcw.dht.model.TorrentInfo {
    <fields>;
    <init>(java.lang.String, java.lang.String, java.lang.String, long, java.util.List, long, java.util.List, long);
}
-keep class cn.lmcw.dht.model.TorrentInfo {
    <init>(java.lang.String, java.lang.String, java.lang.String, long, java.util.List, long, java.util.List, long);
}

# call_method onTorrent / onError / onMetadataFetch
-keep class cn.lmcw.dht.DhtListener {
    public <methods>;
}
-keep class * implements cn.lmcw.dht.DhtListener {
    public <methods>;
}

# Public API
-keep class cn.lmcw.dht.DhtCrawler {
    public <methods>;
}
-keep class cn.lmcw.dht.DhtCrawlerNative {
    public <methods>;
}
-keep class cn.lmcw.dht.NativeLoader {
    public <methods>;
}
