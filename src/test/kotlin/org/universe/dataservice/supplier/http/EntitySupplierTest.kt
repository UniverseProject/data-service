package org.universe.dataservice.supplier.http

import io.lettuce.core.RedisURI
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.universe.dataservice.container.createRedisContainer
import org.universe.dataservice.data.MojangAPI
import org.universe.dataservice.utils.createProfileId
import org.universe.dataservice.utils.getRandomString
import kotlin.test.*

@Testcontainers
class EntitySupplierCompanionTest : KoinTest {

    companion object {
        @JvmStatic
        @Container
        private val redisContainer = createRedisContainer()
    }

    private lateinit var cacheClient: org.universe.dataservice.cache.CacheClient

    private val mojangAPI: MojangAPI by inject()
    private lateinit var cacheEntitySupplier: CacheEntitySupplier

    @BeforeTest
    fun onBefore() = runBlocking {
        cacheClient = org.universe.dataservice.cache.CacheClient {
            uri = RedisURI.create(redisContainer.url)
        }

        startKoin {
            modules(
                module {
                    single { mockk<MojangAPI>(getRandomString()) } bind MojangAPI::class
                    single { cacheClient }
                })
        }
        cacheEntitySupplier = CacheEntitySupplier()
    }

    @AfterTest
    fun onAfter() {
        stopKoin()
        cacheClient.close()
    }

    @Test
    fun `rest supplier corresponding to the class`() {
        assertEquals(RestEntitySupplier::class, EntitySupplier.rest::class)
    }

    @Test
    fun `cache supplier corresponding to the class`() {
        assertEquals(CacheEntitySupplier::class, EntitySupplier.cache::class)
    }

    @Nested
    @DisplayName("Caching rest")
    inner class CachingRest : KoinTest {

        private lateinit var supplier: EntitySupplier

        @BeforeTest
        fun onBefore() {
            supplier = EntitySupplier.cachingRest
        }

        @Test
        fun `data found in rest is saved into cache`() = runBlocking {
            val profileId = createProfileId()
            val name = profileId.name
            coEvery { mojangAPI.getId(name) } returns profileId
            assertEquals(profileId, supplier.getId(name))
            coVerify(exactly = 1) { mojangAPI.getId(name) }
            assertEquals(profileId, cacheEntitySupplier.getId(name))
        }

        @Test
        fun `data present in cache is not used to find value`() = runBlocking {
            val profileId = createProfileId()
            val name = profileId.name
            cacheEntitySupplier.save(profileId)

            coEvery { mojangAPI.getId(name) } returns profileId
            assertEquals(profileId, supplier.getId(name))
            coVerify(exactly = 1) { mojangAPI.getId(name) }
        }
    }

    @Nested
    @DisplayName("Cache with Rest fallback")
    inner class CacheWithRestFallback : KoinTest {

        private lateinit var supplier: EntitySupplier

        @BeforeTest
        fun onBefore() {
            supplier = EntitySupplier.cacheWithRestFallback
        }

        @Test
        fun `data found in rest is not saved into cache`() = runBlocking {
            val profileId = createProfileId()
            val name = profileId.name
            coEvery { mojangAPI.getId(name) } returns profileId
            assertEquals(profileId, supplier.getId(name))
            coVerify(exactly = 1) { mojangAPI.getId(name) }
            assertNull(cacheEntitySupplier.getId(name))
        }

        @Test
        fun `data present in cache is use to avoid rest call`() = runBlocking {
            val profileId = createProfileId()
            val name = profileId.name
            cacheEntitySupplier.save(profileId)

            coEvery { mojangAPI.getId(name) } returns profileId
            assertEquals(profileId, supplier.getId(name))
            coVerify(exactly = 0) { mojangAPI.getId(name) }
        }
    }

    @Nested
    @DisplayName("Cache with caching result of Rest fallback")
    inner class CacheWithCachingRestFallback : KoinTest {

        private lateinit var supplier: EntitySupplier

        @BeforeTest
        fun onBefore() {
            supplier = EntitySupplier.cacheWithCachingRestFallback
        }

        @Test
        fun `data found in rest is saved into cache`() = runBlocking {
            val profileId = createProfileId()
            val name = profileId.name
            coEvery { mojangAPI.getId(name) } returns profileId
            assertEquals(profileId, supplier.getId(name))
            coVerify(exactly = 1) { mojangAPI.getId(name) }
            assertEquals(profileId, cacheEntitySupplier.getId(name))
        }

        @Test
        fun `data present in cache is use to avoid rest call`() = runBlocking {
            val profileId = createProfileId()
            val name = profileId.name
            cacheEntitySupplier.save(profileId)

            coEvery { mojangAPI.getId(name) } returns profileId
            assertEquals(profileId, supplier.getId(name))
            coVerify(exactly = 0) { mojangAPI.getId(name) }
        }
    }

}
