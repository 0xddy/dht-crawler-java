package cn.lmcw.dht

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds

class DhtConfigTest {
    @Test
    fun `defaults match dht-crawler 0_2 JNI defaults`() {
        val config = DhtConfig()

        assertEquals(6881, config.port)
        assertEquals(NetworkMode.IPV4_ONLY, config.networkMode)
        assertEquals(10_000, config.hashQueueCapacity)
        assertEquals(4.seconds, config.metadata.timeout)
        assertEquals(10_000, config.metadata.queueCapacity)
        assertEquals(256, config.metadata.workers)
        assertEquals(100_000, config.nodePool.capacity)
        assertEquals(10_000, config.nodePool.lowWatermark)
        assertEquals(200, config.rateLimit.findNodeRatePerSecond)
        assertEquals(512, config.rateLimit.maxFindNodeInFlight)
        assertEquals(1_048_576L, config.rateLimit.maxResponseBytesPerSecond)
    }

    @Test
    fun `data classes support safe focused copies`() {
        val tuned = DhtConfig().copy(
            port = 12_313,
            metadata = MetadataConfig().copy(workers = 128),
            rateLimit = RateLimitConfig().copy(findNodeRatePerSecond = 100),
        )

        assertEquals(12_313, tuned.port)
        assertEquals(128, tuned.metadata.workers)
        assertEquals(100, tuned.rateLimit.findNodeRatePerSecond)
        assertEquals(100_000, tuned.nodePool.capacity)
    }

    @Test
    fun `invalid configuration fails at construction`() {
        assertFailsWith<IllegalArgumentException> {
            DhtConfig(port = 65_536)
        }
        assertFailsWith<IllegalArgumentException> {
            NodePoolConfig(capacity = 100, lowWatermark = 101)
        }
        assertFailsWith<IllegalArgumentException> {
            RateLimitConfig(metadataPressureFloorPercent = 101)
        }
    }
}
