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
import org.universe.database.client.createIdentity
import org.universe.database.dao.ClientIdentity
import org.universe.utils.getRandomString
import kotlin.test.*

@Testcontainers
class ClientIdentityCacheServiceTest : KoinTest {

    companion object {
        @JvmStatic
        @Container
        val redisContainer = createRedisContainer()
    }

    private lateinit var service: ClientIdentityCacheService

    private lateinit var cacheClient: CacheClient

    @BeforeTest
    fun onBefore() = runBlocking {
        cacheClient = CacheClient {
            uri = RedisURI.create(redisContainer.url)
        }
    }

    private fun setService(
        cacheByUUID: Boolean,
        cacheByName: Boolean
    ) {
        service =
            ClientIdentityCacheService(
                cacheClient,
                getRandomString(),
                cacheByUUID = cacheByUUID,
                cacheByName = cacheByName
            )
    }

    @AfterTest
    fun onAfter() {
        cacheClient.close()
    }

    @Nested
    @DisplayName("Get identity")
    inner class GetIdentity {

        @Test
        fun `data is not into the cache with uuid key`() = runBlocking {
            setService(cacheByUUID = true, cacheByName = false)
            dataNotInCache { service.getByUUID(it.uuid) }
        }

        @Test
        fun `data is not into the cache with name key`() = runBlocking {
            setService(cacheByUUID = false, cacheByName = true)
            dataNotInCache { service.getByName(it.name) }
        }

        private suspend inline fun dataNotInCache(getter: (ClientIdentity) -> ClientIdentity?) {
            val id = createIdentity()
            service.save(id)
            assertNull(getter(createIdentity()))
        }

        @Test
        fun `data is retrieved from the cache with uuid key`() = runBlocking {
            setService(cacheByUUID = true, cacheByName = false)
            dataPresentsInCache { service.getByUUID(it.uuid)!! }
        }

        @Test
        fun `data is retrieved from the cache with name key`() = runBlocking {
            setService(cacheByUUID = false, cacheByName = true)
            dataPresentsInCache { service.getByName(it.name)!! }
        }

        private suspend inline fun dataPresentsInCache(getter: (ClientIdentity) -> ClientIdentity) {
            val id = createIdentity()
            service.save(id)
            assertEquals(id, getter(id))
        }

    }

    @Nested
    @DisplayName("Save identity")
    inner class SaveIdentity {

        @Test
        fun `save identity with uuid not exists`() = runBlocking {
            setService(cacheByUUID = true, cacheByName = false)
            saveWithKeyNotExists(
                { it.uuid },
                { service.getByUUID(it) }
            )
        }

        @Test
        fun `save identity with name not exists`() = runBlocking {
            setService(cacheByUUID = false, cacheByName = true)
            saveWithKeyNotExists(
                { it.name },
                { service.getByName(it) }
            )
        }

        private suspend inline fun <T> saveWithKeyNotExists(
            getKey: (ClientIdentity) -> T,
            getId: (T) -> ClientIdentity?
        ) {
            val id = createIdentity()
            val key = getKey(id)
            assertNull(getId(key))
            service.save(id)
            assertEquals(id, getId(key))
        }

        @Test
        fun `save identity but uuid already exists`() = runBlocking {
            setService(cacheByUUID = true, cacheByName = false)
            saveWithKeyAlreadyExists(
                { it.uuid },
                { createIdentity().apply { uuid = it } },
                { service.getByUUID(it) }
            )
        }

        @Test
        fun `save identity but name already exists`() = runBlocking {
            setService(cacheByUUID = false, cacheByName = true)
            saveWithKeyAlreadyExists(
                { it.name },
                { copy(name = it) },
                { service.getByName(it) }
            )
        }

        private suspend inline fun <T> saveWithKeyAlreadyExists(
            getKey: (ClientIdentity) -> T,
            createId: ClientIdentity.(T) -> ClientIdentity,
            getId: (T) -> ClientIdentity?
        ) {
            val id = createIdentity()
            val key = getKey(id)

            assertNull(getId(key))
            service.save(id)
            assertEquals(id, getId(key))

            val id2 = id.createId(key)
            service.save(id2)
            assertEquals(id2, getId(key))
        }

    }
}