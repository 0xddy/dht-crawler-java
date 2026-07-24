package cn.lmcw.dht

/** IP families used by the DHT socket. */
enum class NetworkMode {
    IPV4_ONLY,
    IPV6_ONLY,
    DUAL_STACK,
}
