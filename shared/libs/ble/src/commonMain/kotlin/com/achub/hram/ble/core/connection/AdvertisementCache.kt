package com.achub.hram.ble.core.connection

import com.achub.hram.ble.ADVERTISEMENT_CACHE_TTL_MS
import com.juul.kable.Advertisement
import com.juul.kable.Identifier
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.TimeSource
import kotlin.uuid.ExperimentalUuidApi

private data class CachedEntry(val advertisement: Advertisement, val seenAt: kotlin.time.TimeMark)

internal class AdvertisementCache(private val clock: TimeSource = TimeSource.Monotonic) {
    private val mutex = Mutex()
    private val cache = mutableMapOf<String, CachedEntry>()

    suspend fun clear() = mutex.withLock { cache.clear() }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun put(advertisement: Advertisement) = mutex.withLock {
        cache[advertisement.identifier.toString()] = CachedEntry(
            advertisement = advertisement,
            seenAt = clock.markNow(),
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun get(identifier: Identifier): Advertisement? = mutex.withLock {
        val entry = cache[identifier.toString()] ?: return@withLock null
        if (entry.seenAt.elapsedNow().inWholeMilliseconds <= ADVERTISEMENT_CACHE_TTL_MS) {
            entry.advertisement
        } else {
            null
        }
    }
}
