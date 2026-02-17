package tr.akguel.interceptor

import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

/**
 * Thread-safe store for request start times.
 * Used to calculate round-trip latency between send_request and receive_reply.
 *
 * Entries are automatically removed after retrieval to prevent memory leaks.
 * A background cleanup is not needed since entries are short-lived.
 */
object RequestTimingStore {
    private val timings = ConcurrentHashMap<String, Long>()

    fun setStartTime(requestId: ByteArray?, nanoTime: Long) {
        val key: String = formatKey(requestId)
        timings[key] = nanoTime
    }

    fun getAndRemoveStartTime(key: String): Long? {
        return timings.remove(key)
    }

    fun getAndRemoveStartTime(requestId: ByteArray?): Long? {
        return timings.remove(formatKey(requestId))
    }

    private fun formatKey(requestId: ByteArray?): String {
        if (requestId == null || requestId.isEmpty()) return "unknown"
        val sb = StringBuilder()
        for (i in 0..<min(requestId.size, 16)) {
            sb.append(String.format("%02x", requestId[i].toInt() and 0xFF))
        }
        return sb.toString()
    }

    fun size(): Int {
        return timings.size
    }

    fun clear() {
        timings.clear()
    }
}