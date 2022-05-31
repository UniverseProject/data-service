package org.universe.cache.service

import io.lettuce.core.RedisURI
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.serializer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.koin.test.KoinTest
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.universe.cache.CacheClient
import org.universe.container.createRedisContainer
import org.universe.model.ProfileSkin
import org.universe.utils.createProfileSkin
import org.universe.utils.getRandomString
import kotlin.test.*

@Testcontainers
class ProfileSkinCacheServiceTest : KoinTest {

    companion object {
        @JvmStatic
        @Container
        private val redisContainer = createRedisContainer()
    }

    private lateinit var service: ProfileSkinCacheService

    private lateinit var cacheClient: CacheClient

    @BeforeTest
    fun onBefore() = runBlocking {
        cacheClient = CacheClient {
            uri = RedisURI.create(redisContainer.url)
        }
        service = ProfileSkinCacheService(cacheClient, getRandomString())
    }

    @AfterTest
    fun onAfter() {
        cacheClient.close()
    }

    @Nested
    @DisplayName("Get")
    inner class Get {

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

        @Test
        fun `data is retrieved from the cache with name key but serial value is not valid`() = runBlocking {
            val profile = createProfileSkin()
            val key = profile.id
            cacheClient.connect {
                val keySerial = cacheClient.binaryFormat.encodeToByteArray(String.serializer(), service.prefixKey + key)
                it.set(keySerial, "test".encodeToByteArray())
            }
            assertNull(service.getByUUID(key))
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

        @Test
        fun `data is saved using the binary format from client`(): Unit = runBlocking {
            val profile = createProfileSkin()
            val key = profile.id
            service.save(profile)

            val keySerial = cacheClient.binaryFormat.encodeToByteArray(String.serializer(), service.prefixKey + key)

            val value = cacheClient.connect {
                it.get(keySerial)
            }!!

            assertEquals(profile, cacheClient.binaryFormat.decodeFromByteArray(ProfileSkin.serializer(), value))
        }

    }
}