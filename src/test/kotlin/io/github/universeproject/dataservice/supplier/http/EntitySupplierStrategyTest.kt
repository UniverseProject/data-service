package io.github.universeproject.dataservice.supplier.http

import io.github.universeproject.MojangAPI
import io.lettuce.core.RedisURI
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import io.github.universeproject.dataservice.cache.CacheClient
import org.universe.dataservice.container.createRedisContainer
import org.universe.dataservice.data.ProfileIdCacheServiceImpl
import org.universe.dataservice.data.ProfileSkinCacheServiceImpl
import org.universe.dataservice.supplier.SupplierConfiguration
import org.universe.dataservice.utils.createProfileId
import org.universe.dataservice.utils.getRandomString
import kotlin.test.*

@Testcontainers
class EntitySupplierStrategyTest {

    companion object {
        @JvmStatic
        @Container
        private val redisContainer = createRedisContainer()
    }

    private lateinit var cacheClient: io.github.universeproject.dataservice.cache.CacheClient

    private lateinit var configuration: SupplierConfiguration

    private lateinit var mojangAPI: MojangAPI
    private lateinit var cacheEntitySupplier: CacheEntitySupplier

    @BeforeTest
    fun onBefore() = runBlocking {
        cacheClient = io.github.universeproject.dataservice.cache.CacheClient {
            uri = RedisURI.create(redisContainer.url)
        }

        mojangAPI = mockk(getRandomString())

        cacheEntitySupplier = CacheEntitySupplier(
            ProfileSkinCacheServiceImpl(cacheClient),
            ProfileIdCacheServiceImpl(cacheClient)
        )
        configuration = SupplierConfiguration(mojangAPI, cacheClient)
    }

    @AfterTest
    fun onAfter() {
        cacheClient.close()
    }

    @Test
    fun `rest supplier corresponding to the class`() {
        val configuration = SupplierConfiguration(mockk(), mockk())
        assertEquals(RestEntitySupplier::class, EntitySupplier.rest(configuration)::class)
    }

    @Test
    fun `cache supplier corresponding to the class`() {
        val configuration = SupplierConfiguration(mockk(), mockk())
        assertEquals(CacheEntitySupplier::class, EntitySupplier.cache(configuration)::class)
    }

    @Nested
    @DisplayName("Caching rest")
    inner class CachingRest {

        private lateinit var supplier: EntitySupplier

        @BeforeTest
        fun onBefore() {
            supplier = EntitySupplier.cachingRest(configuration)
        }

        @Test
        fun `data found in rest is saved into cache`() = runBlocking {
            val profileId = createProfileId()
            val name = profileId.name
            coEvery { mojangAPI.getUUID(name) } returns profileId
            assertEquals(profileId, supplier.getUUID(name))
            coVerify(exactly = 1) { mojangAPI.getUUID(name) }
            assertEquals(profileId, cacheEntitySupplier.getUUID(name))
        }

        @Test
        fun `data present in cache is not used to find value`() = runBlocking {
            val profileId = createProfileId()
            val name = profileId.name
            cacheEntitySupplier.save(profileId)

            coEvery { mojangAPI.getUUID(name) } returns profileId
            assertEquals(profileId, supplier.getUUID(name))
            coVerify(exactly = 1) { mojangAPI.getUUID(name) }
        }
    }

    @Nested
    @DisplayName("Cache with Rest fallback")
    inner class CacheWithRestFallback {

        private lateinit var supplier: EntitySupplier

        @BeforeTest
        fun onBefore() {
            supplier = EntitySupplier.cacheWithRestFallback(configuration)
        }

        @Test
        fun `data found in rest is not saved into cache`() = runBlocking {
            val profileId = createProfileId()
            val name = profileId.name
            coEvery { mojangAPI.getUUID(name) } returns profileId
            assertEquals(profileId, supplier.getUUID(name))
            coVerify(exactly = 1) { mojangAPI.getUUID(name) }
            assertNull(cacheEntitySupplier.getUUID(name))
        }

        @Test
        fun `data present in cache is use to avoid rest call`() = runBlocking {
            val profileId = createProfileId()
            val name = profileId.name
            cacheEntitySupplier.save(profileId)

            coEvery { mojangAPI.getUUID(name) } returns profileId
            assertEquals(profileId, supplier.getUUID(name))
            coVerify(exactly = 0) { mojangAPI.getUUID(name) }
        }
    }

    @Nested
    @DisplayName("Cache with caching result of Rest fallback")
    inner class CacheWithCachingRestFallback {

        private lateinit var supplier: EntitySupplier

        @BeforeTest
        fun onBefore() {
            supplier = EntitySupplier.cacheWithCachingRestFallback(configuration)
        }

        @Test
        fun `data found in rest is saved into cache`() = runBlocking {
            val profileId = createProfileId()
            val name = profileId.name
            coEvery { mojangAPI.getUUID(name) } returns profileId
            assertEquals(profileId, supplier.getUUID(name))
            coVerify(exactly = 1) { mojangAPI.getUUID(name) }
            assertEquals(profileId, cacheEntitySupplier.getUUID(name))
        }

        @Test
        fun `data present in cache is use to avoid rest call`() = runBlocking {
            val profileId = createProfileId()
            val name = profileId.name
            cacheEntitySupplier.save(profileId)

            coEvery { mojangAPI.getUUID(name) } returns profileId
            assertEquals(profileId, supplier.getUUID(name))
            coVerify(exactly = 0) { mojangAPI.getUUID(name) }
        }
    }

}
