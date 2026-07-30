# dht-kt

[`dht-crawler`](https://github.com/0xddy/dht-crawler) 的 Kotlin/JVM JNI 封装。

当前 `2.0.0` 对应 native `v0.2.1`。公开 API 是 Kotlin-first：配置使用不可变
`data class`，创建使用类型安全 DSL，回调直接使用函数类型；JNI 字段、句柄和 callback
adapter 全部隐藏在内部。

## 30 秒上手（Coroutine）

```kotlin
import cn.lmcw.dht.coroutines.runDhtCrawler

suspend fun main() {
    runDhtCrawler {
        port = 12_313

        onTorrent { torrent ->
            println("${torrent.infoHash}  ${torrent.name}")
        }
        onError(::println)
    }
}
```

`runDhtCrawler` 会立即启动，挂起到当前 coroutine 被取消，并在取消或异常时自动关闭
native 资源。默认配置可直接用于运行；除了回调，所有配置都是可选的。

## API 一览

| API | 用途 |
|---|---|
| `dhtCrawler { ... }` | 创建 crawler，配置和回调写在同一个 DSL 中 |
| `DhtConfig` | 可复用、可 `copy` 的完整不可变配置 |
| `MetadataConfig` | Metadata 超时、队列和 worker |
| `NodePoolConfig` | 节点池与响应节点保留策略 |
| `RateLimitConfig` | 主动爬取、在途和响应预算 |
| `DhtCrawler` | `start`、`stop`、`state`、`nodeCount` |
| `runDhtCrawler { ... }` | Coroutine 长期运行入口；取消时自动关闭 |
| `withDhtCrawler(...) { ... }` | 将 crawler 生命周期限制在结构化并发作用域内 |
| `runUntilCancelled()` | 启动已有 `DhtCrawler`，挂起到取消并关闭 |
| `TorrentInfo` / `FileInfo` | 抓取结果，只读 `data class` |
| `DhtCrawlerNative` | 仅在 native 是磁盘文件时手动加载 |

## 配置 DSL

常用参数按职责分组，不需要面对 JNI 的扁平字段：

```kotlin
import cn.lmcw.dht.NetworkMode
import cn.lmcw.dht.dhtCrawler
import kotlin.time.Duration.Companion.seconds

val crawler = dhtCrawler {
    port = 12_313
    networkMode = NetworkMode.IPV4_ONLY
    hashQueueCapacity = 8_000

    metadata {
        timeout = 5.seconds
        queueCapacity = 10_000
        workers = 200
    }

    nodePool {
        capacity = 80_000
        lowWatermark = 8_000
    }

    rateLimit {
        findNodeRatePerSecond = 200
        maxFindNodeInFlight = 512
        maxInFlightPerSubnet = 8
    }

    onTorrent { println(it.name) }
    onError { message -> System.err.println(message) }
    filterMetadata { infoHash -> infoHash !in blockedHashes }
}
```

`dhtCrawler` 只负责创建并持有 native 资源，不会自动启动。调用 `start()` 后才开始爬取。

## Coroutine 生命周期

Coroutine 支持位于可选的 `dht-jni-coroutines` 模块，核心包不会强制引入协程：

```kotlin
dependencies {
    implementation(files("libs/dht-jni-core-2.0.0.jar"))
    implementation(files("libs/dht-jni-coroutines-2.0.0.jar"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    runtimeOnly(files("libs/dht-jni-native-linux-x64-2.0.0.jar"))
}
```

长期服务直接使用 `runDhtCrawler`。用 `withTimeout`、父 `Job` 或应用作用域取消它即可停止：

```kotlin
import cn.lmcw.dht.coroutines.runDhtCrawler
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.minutes

withTimeout(10.minutes) {
    runDhtCrawler {
        onTorrent(::saveTorrent)
        onError(logger::error)
    }
}
```

需要在运行期间读取状态或启动子任务时，使用 `withDhtCrawler`：

```kotlin
import cn.lmcw.dht.coroutines.withDhtCrawler
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

withDhtCrawler(
    configure = {
        port = 12_313
        onTorrent(::saveTorrent)
    },
) { crawler ->
    launch {
        while (isActive) {
            delay(30.seconds)
            println("nodes=${crawler.nodeCount}")
        }
    }
}
```

`withDhtCrawler` 会等待 block 及其所有子 coroutine 结束，再关闭 crawler。创建前已经取消的
coroutine 不会分配 native 资源。这里不提供含义模糊的裸 `await()`：crawler 没有可返回的
计算结果，长期运行语义由 `runDhtCrawler` / `runUntilCancelled` 明确表达。

## 可复用配置

配置类都是不可变 `data class`，适合保存基线并用 `copy` 派生不同实例：

```kotlin
import cn.lmcw.dht.DhtConfig
import cn.lmcw.dht.MetadataConfig
import cn.lmcw.dht.RateLimitConfig
import cn.lmcw.dht.dhtCrawler
import kotlin.time.Duration.Companion.seconds

val production = DhtConfig(
    metadata = MetadataConfig(
        timeout = 5.seconds,
        workers = 256,
    ),
    rateLimit = RateLimitConfig(
        findNodeRatePerSecond = 200,
    ),
)

val crawler = dhtCrawler(production.copy(port = 12_313)) {
    onTorrent(::saveTorrent)
    onError(logger::error)
}
```

非法端口、容量、百分比和负数预算会在配置构造时立即抛出
`IllegalArgumentException`，不会延迟到 JNI 调用。

## 回调

```kotlin
dhtCrawler {
    onTorrent { torrent ->
        // TorrentInfo 已完成 SHA1 校验和 bencode 解析
    }

    onError { message ->
        // native 运行时错误
    }

    filterMetadata { infoHash ->
        // true 才进入 metadata 下载；应快速返回且线程安全
        true
    }
}
```

- 回调可能从多个 Rust 工作线程并发执行。
- `onTorrent` 正常返回后，native 按已接受交付处理。
- `filterMetadata` 抛出异常时，native 会记录并清除异常，按 `false` 拒绝该 InfoHash；
  异常不会从 JNI 回调继续传播。
- 当前 JNI 尚未暴露 Rust 的 delivery ack 和 metadata completion callback。
- 不注册 `onError` 时错误仍由 native 日志处理。

## 生命周期

```kotlin
val crawler = dhtCrawler {
    onTorrent(::saveTorrent)
}.start()                           // 非阻塞；重复调用安全

Runtime.getRuntime().addShutdownHook(Thread(crawler::close))

println(crawler.state)              // CREATED / RUNNING / CLOSED
println(crawler.nodeCount)
```

- `start()` 返回当前 `DhtCrawler`，可直接 `dhtCrawler { ... }.start()`。
- `stop()`、`close()` 幂等。
- 局部任务可使用 `dhtCrawler { ... }.use { crawler -> ... }` 自动关闭。
- 关闭后不能重新启动。
- native Tokio runtime 在专用后台线程释放，不阻塞调用线程。

## 配置默认值

### 顶层与 Metadata

| 配置 | 默认值 |
|---|---:|
| `port` | `6881` |
| `networkMode` | `IPV4_ONLY` |
| `hashQueueCapacity` | `10000` |
| `metadata.timeout` | `4.seconds` |
| `metadata.queueCapacity` | `10000` |
| `metadata.workers` | `256` |

### Node pool

| 配置 | 默认值 |
|---|---:|
| `capacity` | `100000` |
| `lowWatermark` | `10000` |
| `recentProbeTtl` | `600.seconds` |
| `responsiveCapacity` | `16384` |
| `responsiveTtl` | `900.seconds` |

### Rate limit

| 配置 | 默认值 |
|---|---:|
| `findNodeRatePerSecond` | `200` |
| `findNodeBurst` | `40` |
| `maxFindNodeInFlight` | `512` |
| `requestTimeout` | `2.seconds` |
| `maxNewDestinationsPerMinute` | `10000` |
| `maxReplacementsPerMinute` | `25000` |
| `maxResponseRatePerSecond` | `500` |
| `maxResponseBytesPerSecond` | `1048576` |
| `maxResponseRatePerSource` | `40` |
| `metadataPressureFloorPercent` | `25` |
| `maxInFlightPerSubnet` | `8` |

Native 只接受整秒，正的不足一秒部分会向上取整，例如 `1500.milliseconds` 按 `2s`
传入。

上游还有部分配置暂未通过 JNI 暴露，将使用 Rust 默认值：

- Metadata Peer failure cache；
- 主动 `get_peers` Peer discovery；
- bootstrap、target 和内部 scheduler 细分参数；
- runtime stats / observability snapshot。

## 依赖与 native

不使用 coroutine 时，生产环境只需加入 core 和当前平台的 native JAR：

```kotlin
dependencies {
    implementation(files("libs/dht-jni-core-2.0.0.jar"))
    runtimeOnly(files("libs/dht-jni-native-linux-x64-2.0.0.jar"))
}
```

可用 native 模块：

- `dht-jni-native-win-x64`
- `dht-jni-native-linux-x64`
- `dht-jni-native-linux-aarch64`
- `dht-jni-native-osx-x64`
- `dht-jni-native-osx-aarch64`

第一次创建 crawler 时会自动从 classpath 解压并加载 native。如果 native 位于磁盘：

```kotlin
import cn.lmcw.dht.DhtCrawlerNative
import kotlin.io.path.Path

DhtCrawlerNative.load(Path("/opt/dht/libdht_crawler.so"))
```

必须在第一次 `dhtCrawler` 调用前执行。可通过 `DhtCrawlerNative.isLoaded` 查看状态。

## 从 1.x 迁移

| 1.x | 2.0 |
|---|---|
| `DHTOptions` 可变 JNI DTO | `DhtConfig` 和分组 `data class` |
| `DhtListener` | `onTorrent` / `onError` / `filterMetadata` lambda |
| `DhtCrawler.createServer(...)` | `dhtCrawler { ... }` |
| `getNodePoolSize()` | `nodeCount` |
| `isStarted()` | `isRunning` |
| `DhtCrawlerNative.loadFromPath(...)` | `DhtCrawlerNative.load(...)` |
| 整数网络模式 | `NetworkMode` enum |
| 秒数 `Long` | `kotlin.time.Duration` |

2.0 core JAR 只能搭配 `dht-crawler 0.2.x` native，不要与 1.x 制品混用。

## 开发与验证

```powershell
.\gradlew.bat test
.\gradlew.bat :dht-jni-sample:shadowJar
```

使用本地 native：

```powershell
.\gradlew.bat `
  "-Ddht.jni.library.path=G:\path\to\dht-crawler\target\release\dht_crawler.dll" `
  "-Ddht.durationSec=5" `
  :dht-jni-sample:run
```

完整示例见
[SampleMain.kt](dht-jni-sample/src/main/kotlin/cn/lmcw/dht/sample/SampleMain.kt)，版本记录见
[CHANGELOG.md](CHANGELOG.md)。

R8/ProGuard 规则已内置于 core JAR 的 `META-INF/proguard/dht-jni.pro`。
