package cn.lmcw.dht

import cn.lmcw.dht.model.DHTOptions

/** JNI static methods must match Rust symbols Java_cn_lmcw_dht_DhtCrawlerJni_* */
class DhtCrawlerJni private constructor() {
    companion object {
        init {
            NativeLoader.load()
        }

        @JvmStatic
        external fun createServer(options: DHTOptions?, listener: DhtListener?): Long

        @JvmStatic
        external fun startServer(handle: Long)

        @JvmStatic
        external fun stopServer(handle: Long)

        @JvmStatic
        external fun getNodePoolSize(handle: Long): Int
    }
}
