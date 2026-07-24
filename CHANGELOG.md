# Changelog

## 2.0.0

面向 `dht-crawler v0.2.1` 的破坏性升级。

### 公开 API 重构

- 新增 `dhtCrawler { ... }` 类型安全 DSL。
- 用不可变 `DhtConfig`、`MetadataConfig`、`NodePoolConfig`、`RateLimitConfig`
  取代直接暴露的可变 `DHTOptions` JNI DTO。
- 配置类改为 Kotlin `data class`，支持默认参数、命名参数和 `copy`。
- 时长配置改用 `kotlin.time.Duration`。
- `onTorrent`、`onError`、`filterMetadata` 改用函数类型，不再要求实现
  `DhtListener`。
- `TorrentInfo`、`FileInfo` 改为只读 `data class`，集合保证非空。
- 生命周期属性改为 `state`、`isRunning`、`isOpen`、`nodeCount`。
- 手动 native 加载入口简化为 `DhtCrawlerNative.load(path)`。
- 新增可选 `dht-jni-coroutines` 模块：
  - `runDhtCrawler` 挂起运行，并在取消或异常时关闭 crawler；
  - `withDhtCrawler` 提供结构化并发生命周期；
  - `runUntilCancelled` 用于已有 `DhtCrawler`。

### JNI 与运行时

- JNI 扁平字段和 callback adapter 移入 `internal` 包，不再污染公开 API。
- 同步 dht-crawler 0.2 的 metadata、crawl、response budget 和 subnet 默认值。
- 所有配置在进入 JNI 前验证。
- native 只支持整秒；正的小数秒向上取整。
- native 解压缓存 namespace 升级到 `2.0.0`，避免复用旧 ABI 二进制。
- Release 固定使用 `dht-crawler v0.2.1`，并增加发版前测试。

### 兼容性

2.0 core JAR 必须搭配 `dht-crawler 0.2.x` native。不能与 1.x core/native
交叉组合。
