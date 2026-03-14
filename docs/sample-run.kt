/**
 * Sample 与 SampleMain.kt 一致。
 *
 * so/dll：classpath 上有对应 native jar 时，第一次用 DhtCrawler 会 **内部自动** 解压并加载，无需手写。
 * 仅当库在磁盘路径时，在任意 DhtCrawler 前调用 loadFromPath。
 */
package cn.lmcw.dht.sample

import cn.lmcw.dht.DhtCrawler
import cn.lmcw.dht.DhtListener
import cn.lmcw.dht.model.DHTOptions
import cn.lmcw.dht.model.TorrentInfo
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.io.path.Path

fun main() {
    System.getProperty("dht.jni.library.path")?.trim()?.takeIf { it.isNotEmpty() }?.let {
        cn.lmcw.dht.DhtCrawlerNative.loadFromPath(Path(it))
    }

    val port = System.getProperty("dht.port")?.toIntOrNull() ?: 12313
    val netMode = System.getProperty("dht.netMode")?.toIntOrNull()?.coerceIn(0, 2) ?: 0
    val durationSec = System.getProperty("dht.durationSec")?.toLongOrNull() ?: 0L
    val statsSec = System.getProperty("dht.statsSec")?.toLongOrNull()?.coerceAtLeast(10) ?: 30L

    val options = DHTOptions()
        .setPort(port)
        .setNetMode(netMode)
        .setMetadataTimeout(5L)
        .setMaxMetadataWorkerCount(200)
        .setMaxMetadataQueueSize(50_000)
        .setNodeQueueCapacity(80_000)
        .setHashQueueCapacity(8_000)

    val metadataOk = AtomicLong(0L)
    val startedAt = System.currentTimeMillis()

    val listener = object : DhtListener {
        override fun onTorrent(info: TorrentInfo) {
            metadataOk.incrementAndGet()
            println("${info.infoHash}  ${info.name}")
        }
        override fun onError(message: String) {
            System.err.println(message)
        }
    }

    println("UDP port=$port\n")

    val scheduler = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "dht-sample-stats").apply { isDaemon = true }
    }

    DhtCrawler.createServer(options, listener).use { crawler ->
        Runtime.getRuntime().addShutdownHook(Thread {
            scheduler.shutdownNow()
            try { crawler.stop() } catch (_: Throwable) { }
        })
        crawler.start()

        val statsTask = scheduler.scheduleAtFixedRate({
            val elapsedSec = ((System.currentTimeMillis() - startedAt).coerceAtLeast(1)) / 1000
            val n = metadataOk.get()
            println("[port=$port] metadata_ok=$n  speed=${"%.2f".format(n.toDouble() / elapsedSec)}/s")
        }, statsSec, statsSec, TimeUnit.SECONDS)

        if (durationSec <= 0) {
            Thread.sleep(Long.MAX_VALUE)
        } else {
            Thread.sleep(TimeUnit.SECONDS.toMillis(durationSec))
            statsTask.cancel(false)
            scheduler.shutdown()
            scheduler.awaitTermination(2, TimeUnit.SECONDS)
            val elapsedSec = ((System.currentTimeMillis() - startedAt).coerceAtLeast(1)) / 1000
            val n = metadataOk.get()
            println("[done] port=$port metadata_ok=$n  avg=${"%.2f".format(n.toDouble() / elapsedSec)}/s")
        }
    }
}
