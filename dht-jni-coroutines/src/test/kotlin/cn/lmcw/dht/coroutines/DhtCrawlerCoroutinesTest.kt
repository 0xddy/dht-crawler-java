package cn.lmcw.dht.coroutines

import cn.lmcw.dht.DhtCrawler
import cn.lmcw.dht.DhtCrawlerNative
import cn.lmcw.dht.dhtCrawler
import java.util.concurrent.CancellationException
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

class DhtCrawlerCoroutinesTest {
    @Test
    fun `cancelled coroutine does not allocate a native crawler`() = runBlocking {
        var configured = false

        val job = launch {
            coroutineContext.cancel()

            assertFailsWith<CancellationException> {
                runDhtCrawler {
                    configured = true
                }
            }
        }

        job.join()
        assertFalse(configured)
    }

    @Test
    fun `cancellation closes a running crawler`() {
        val nativePath = System.getProperty("dht.jni.library.path") ?: return
        DhtCrawlerNative.load(Path(nativePath))
        val crawler = dhtCrawler {
            port = 0
        }

        runBlocking {
            withTimeoutOrNull(1.seconds) {
                crawler.runUntilCancelled()
            }
        }

        assertEquals(DhtCrawler.State.CLOSED, crawler.state)
    }
}
