# dht-kt 2.x — JNI/R8/ProGuard keep rules
# Keep synchronized with dht-jni/proguard-dht-jni.pro.

# Rust exports Java_cn_lmcw_dht_DhtCrawlerJni_*.
-keepclasseswithmembernames class cn.lmcw.dht.DhtCrawlerJni {
    native <methods>;
}
-keep class cn.lmcw.dht.DhtCrawlerJni {
    <init>();
}

# Rust reads these exact field names and primitive descriptors with GetField.
-keepclassmembers class cn.lmcw.dht.internal.NativeOptions {
    <fields>;
}

# Rust invokes these exact callback method names.
-keepclassmembers class cn.lmcw.dht.internal.NativeListener {
    void onTorrent(cn.lmcw.dht.model.TorrentInfo);
    void onError(java.lang.String);
    boolean onMetadataFetch(java.lang.String);
}

# Rust resolves these classes by name and calls their constructors.
-keep class cn.lmcw.dht.model.FileInfo {
    public *;
}
-keep class cn.lmcw.dht.model.TorrentInfo {
    public *;
}

# Stable public Kotlin/JVM API.
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
