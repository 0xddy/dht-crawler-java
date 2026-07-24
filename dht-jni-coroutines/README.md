# dht-jni-coroutines

`dht-jni-core` 的可选 Kotlin Coroutine 生命周期扩展。依赖
`kotlinx-coroutines-core 1.11.0`，不会让非协程用户承担额外依赖。

```kotlin
import cn.lmcw.dht.coroutines.runDhtCrawler

suspend fun main() {
    runDhtCrawler {
        onTorrent(::saveTorrent)
        onError(logger::error)
    }
}
```

- `runDhtCrawler`：创建、启动并挂起到取消，随后关闭。
- `withDhtCrawler`：在结构化并发 block 中运行，block 和所有子任务结束后关闭。
- `DhtCrawler.runUntilCancelled`：启动已有 crawler，挂起到取消并关闭。

取消、block 异常和启动异常都会触发关闭。已经取消的 coroutine 不会创建 native
crawler。
