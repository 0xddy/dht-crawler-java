package cn.lmcw.dht.sample

import cn.lmcw.dht.DhtCrawler
import cn.lmcw.dht.DhtListener
import cn.lmcw.dht.NativeLoader
import cn.lmcw.dht.model.DHTOptions
import cn.lmcw.dht.model.TorrentInfo
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.io.path.Path

/**
 * Minimal sample: UDP port, metadata count (valid info_hashes with .torrent name), grab rate.
 *
 * - dht.jni.library.path — native lib path if not in jar
 * - dht.port — UDP port (default 12313)
 * - dht.netMode — 0 IPv4, 1 IPv6, 2 dual
 * - dht.durationSec — >0 exit after N seconds; default run until Ctrl+C
 * - dht.statsSec — how often to print speed line (default 30)
 */
fun main() {
    val override = System.getProperty("dht.jni.library.path")
    if (!override.isNullOrBlank()) {
        cn.lmcw.dht.DhtCrawlerNative.loadFromPath(Path(override))
    } else {
        val resource = NativeLoader.resourcePathForCurrentPlatform()
        if (resource == null) {
            System.err.println("No native mapping for this OS/ARCH. Set -Ddht.jni.library.path=...")
            return
        }
        val hasResource = NativeLoader::class.java.getResourceAsStream(resource)?.use { true } ?: false
        if (!hasResource) {
            System.err.println("Missing classpath resource: $resource")
            return
        }
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

    println("UDP port=$port  (metadata lines: info_hash + name)")
    println()

    val scheduler = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "dht-sample-stats").apply { isDaemon = true }
    }

    DhtCrawler.createServer(options, listener).use { crawler ->
        Runtime.getRuntime().addShutdownHook(
            Thread {
                scheduler.shutdownNow()
                try {
                    crawler.stop()
                } catch (_: Throwable) { }
            },
        )

        crawler.start()

        val statsTask = scheduler.scheduleAtFixedRate({
            val elapsedSec = ((System.currentTimeMillis() - startedAt).coerceAtLeast(1)) / 1000
            val n = metadataOk.get()
            val perSec = n.toDouble() / elapsedSec
            println("[port=$port] metadata_ok=$n  speed=${"%.2f".format(perSec)}/s")
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
