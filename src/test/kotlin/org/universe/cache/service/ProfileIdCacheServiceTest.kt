package org.universe.cache.service

import io.lettuce.core.RedisURI
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.koin.test.KoinTest
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.universe.cache.CacheClient
import org.universe.container.createRedisContainer
import org.universe.utils.createProfileId
import org.universe.utils.getRandomString
import kotlin.test.*

@Testcontainers
class ProfileIdCacheServiceTest : KoinTest {

    companion object {
        @JvmStatic
        @Container
        val redisContainer = createRedisContainer()
    }

    private lateinit var service: ProfileIdCacheService

    private lateinit var cacheClient: CacheClient

    @BeforeTest
    fun onBefore() = runBlocking {
        cacheClient = CacheClient {
            uri = RedisURI.create(ClientIdentityCacheServiceTest.redisContainer.url)
        }
        service = ProfileIdCacheService(cacheClient, getRandomString())
    }

    @AfterTest
    fun onAfter() {
        cacheClient.close()
    }

    @Nested
    @DisplayName("Get identity")
    inner class GetIdentity {

        @Test
        fun `data is not into the cache`() = runBlocking {
            val id = createProfileId()
            service.save(id)
            assertNull(service.getByName(getRandomString()))
        }

        @Test
        fun `data is retrieved from the cache`() = runBlocking {
            val id = createProfileId()
            service.save(id)
            assertEquals(id, service.getByName(id.name))
        }

    }

    @Nested
    @DisplayName("Save")
    inner class Save {

        @Test
        fun `save identity with key not exists`() = runBlocking {
            val id = createProfileId()
            val key = id.name
            assertNull(service.getByName(key))
            service.save(id)
            assertEquals(id, service.getByName(key))
        }

        @Test
        fun `save identity but key already exists`() = runBlocking {
            val id = createProfileId()
            val key = id.name

            assertNull(service.getByName(key))
            service.save(id)
            assertEquals(id, service.getByName(key))

            val id2 = id.copy(name = key)
            service.save(id2)
            assertEquals(id2, service.getByName(key))
        }

    }
}