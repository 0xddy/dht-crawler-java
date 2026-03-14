package cn.lmcw.dht

import cn.lmcw.dht.model.DHTOptions

/** DHT session; callbacks run on Rust threads — implementations must be thread-safe. */
class DhtCrawler private constructor(
    @Volatile private var handle: Long,
) : AutoCloseable {

    @Volatile
    private var started: Boolean = false

    private val sync = Any()

    fun start() {
        synchronized(sync) {
            check(handle != 0L) { "Session closed; cannot start" }
            if (started) return
            DhtCrawlerJni.startServer(handle)
            started = true
        }
    }

    fun stop() {
        synchronized(sync) {
            if (handle == 0L) return
            DhtCrawlerJni.stopServer(handle)
            handle = 0L
            started = false
        }
    }

    fun isStarted(): Boolean = synchronized(sync) { started && handle != 0L }

    fun isOpen(): Boolean = synchronized(sync) { handle != 0L }

    fun getNodePoolSize(): Int {
        synchronized(sync) {
            if (handle == 0L) return 0
            return DhtCrawlerJni.getNodePoolSize(handle)
        }
    }

    override fun close() {
        stop()
    }

    companion object {
        /** @param options null = Rust defaults; @param listener null = no callbacks */
        @JvmStatic
        fun createServer(options: DHTOptions?, listener: DhtListener?): DhtCrawler {
            options?.let { validateOptions(it) }
            val h = DhtCrawlerJni.createServer(options, listener)
            if (h == 0L) {
                throw IllegalStateException("DHT server create failed (createServer returned 0)")
            }
            return DhtCrawler(h)
        }

        private fun validateOptions(options: DHTOptions) {
            require(options.port in 0..65535) { "port must be in [0, 65535], got ${options.port}" }
            require(options.metadataTimeout >= 0L) {
                "metadataTimeout must be >= 0, got ${options.metadataTimeout}"
            }
            require(options.maxMetadataQueueSize > 0) {
                "maxMetadataQueueSize must be > 0, got ${options.maxMetadataQueueSize}"
            }
            require(options.maxMetadataWorkerCount > 0) {
                "maxMetadataWorkerCount must be > 0, got ${options.maxMetadataWorkerCount}"
            }
            require(options.nodeQueueCapacity > 0) {
                "nodeQueueCapacity must be > 0, got ${options.nodeQueueCapacity}"
            }
            require(options.hashQueueCapacity > 0) {
                "hashQueueCapacity must be > 0, got ${options.hashQueueCapacity}"
            }
            require(options.netMode in 0..2) { "netMode must be 0, 1 or 2, got ${options.netMode}" }
        }
    }
}
