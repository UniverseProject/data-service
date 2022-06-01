package org.universe.dataservice.data.clientIdentity

import io.lettuce.core.RedisURI
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.serializer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.universe.dataservice.container.createRedisContainer
import org.universe.dataservice.data.ClientIdentity
import org.universe.dataservice.data.ClientIdentityCacheServiceImpl
import org.universe.dataservice.utils.createIdentity
import org.universe.dataservice.utils.getRandomString
import kotlin.test.*

@Testcontainers
class ClientIdentityCacheServiceImplTest : KoinTest {

    companion object {
        @JvmStatic
        @Container
        private val redisContainer = createRedisContainer()
    }

    private lateinit var service: ClientIdentityCacheServiceImpl

    private lateinit var cacheClient: org.universe.dataservice.cache.CacheClient

    @BeforeTest
    fun onBefore() = runBlocking<Unit> {
        cacheClient = org.universe.dataservice.cache.CacheClient {
            uri = RedisURI.create(redisContainer.url)
        }

        startKoin {
            modules(
                module {
                    single { cacheClient }
                })
        }
    }

    @AfterTest
    fun onAfter() {
        cacheClient.close()
        stopKoin()
    }

    private fun setService(
        cacheByUUID: Boolean,
        cacheByName: Boolean
    ) {
        service =
            ClientIdentityCacheServiceImpl(
                getRandomString(),
                cacheByUUID = cacheByUUID,
                cacheByName = cacheByName
            )
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

        @Test
        fun `data is retrieved from the cache with name key but serial value is not valid`() = runBlocking {
            setService(cacheByUUID = false, cacheByName = true)
            val id = createIdentity()
            val key = id.name
            cacheClient.connect {
                val keySerial = cacheClient.binaryFormat.encodeToByteArray(String.serializer(), service.prefixKey + key)
                it.set(keySerial, "test".encodeToByteArray())
            }
            assertNull(service.getByName(key))
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
                { createIdentity().apply { name = it } },
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

        @Test
        fun `data is saved using the binary format from client`(): Unit = runBlocking {
            setService(cacheByUUID = true, cacheByName = false)
            val id = createIdentity()
            val key = id.uuid
            service.save(id)

            val keySerial = cacheClient.binaryFormat.encodeToByteArray(String.serializer(), service.prefixKey + key)

            val value = cacheClient.connect {
                it.get(keySerial)
            }!!

            val expectedName = id.name
            assertEquals(expectedName, cacheClient.binaryFormat.decodeFromByteArray(String.serializer(), value))
            assertNotEquals(expectedName, value.decodeToString())
        }

    }
}