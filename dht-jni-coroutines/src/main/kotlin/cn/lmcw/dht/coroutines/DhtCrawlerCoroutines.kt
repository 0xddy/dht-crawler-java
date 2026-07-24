package cn.lmcw.dht.coroutines

import cn.lmcw.dht.DhtConfig
import cn.lmcw.dht.DhtCrawler
import cn.lmcw.dht.DhtCrawlerScope
import cn.lmcw.dht.dhtCrawler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Creates and starts a crawler, then suspends until the calling coroutine is cancelled.
 *
 * Cancellation and startup failures always close the crawler before they propagate.
 */
suspend fun runDhtCrawler(
    config: DhtConfig = DhtConfig(),
    configure: DhtCrawlerScope.() -> Unit = {},
): Nothing =
    withDhtCrawler(config, configure) {
        awaitCancellation()
    }

/**
 * Runs [block] with a started crawler and closes it after the block and all its children complete.
 *
 * The [CoroutineScope] receiver makes child jobs part of the crawler's structured lifetime.
 */
suspend fun <R> withDhtCrawler(
    config: DhtConfig = DhtConfig(),
    configure: DhtCrawlerScope.() -> Unit = {},
    block: suspend CoroutineScope.(DhtCrawler) -> R,
): R {
    currentCoroutineContext().ensureActive()
    return dhtCrawler(config, configure).use { crawler ->
        crawler.start()
        coroutineScope {
            block(crawler)
        }
    }
}

/**
 * Starts this crawler and suspends until cancellation, then closes it.
 *
 * Prefer [runDhtCrawler] when the coroutine should also create the crawler.
 */
suspend fun DhtCrawler.runUntilCancelled(): Nothing {
    currentCoroutineContext().ensureActive()
    try {
        start()
        awaitCancellation()
    } finally {
        close()
    }
}
