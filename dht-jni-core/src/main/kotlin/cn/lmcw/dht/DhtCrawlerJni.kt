package cn.lmcw.dht

/** JNI static methods must match Rust symbols Java_cn_lmcw_dht_DhtCrawlerJni_* */
class DhtCrawlerJni private constructor() {
    companion object {
        init {
            NativeLoader.load()
        }

        @JvmStatic
        external fun createServer(options: Any, listener: Any?): Long

        @JvmStatic
        external fun startServer(handle: Long)

        @JvmStatic
        external fun stopServer(handle: Long)

        @JvmStatic
        external fun getNodePoolSize(handle: Long): Int
    }
}
