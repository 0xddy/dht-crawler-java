package cn.lmcw.dht

import cn.lmcw.dht.model.TorrentInfo
import kotlin.time.Duration

@DslMarker
annotation class DhtDsl

/**
 * Creates a crawler from production defaults.
 *
 * The returned crawler owns native resources but is not started yet. Call [DhtCrawler.start] and
 * close it with `use`, [DhtCrawler.stop], or [DhtCrawler.close].
 */
fun dhtCrawler(
    config: DhtConfig = DhtConfig(),
    block: DhtCrawlerScope.() -> Unit = {},
): DhtCrawler {
    val scope = DhtCrawlerScope(config).apply(block)
    return DhtCrawler.create(scope.buildConfig(), scope.buildCallbacks())
}

/** Configuration and callback scope. Callback handlers may run concurrently on Rust threads. */
@DhtDsl
class DhtCrawlerScope internal constructor(config: DhtConfig) {
    var port: Int = config.port
    var networkMode: NetworkMode = config.networkMode
    var hashQueueCapacity: Int = config.hashQueueCapacity

    private var metadataConfig: MetadataConfig = config.metadata
    private var nodePoolConfig: NodePoolConfig = config.nodePool
    private var rateLimitConfig: RateLimitConfig = config.rateLimit

    private var torrentHandler: ((TorrentInfo) -> Unit)? = null
    private var errorHandler: ((String) -> Unit)? = null
    private var fetchFilter: ((String) -> Boolean)? = null

    fun metadata(block: MetadataScope.() -> Unit) {
        metadataConfig = MetadataScope(metadataConfig).apply(block).build()
    }

    fun nodePool(block: NodePoolScope.() -> Unit) {
        nodePoolConfig = NodePoolScope(nodePoolConfig).apply(block).build()
    }

    fun rateLimit(block: RateLimitScope.() -> Unit) {
        rateLimitConfig = RateLimitScope(rateLimitConfig).apply(block).build()
    }

    fun onTorrent(handler: (TorrentInfo) -> Unit) {
        torrentHandler = handler
    }

    /** Receives recoverable native runtime errors. */
    fun onError(handler: (String) -> Unit) {
        errorHandler = handler
    }

    /** Return false to reject an InfoHash before metadata download. Keep this callback fast. */
    fun filterMetadata(predicate: (String) -> Boolean) {
        fetchFilter = predicate
    }

    internal fun buildConfig(): DhtConfig =
        DhtConfig(
            port = port,
            networkMode = networkMode,
            hashQueueCapacity = hashQueueCapacity,
            metadata = metadataConfig,
            nodePool = nodePoolConfig,
            rateLimit = rateLimitConfig,
        )

    internal fun buildCallbacks(): DhtCallbacks =
        DhtCallbacks(
            onTorrent = torrentHandler,
            onError = errorHandler,
            metadataFilter = fetchFilter,
        )
}

@DhtDsl
class MetadataScope internal constructor(config: MetadataConfig) {
    var timeout: Duration = config.timeout
    var queueCapacity: Int = config.queueCapacity
    var workers: Int = config.workers

    internal fun build(): MetadataConfig =
        MetadataConfig(
            timeout = timeout,
            queueCapacity = queueCapacity,
            workers = workers,
        )
}

@DhtDsl
class NodePoolScope internal constructor(config: NodePoolConfig) {
    var capacity: Int = config.capacity
    var lowWatermark: Int = config.lowWatermark
    var recentProbeTtl: Duration = config.recentProbeTtl
    var responsiveCapacity: Int = config.responsiveCapacity
    var responsiveTtl: Duration = config.responsiveTtl

    internal fun build(): NodePoolConfig =
        NodePoolConfig(
            capacity = capacity,
            lowWatermark = lowWatermark,
            recentProbeTtl = recentProbeTtl,
            responsiveCapacity = responsiveCapacity,
            responsiveTtl = responsiveTtl,
        )
}

@DhtDsl
class RateLimitScope internal constructor(config: RateLimitConfig) {
    var findNodeRatePerSecond: Int = config.findNodeRatePerSecond
    var findNodeBurst: Int = config.findNodeBurst
    var maxFindNodeInFlight: Int = config.maxFindNodeInFlight
    var requestTimeout: Duration = config.requestTimeout
    var maxNewDestinationsPerMinute: Int = config.maxNewDestinationsPerMinute
    var maxReplacementsPerMinute: Int = config.maxReplacementsPerMinute
    var maxResponseRatePerSecond: Int = config.maxResponseRatePerSecond
    var maxResponseBytesPerSecond: Long = config.maxResponseBytesPerSecond
    var maxResponseRatePerSource: Int = config.maxResponseRatePerSource
    var metadataPressureFloorPercent: Int = config.metadataPressureFloorPercent
    var maxInFlightPerSubnet: Int = config.maxInFlightPerSubnet

    internal fun build(): RateLimitConfig =
        RateLimitConfig(
            findNodeRatePerSecond = findNodeRatePerSecond,
            findNodeBurst = findNodeBurst,
            maxFindNodeInFlight = maxFindNodeInFlight,
            requestTimeout = requestTimeout,
            maxNewDestinationsPerMinute = maxNewDestinationsPerMinute,
            maxReplacementsPerMinute = maxReplacementsPerMinute,
            maxResponseRatePerSecond = maxResponseRatePerSecond,
            maxResponseBytesPerSecond = maxResponseBytesPerSecond,
            maxResponseRatePerSource = maxResponseRatePerSource,
            metadataPressureFloorPercent = metadataPressureFloorPercent,
            maxInFlightPerSubnet = maxInFlightPerSubnet,
        )
}

internal data class DhtCallbacks(
    val onTorrent: ((TorrentInfo) -> Unit)? = null,
    val onError: ((String) -> Unit)? = null,
    val metadataFilter: ((String) -> Boolean)? = null,
)
