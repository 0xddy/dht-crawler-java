# dht-crawler-java

基于 [dht-crawler](https://github.com/0xddy/dht-crawler)（Rust）的 **Kotlin / JVM JNI 绑定**。在 Java 里启动 DHT、收种子元数据。

---

### 用 JAR 接入

1. 打开 **[Releases](https://github.com/0xddy/dht-crawler-java/releases)**，下载对应版本：
  - `**dht-jni-core-<版本>.jar`** — 必下，API 在这
  - **native**：`linux-x64` / `linux-aarch64` / `win-x64` / `osx-x64` / `osx-aarch64`（与 CPU、系统对应；发版 CI 会打 **linux-aarch64**、**macos-aarch64** 的 sample 与 native JAR）
2. 放进工程的 `libs/`，Gradle 示例：

```kotlin
val dht = "1.0.2"   // 与 Release 里 jar 文件名版本一致
dependencies {
    implementation(files("libs/dht-jni-core-$dht.jar"))
    runtimeOnly(files("libs/dht-jni-native-linux-x64-$dht.jar"))
    // Linux ARM：dht-jni-native-linux-aarch64；Windows：win-x64；Mac M 系列：osx-aarch64
}
```

1. 代码里直接用 `DhtCrawler` 即可：**第一次**用到 JNI 时会在内部自动从 classpath 的 native jar 里 **解压 so/dll 再 `System.load`**，不必自己写释放/加载。只有库放在磁盘路径时，才需在任意 `DhtCrawler` 调用前 `DhtCrawlerNative.loadFromPath(Path("..."))`。
2. **只想先跑通**：按架构选胖 JAR：`linux-x64-all` / `linux-aarch64-all` / `win-x64-all` / `macos-aarch64-all`。

```bash
java -jar dht-sample-vx.x.x-linux-aarch64-all.jar   # 例：ARM64 Linux
```

常用 JVM 参数：`-Ddht.port=12313`、`-Ddht.durationSec=60`（跑满 60 秒退出）、`-Ddht.statsSec=30`（统计间隔）。自备 **JDK 21**。



### 启动运行

```kotlin
import cn.lmcw.dht.DhtCrawler
import cn.lmcw.dht.DhtListener
import cn.lmcw.dht.model.DHTOptions
import cn.lmcw.dht.model.TorrentInfo
import kotlin.io.path.Path

// 可选：仅当 so/dll 不在 jar 里、而在磁盘上时，必须在 createServer 之前调用
System.getProperty("dht.jni.library.path")?.trim()?.takeIf { it.isNotEmpty() }?.let {
    DhtCrawlerNative.loadFromPath(Path(it))
}

val options = DHTOptions().setPort(System.getProperty("dht.port")?.toIntOrNull() ?: 12313)
val listener = object : DhtListener {
    override fun onTorrent(info: TorrentInfo) = println("${info.infoHash}  ${info.name}")
    override fun onError(message: String) = System.err.println(message)
}
// 下面第一次碰到 JNI → 内部 NativeLoader 自动解压 classpath 里的 native 并加载
DhtCrawler.createServer(options, listener).use { it.start(); Thread.sleep(Long.MAX_VALUE) }
```

完整版（统计间隔、`durationSec` 退出等）见 **[docs/sample-run.kt](docs/sample-run.kt)**。

---

## 混淆（ProGuard / R8）

打 release 必须保留 JNI / 回调相关符号，可直接复制下面整段进 `proguard-rules.pro` 或 `-include` 保存后的文件。与仓库 `**dht-jni-core/proguard-dht-jni.pro`** 同源（改规则时请同步该文件）。

```proguard
# dht-jni — JNI 与 Rust 侧 symbols 一致
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
```

也可从 `**dht-jni-core-*.jar**` 内使用 `**META-INF/proguard/dht-jni.pro**`。

---

## 开发本仓库


| 命令                                | 作用             |
| --------------------------------- | -------------- |
| `./gradlew :dht-jni-sample:run`   | 本地跑示例          |
| `./gradlew :dht-jni-sample:build` | 打 sample 胖 JAR |


---

## 说明与合规

- 回调在 **Rust 线程**里执行，监听器请 **线程安全**。

