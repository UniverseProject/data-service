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
import org.universe.utils.createProfileSkin
import org.universe.utils.getRandomString
import kotlin.test.*

@Testcontainers
class ProfileSkinCacheServiceTest : KoinTest {

    companion object {
        @JvmStatic
        @Container
        val redisContainer = createRedisContainer()
    }

    private lateinit var service: ProfileSkinCacheService

    private lateinit var cacheClient: CacheClient

    @BeforeTest
    fun onBefore() {
        cacheClient = CacheClient(RedisURI.create(redisContainer.url))
        service = ProfileSkinCacheService(cacheClient, getRandomString())
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
            val profile = createProfileSkin()
            service.save(profile)
            assertNull(service.getByUUID(getRandomString()))
        }

        @Test
        fun `data is retrieved from the cache`() = runBlocking {
            val profile = createProfileSkin()
            service.save(profile)
            assertEquals(profile, service.getByUUID(profile.id))
        }

    }

    @Nested
    @DisplayName("Save")
    inner class Save {

        @Test
        fun `save identity with key not exists`() = runBlocking {
            val profile = createProfileSkin()
            val key = profile.id
            assertNull(service.getByUUID(key))
            service.save(profile)
            assertEquals(profile, service.getByUUID(key))
        }

        @Test
        fun `save identity but key already exists`() = runBlocking {
            val profile = createProfileSkin()
            val key = profile.id

            assertNull(service.getByUUID(key))
            service.save(profile)
            assertEquals(profile, service.getByUUID(key))

            val profile2 = profile.copy(id = key)
            service.save(profile2)
            assertEquals(profile2, service.getByUUID(key))
        }

    }
}