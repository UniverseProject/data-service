package io.github.universeproject.dataservice.data.profileId

import io.lettuce.core.RedisURI
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.serializer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import io.github.universeproject.dataservice.cache.CacheClient
import org.universe.dataservice.container.createRedisContainer
import org.universe.dataservice.data.ProfileIdCacheServiceImpl
import org.universe.dataservice.utils.createProfileId
import org.universe.dataservice.utils.getRandomString
import kotlin.test.*

@Testcontainers
class ProfileIdCacheServiceImplTest {

    companion object {
        @JvmStatic
        @Container
        private val redisContainer = createRedisContainer()
    }

    private lateinit var service: ProfileIdCacheServiceImpl

    private lateinit var cacheClient: io.github.universeproject.dataservice.cache.CacheClient

    @BeforeTest
    fun onBefore() = runBlocking {
        cacheClient = io.github.universeproject.dataservice.cache.CacheClient {
            uri = RedisURI.create(redisContainer.url)
        }
        service = ProfileIdCacheServiceImpl(cacheClient, getRandomString())
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
            val profile = createProfileId()
            service.save(profile)
            assertNull(service.getByName(getRandomString()))
        }

        @Test
        fun `data is retrieved from the cache`() = runBlocking {
            val profile = createProfileId()
            service.save(profile)
            assertEquals(profile, service.getByName(profile.name))
        }

        @Test
        fun `data is retrieved from the cache with name key but serial value is not valid`() = runBlocking {
            val profile = createProfileId()
            val key = profile.name
            cacheClient.connect {
                val keySerial = cacheClient.binaryFormat.encodeToByteArray(String.serializer(), service.prefixKey + key)
                it.set(keySerial, "test".encodeToByteArray())
            }
            assertNull(service.getByName(key))
        }

    }

    @Nested
    @DisplayName("Save")
    inner class Save {

        @Test
        fun `save identity with key not exists`() = runBlocking {
            val profile = createProfileId()
            val key = profile.name
            assertNull(service.getByName(key))
            service.save(profile)
            assertEquals(profile, service.getByName(key))
        }

        @Test
        fun `save identity but key already exists`() = runBlocking {
            val profile = createProfileId()
            val key = profile.name

            assertNull(service.getByName(key))
            service.save(profile)
            assertEquals(profile, service.getByName(key))

            val id2 = profile.copy(name = key)
            service.save(id2)
            assertEquals(id2, service.getByName(key))
        }

        @Test
        fun `data is saved using the binary format from client`(): Unit = runBlocking {
            val profile = createProfileId()
            val key = profile.name
            service.save(profile)

            val keySerial = cacheClient.binaryFormat.encodeToByteArray(String.serializer(), service.prefixKey + key)

            val value = cacheClient.connect {
                it.get(keySerial)
            }!!

            val expected = profile.id
            assertEquals(expected, cacheClient.binaryFormat.decodeFromByteArray(String.serializer(), value))
            assertNotEquals(expected, value.decodeToString())
        }

    }
}