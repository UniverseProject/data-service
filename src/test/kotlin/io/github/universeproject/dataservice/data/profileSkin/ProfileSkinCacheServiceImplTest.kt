@file:OptIn(ExperimentalLettuceCoroutinesApi::class)

package io.github.universeproject.dataservice.data.profileSkin

import io.github.universeproject.ProfileSkin
import io.github.universeproject.dataservice.cache.CacheClient
import io.github.universeproject.dataservice.container.createRedisContainer
import io.github.universeproject.dataservice.data.ProfileSkinCacheServiceImpl
import io.github.universeproject.dataservice.utils.createProfileSkin
import io.github.universeproject.dataservice.utils.getRandomString
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisURI
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.serializer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.*

@Testcontainers
class ProfileSkinCacheServiceImplTest {

    companion object {
        @JvmStatic
        @Container
        private val redisContainer = createRedisContainer()
    }

    private lateinit var service: ProfileSkinCacheServiceImpl

    private lateinit var cacheClient: CacheClient

    @BeforeTest
    fun onBefore() = runBlocking {
        cacheClient = CacheClient {
            uri = RedisURI.create(redisContainer.url)
        }
        service = ProfileSkinCacheServiceImpl(cacheClient, getRandomString())
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