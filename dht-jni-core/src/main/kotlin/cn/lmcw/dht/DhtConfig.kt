package cn.lmcw.dht

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Immutable top-level crawler configuration. */
data class DhtConfig(
    val port: Int = 6881,
    val networkMode: NetworkMode = NetworkMode.IPV4_ONLY,
    val hashQueueCapacity: Int = 10_000,
    val metadata: MetadataConfig = MetadataConfig(),
    val nodePool: NodePoolConfig = NodePoolConfig(),
    val rateLimit: RateLimitConfig = RateLimitConfig(),
) {
    init {
        require(port in 0..65535) { "port must be in [0, 65535], got $port" }
        require(hashQueueCapacity > 0) {
            "hashQueueCapacity must be > 0, got $hashQueueCapacity"
        }
    }
}

/** Metadata download queue and worker settings. */
data class MetadataConfig(
    val timeout: Duration = 4.seconds,
    val queueCapacity: Int = 10_000,
    val workers: Int = 256,
) {
    init {
        requireFiniteNonNegative(timeout, "metadata.timeout")
        require(queueCapacity > 0) {
            "metadata.queueCapacity must be > 0, got $queueCapacity"
        }
        require(workers > 0) { "metadata.workers must be > 0, got $workers" }
    }
}

/** Crawl node-pool and responsive-node retention settings. */
data class NodePoolConfig(
    val capacity: Int = 100_000,
    val lowWatermark: Int = 10_000,
    val recentProbeTtl: Duration = 600.seconds,
    val responsiveCapacity: Int = 16_384,
    val responsiveTtl: Duration = 900.seconds,
) {
    init {
        require(capacity > 0) { "nodePool.capacity must be > 0, got $capacity" }
        require(lowWatermark in 0..capacity) {
            "nodePool.lowWatermark must be in [0, capacity], got $lowWatermark"
        }
        requireFiniteNonNegative(recentProbeTtl, "nodePool.recentProbeTtl")
        require(responsiveCapacity > 0) {
            "nodePool.responsiveCapacity must be > 0, got $responsiveCapacity"
        }
        requireFiniteNonNegative(responsiveTtl, "nodePool.responsiveTtl")
    }
}

/** Active crawl, in-flight and outbound response budgets. */
data class RateLimitConfig(
    val findNodeRatePerSecond: Int = 200,
    val findNodeBurst: Int = 40,
    val maxFindNodeInFlight: Int = 512,
    val requestTimeout: Duration = 2.seconds,
    val maxNewDestinationsPerMinute: Int = 10_000,
    val maxReplacementsPerMinute: Int = 25_000,
    val maxResponseRatePerSecond: Int = 500,
    val maxResponseBytesPerSecond: Long = 1_048_576L,
    val maxResponseRatePerSource: Int = 40,
    val metadataPressureFloorPercent: Int = 25,
    val maxInFlightPerSubnet: Int = 8,
) {
    init {
        require(findNodeRatePerSecond >= 0) {
            "rateLimit.findNodeRatePerSecond must be >= 0, got $findNodeRatePerSecond"
        }
        require(findNodeBurst >= 0) {
            "rateLimit.findNodeBurst must be >= 0, got $findNodeBurst"
        }
        require(maxFindNodeInFlight > 0) {
            "rateLimit.maxFindNodeInFlight must be > 0, got $maxFindNodeInFlight"
        }
        requireFiniteNonNegative(requestTimeout, "rateLimit.requestTimeout")
        require(maxNewDestinationsPerMinute >= 0) {
            "rateLimit.maxNewDestinationsPerMinute must be >= 0, " +
                "got $maxNewDestinationsPerMinute"
        }
        require(maxReplacementsPerMinute >= 0) {
            "rateLimit.maxReplacementsPerMinute must be >= 0, got $maxReplacementsPerMinute"
        }
        require(maxResponseRatePerSecond >= 0) {
            "rateLimit.maxResponseRatePerSecond must be >= 0, got $maxResponseRatePerSecond"
        }
        require(maxResponseBytesPerSecond >= 0L) {
            "rateLimit.maxResponseBytesPerSecond must be >= 0, " +
                "got $maxResponseBytesPerSecond"
        }
        require(maxResponseRatePerSource >= 0) {
            "rateLimit.maxResponseRatePerSource must be >= 0, got $maxResponseRatePerSource"
        }
        require(metadataPressureFloorPercent in 0..100) {
            "rateLimit.metadataPressureFloorPercent must be in [0, 100], " +
                "got $metadataPressureFloorPercent"
        }
        require(maxInFlightPerSubnet > 0) {
            "rateLimit.maxInFlightPerSubnet must be > 0, got $maxInFlightPerSubnet"
        }
    }
}

private fun requireFiniteNonNegative(value: Duration, name: String) {
    require(value.isFinite() && !value.isNegative()) {
        "$name must be finite and >= 0, got $value"
    }
}
