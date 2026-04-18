package com.achub.hram.ble.core.connection

import com.achub.hram.ble.ADVERTISEMENT_CACHE_TTL_MS
import com.achub.hram.ble.identifier
import com.juul.kable.Advertisement
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class AdvertisementCacheTest {
    private val identifier = identifier("test-device-id")
    private lateinit var advertisementMock: Advertisement

    @BeforeTest
    fun setup() {
        advertisementMock = mock(MockMode.autofill)
        every { advertisementMock.identifier } returns identifier
    }

    @Test
    fun `get - returns null when cache is empty`() = runTest {
        val cache = AdvertisementCache()
        assertNull(cache.get(identifier))
    }

    @Test
    fun `get - returns advertisement after put`() = runTest {
        val cache = AdvertisementCache()
        cache.put(advertisementMock)
        assertEquals(advertisementMock, cache.get(identifier))
    }

    @Test
    fun `get - returns null when entry is expired`() = runTest {
        val clock = TestTimeSource()
        val cache = AdvertisementCache(clock)
        cache.put(advertisementMock)
        clock += (ADVERTISEMENT_CACHE_TTL_MS + 1).milliseconds
        assertNull(cache.get(identifier))
    }

    @Test
    fun `get - returns advertisement when entry is exactly at TTL boundary`() = runTest {
        val clock = TestTimeSource()
        val cache = AdvertisementCache(clock)
        cache.put(advertisementMock)
        clock += ADVERTISEMENT_CACHE_TTL_MS.milliseconds
        assertEquals(advertisementMock, cache.get(identifier))
    }

    @Test
    fun `clear - removes all entries`() = runTest {
        val cache = AdvertisementCache()
        cache.put(advertisementMock)
        cache.clear()
        assertNull(cache.get(identifier))
    }

    @Test
    fun `put - overwrites existing entry and resets TTL`() = runTest {
        val clock = TestTimeSource()
        val cache = AdvertisementCache(clock)
        val anotherMock: Advertisement = mock(MockMode.autofill)
        every { anotherMock.identifier } returns identifier
        cache.put(advertisementMock)
        // advance close to expiry
        clock += (ADVERTISEMENT_CACHE_TTL_MS - 100).milliseconds
        // refresh the entry
        cache.put(anotherMock)
        // advance past original TTL
        clock += 200.milliseconds
        // should still be valid because the second put reset the TTL
        assertEquals(anotherMock, cache.get(identifier))
    }

    @Test
    fun `clear - does not affect entries added after clear`() = runTest {
        val cache = AdvertisementCache()
        cache.put(advertisementMock)
        cache.clear()
        cache.put(advertisementMock)
        assertEquals(advertisementMock, cache.get(identifier))
    }
}
