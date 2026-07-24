package cn.lmcw.dht.sample

import cn.lmcw.dht.DhtCrawlerNative
import cn.lmcw.dht.NetworkMode
import cn.lmcw.dht.coroutines.withDhtCrawler
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

suspend fun main() {
    System.getProperty("dht.jni.library.path")
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?.let { DhtCrawlerNative.load(Path(it)) }

    val port = System.getProperty("dht.port")?.toIntOrNull() ?: 12_313
    val networkMode = System.getProperty("dht.networkMode")
        ?.let { NetworkMode.valueOf(it.uppercase(Locale.ROOT)) }
        ?: NetworkMode.IPV4_ONLY
    val durationSeconds = System.getProperty("dht.durationSec")?.toLongOrNull() ?: 0L
    val statsSeconds = System.getProperty("dht.statsSec")?.toLongOrNull()?.coerceAtLeast(10) ?: 30L

    val metadataCount = AtomicLong()
    val startedAt = System.nanoTime()

    val stopSignal = CompletableDeferred<Unit>()
    val stopped = CompletableDeferred<Unit>()
    val shutdownHook = Thread(
        {
            stopSignal.complete(Unit)
            runBlocking {
                withTimeoutOrNull(5.seconds) {
                    stopped.await()
                }
            }
        },
        "dht-sample-shutdown",
    )
    Runtime.getRuntime().addShutdownHook(shutdownHook)

    try {
        withDhtCrawler(
            configure = {
                this.port = port
                this.networkMode = networkMode

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
                }

                onTorrent { torrent ->
                    metadataCount.incrementAndGet()
                    println("${torrent.infoHash}  ${torrent.name}")
                }
                onError(System.err::println)
            },
        ) { crawler ->
            println("DHT crawler started: port=$port, networkMode=$networkMode")

            val statsJob = launch {
                while (isActive) {
                    delay(statsSeconds.seconds)
                    val elapsed =
                        (System.nanoTime() - startedAt).coerceAtLeast(1) / 1_000_000_000.0
                    val count = metadataCount.get()
                    println(
                        "[stats] nodes=${crawler.nodeCount} metadata=$count " +
                            "speed=${"%.2f".format(count / elapsed)}/s",
                    )
                }
            }

            try {
                if (durationSeconds <= 0) {
                    stopSignal.await()
                } else {
                    withTimeoutOrNull(durationSeconds.seconds) {
                        stopSignal.await()
                    }
                }
            } finally {
                statsJob.cancelAndJoin()
            }
        }
    } finally {
        stopped.complete(Unit)
        runCatching { Runtime.getRuntime().removeShutdownHook(shutdownHook) }
    }

    val elapsed = (System.nanoTime() - startedAt).coerceAtLeast(1) / 1_000_000_000.0
    val count = metadataCount.get()
    println("[done] metadata=$count avg=${"%.2f".format(count / elapsed)}/s")
}
