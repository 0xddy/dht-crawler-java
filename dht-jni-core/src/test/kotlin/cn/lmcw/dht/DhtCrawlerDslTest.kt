package cn.lmcw.dht

import cn.lmcw.dht.model.TorrentInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class DhtCrawlerDslTest {
    @Test
    fun `DSL groups configuration and callbacks`() {
        var delivered: TorrentInfo? = null
        val scope = DhtCrawlerScope(DhtConfig()).apply {
            port = 12_313
            networkMode = NetworkMode.DUAL_STACK
            metadata {
                timeout = 5.seconds
                workers = 128
            }
            nodePool {
                capacity = 80_000
                lowWatermark = 8_000
            }
            rateLimit {
                findNodeRatePerSecond = 100
            }
            onTorrent { delivered = it }
            filterMetadata { it.startsWith("00") }
        }

        val config = scope.buildConfig()
        val callbacks = scope.buildCallbacks()
        assertEquals(12_313, config.port)
        assertEquals(NetworkMode.DUAL_STACK, config.networkMode)
        assertEquals(5.seconds, config.metadata.timeout)
        assertEquals(128, config.metadata.workers)
        assertEquals(80_000, config.nodePool.capacity)
        assertEquals(100, config.rateLimit.findNodeRatePerSecond)
        val filter = requireNotNull(callbacks.metadataFilter)
        assertTrue(filter("00abcdef"))
        assertFalse(filter("12abcdef"))

        val torrent = TorrentInfo("hash", "magnet", "name", 1, emptyList(), 1, emptyList(), 0)
        requireNotNull(callbacks.onTorrent)(torrent)
        assertEquals(torrent, delivered)
    }
}
