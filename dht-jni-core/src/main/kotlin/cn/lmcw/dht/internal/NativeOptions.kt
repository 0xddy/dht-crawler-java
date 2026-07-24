package cn.lmcw.dht.internal

import cn.lmcw.dht.DhtConfig
import cn.lmcw.dht.NetworkMode
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Exact field contract read by dht-crawler/jni/types.rs.
 *
 * This is deliberately separate from the public immutable configuration API.
 */
internal class NativeOptions(config: DhtConfig) {
    @JvmField val port: Int = config.port
    @JvmField val metadataTimeout: Long = config.metadata.timeout.nativeSeconds()
    @JvmField val maxMetadataQueueSize: Int = config.metadata.queueCapacity
    @JvmField val maxMetadataWorkerCount: Int = config.metadata.workers
    @JvmField val poolCapacity: Int = config.nodePool.capacity
    @JvmField val findNodeRatePerSecond: Int = config.rateLimit.findNodeRatePerSecond
    @JvmField val findNodeBurst: Int = config.rateLimit.findNodeBurst
    @JvmField val maxFindNodeInFlight: Int = config.rateLimit.maxFindNodeInFlight
    @JvmField
    val maxNewDestinationsPerMinute: Int = config.rateLimit.maxNewDestinationsPerMinute
    @JvmField val maxReplacementsPerMinute: Int = config.rateLimit.maxReplacementsPerMinute
    @JvmField val requestTimeoutSeconds: Long = config.rateLimit.requestTimeout.nativeSeconds()
    @JvmField val maxResponseRatePerSecond: Int = config.rateLimit.maxResponseRatePerSecond
    @JvmField val maxResponseBytesPerSecond: Long = config.rateLimit.maxResponseBytesPerSecond
    @JvmField val maxResponseRatePerSource: Int = config.rateLimit.maxResponseRatePerSource
    @JvmField
    val metadataPressureFloorPercent: Int = config.rateLimit.metadataPressureFloorPercent
    @JvmField val recentProbeTtlSeconds: Long = config.nodePool.recentProbeTtl.nativeSeconds()
    @JvmField val responsiveCapacity: Int = config.nodePool.responsiveCapacity
    @JvmField val responsiveTtlSeconds: Long = config.nodePool.responsiveTtl.nativeSeconds()
    @JvmField val poolLowWatermark: Int = config.nodePool.lowWatermark
    @JvmField val maxInFlightPerSubnet: Int = config.rateLimit.maxInFlightPerSubnet
    @JvmField val hashQueueCapacity: Int = config.hashQueueCapacity
    @JvmField
    val netMode: Int = when (config.networkMode) {
        NetworkMode.IPV4_ONLY -> 0
        NetworkMode.IPV6_ONLY -> 1
        NetworkMode.DUAL_STACK -> 2
    }
}

/** Native durations have whole-second precision; positive fractions round up. */
private fun Duration.nativeSeconds(): Long {
    val floor = inWholeSeconds
    return if (this == floor.seconds) floor else Math.addExact(floor, 1)
}
