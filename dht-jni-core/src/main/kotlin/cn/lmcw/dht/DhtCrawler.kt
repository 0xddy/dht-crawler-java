package cn.lmcw.dht

import cn.lmcw.dht.internal.NativeListener
import cn.lmcw.dht.internal.NativeOptions

/**
 * One native DHT crawler session.
 *
 * Create it with [dhtCrawler]. [start] is non-blocking; [stop] and [close] are idempotent.
 */
class DhtCrawler internal constructor(
    @Volatile private var handle: Long,
    val config: DhtConfig,
) : AutoCloseable {
    private val sync = Any()

    @Volatile
    private var currentState: State = State.CREATED

    val state: State
        get() = synchronized(sync) { currentState }

    val isRunning: Boolean
        get() = state == State.RUNNING

    val isOpen: Boolean
        get() = state != State.CLOSED

    /** Current number of nodes retained by the native crawler. */
    val nodeCount: Int
        get() = synchronized(sync) {
            if (handle == 0L) 0 else DhtCrawlerJni.getNodePoolSize(handle)
        }

    /** Starts the crawler in the background and returns this session for concise chaining. */
    fun start(): DhtCrawler {
        synchronized(sync) {
            check(handle != 0L) { "Crawler is closed and cannot be started" }
            if (currentState == State.RUNNING) return this
            DhtCrawlerJni.startServer(handle)
            currentState = State.RUNNING
            return this
        }
    }

    /** Stops this session and schedules native runtime cleanup. */
    fun stop() {
        val handleToStop = synchronized(sync) {
            if (handle == 0L) return
            val value = handle
            handle = 0L
            currentState = State.CLOSED
            value
        }
        DhtCrawlerJni.stopServer(handleToStop)
    }

    override fun close() = stop()

    enum class State {
        CREATED,
        RUNNING,
        CLOSED,
    }

    internal companion object {
        fun create(config: DhtConfig, callbacks: DhtCallbacks): DhtCrawler {
            val listener = callbacks.toNativeListenerOrNull()
            val handle = DhtCrawlerJni.createServer(NativeOptions(config), listener)
            check(handle != 0L) { "Native DHT crawler creation failed" }
            return DhtCrawler(handle, config)
        }

        private fun DhtCallbacks.toNativeListenerOrNull(): NativeListener? =
            if (onTorrent == null && onError == null && metadataFilter == null) {
                null
            } else {
                NativeListener(this)
            }
    }
}
