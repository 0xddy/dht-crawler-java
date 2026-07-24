# dht-jni（聚合）

无源码，仅把 **dht-jni-core** 与 **全平台 native** 绑在一起。聚合 **jar** 内同样带有 **`META-INF/proguard/dht-jni.pro`**（与 [proguard-dht-jni.pro](proguard-dht-jni.pro) 同源，JNI/R8 必留规则）。

2.0.0 绑定固定配套 `dht-crawler v0.2.1` native；不要与 1.x core/native
交叉组合。生产环境通常应直接依赖 core 加单个平台模块，以减小制品。

公开 API 位于 core，采用 Kotlin `data class` 配置与 `dhtCrawler { ... }` DSL；
本模块不增加额外 API。需要结构化并发生命周期时，额外依赖
`dht-jni-coroutines`，使用 `runDhtCrawler` 或 `withDhtCrawler`。

- 继续用 `implementation(project(":dht-jni"))` 时，行为与拆分前一致（classpath 上仍有全部平台的 so/dll）。
- 若发布到 Maven，可只发 **dht-jni-core** + 各 **dht-jni-native-***，由使用方按平台选一条 `runtimeOnly`。
