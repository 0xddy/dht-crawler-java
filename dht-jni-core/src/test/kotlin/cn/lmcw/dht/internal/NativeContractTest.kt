package cn.lmcw.dht.internal

import cn.lmcw.dht.DhtConfig
import cn.lmcw.dht.DhtCallbacks
import cn.lmcw.dht.MetadataConfig
import cn.lmcw.dht.NetworkMode
import cn.lmcw.dht.model.TorrentInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class NativeContractTest {
    @Test
    fun `native fields keep Rust names and JVM primitive types`() {
        val expected = mapOf(
            "port" to Int::class.javaPrimitiveType,
            "metadataTimeout" to Long::class.javaPrimitiveType,
            "maxMetadataQueueSize" to Int::class.javaPrimitiveType,
            "maxMetadataWorkerCount" to Int::class.javaPrimitiveType,
            "poolCapacity" to Int::class.javaPrimitiveType,
            "findNodeRatePerSecond" to Int::class.javaPrimitiveType,
            "findNodeBurst" to Int::class.javaPrimitiveType,
            "maxFindNodeInFlight" to Int::class.javaPrimitiveType,
            "maxNewDestinationsPerMinute" to Int::class.javaPrimitiveType,
            "maxReplacementsPerMinute" to Int::class.javaPrimitiveType,
            "requestTimeoutSeconds" to Long::class.javaPrimitiveType,
            "maxResponseRatePerSecond" to Int::class.javaPrimitiveType,
            "maxResponseBytesPerSecond" to Long::class.javaPrimitiveType,
            "maxResponseRatePerSource" to Int::class.javaPrimitiveType,
            "metadataPressureFloorPercent" to Int::class.javaPrimitiveType,
            "recentProbeTtlSeconds" to Long::class.javaPrimitiveType,
            "responsiveCapacity" to Int::class.javaPrimitiveType,
            "responsiveTtlSeconds" to Long::class.javaPrimitiveType,
            "poolLowWatermark" to Int::class.javaPrimitiveType,
            "maxInFlightPerSubnet" to Int::class.javaPrimitiveType,
            "hashQueueCapacity" to Int::class.javaPrimitiveType,
            "netMode" to Int::class.javaPrimitiveType,
        )

        expected.forEach { (name, type) ->
            assertEquals(type, NativeOptions::class.java.getField(name).type, name)
        }
    }

    @Test
    fun `mapping rounds durations up and maps enum ordinals explicitly`() {
        val native = NativeOptions(
            DhtConfig(
                networkMode = NetworkMode.DUAL_STACK,
                metadata = MetadataConfig(timeout = 1_500.milliseconds),
            ),
        )

        assertEquals(2L, native.metadataTimeout)
        assertEquals(2, native.netMode)
    }

    @Test
    fun `native listener forwards all Rust callback methods`() {
        var torrentDelivered = false
        var errorDelivered = false
        val listener = NativeListener(
            DhtCallbacks(
                onTorrent = { torrentDelivered = true },
                onError = { errorDelivered = true },
                metadataFilter = { it == "allowed" },
            ),
        )

        listener.onTorrent(
            TorrentInfo("hash", "magnet", "name", 1, emptyList(), 1, emptyList(), 0),
        )
        listener.onError("error")

        assertTrue(torrentDelivered)
        assertTrue(errorDelivered)
        assertTrue(listener.onMetadataFetch("allowed"))
        assertFalse(listener.onMetadataFetch("blocked"))
    }
}
