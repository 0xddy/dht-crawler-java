# dht-kt 2.x — JNI/R8/ProGuard keep rules
# Keep synchronized with dht-jni-core/proguard-dht-jni.pro.

-keepclasseswithmembernames class cn.lmcw.dht.DhtCrawlerJni {
    native <methods>;
}
-keep class cn.lmcw.dht.DhtCrawlerJni {
    <init>();
}

-keepclassmembers class cn.lmcw.dht.internal.NativeOptions {
    <fields>;
}

-keepclassmembers class cn.lmcw.dht.internal.NativeListener {
    void onTorrent(cn.lmcw.dht.model.TorrentInfo);
    void onError(java.lang.String);
    boolean onMetadataFetch(java.lang.String);
}

-keep class cn.lmcw.dht.model.FileInfo {
    public *;
}
-keep class cn.lmcw.dht.model.TorrentInfo {
    public *;
}

-keep class cn.lmcw.dht.DhtCrawler {
    public *;
}
-keep class cn.lmcw.dht.DhtCrawler$State {
    public *;
}
-keep class cn.lmcw.dht.DhtCrawlerNative {
    public *;
}
-keep class cn.lmcw.dht.NetworkMode {
    public *;
}
-keep class cn.lmcw.dht.DhtConfig {
    public *;
}
-keep class cn.lmcw.dht.MetadataConfig {
    public *;
}
-keep class cn.lmcw.dht.NodePoolConfig {
    public *;
}
-keep class cn.lmcw.dht.RateLimitConfig {
    public *;
}
-keep class cn.lmcw.dht.DhtCrawlerDslKt {
    public static <methods>;
}
