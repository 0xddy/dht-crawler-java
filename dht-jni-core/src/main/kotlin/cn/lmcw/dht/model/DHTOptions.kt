package cn.lmcw.dht.model

/**
 * Mirrors Rust DHTOptions; field names/types must match jni/types.rs GetField.
 * netMode: 0=IPv4Only, 1=IPv6Only, 2=DualStack
 */
class DHTOptions {
    @JvmField
    var port: Int = 6881

    @JvmField
    var metadataTimeout: Long = 3L

    @JvmField
    var maxMetadataQueueSize: Int = 100_000

    @JvmField
    var maxMetadataWorkerCount: Int = 1_000

    @JvmField
    var nodeQueueCapacity: Int = 100_000

    @JvmField
    var hashQueueCapacity: Int = 10_000

    @JvmField
    var netMode: Int = 0

    fun setPort(port: Int) = apply { this.port = port }
    fun setMetadataTimeout(metadataTimeout: Long) = apply { this.metadataTimeout = metadataTimeout }
    fun setMaxMetadataQueueSize(maxMetadataQueueSize: Int) = apply { this.maxMetadataQueueSize = maxMetadataQueueSize }
    fun setMaxMetadataWorkerCount(maxMetadataWorkerCount: Int) = apply { this.maxMetadataWorkerCount = maxMetadataWorkerCount }
    fun setNodeQueueCapacity(nodeQueueCapacity: Int) = apply { this.nodeQueueCapacity = nodeQueueCapacity }
    fun setHashQueueCapacity(hashQueueCapacity: Int) = apply { this.hashQueueCapacity = hashQueueCapacity }
    fun setNetMode(netMode: Int) = apply { this.netMode = netMode }

    override fun toString(): String =
        "DHTOptions(port=$port, metadataTimeout=$metadataTimeout, netMode=$netMode)"
}
