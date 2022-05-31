package org.universe.database.supplier

import io.lettuce.core.RedisURI
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.serializer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junitpioneer.jupiter.SetSystemProperty
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.universe.cache.CacheClient
import org.universe.container.createRedisContainer
import org.universe.database.client.createIdentity
import org.universe.database.dao.ClientIdentity
import kotlin.test.*

@Testcontainers
class CacheEntitySupplierTest : KoinTest {

    companion object {
        @JvmStatic
        @Container
        private val redisContainer = createRedisContainer()
    }

    private lateinit var cacheEntitySupplier: CacheEntitySupplier

    private lateinit var cacheClient: CacheClient

    @BeforeTest
    fun onBefore() = runBlocking {
        cacheClient = CacheClient {
            uri = RedisURI.create(redisContainer.url)
        }

        startKoin {
            modules(
                module {
                    single {
                        cacheClient
                    }
                })
        }
        cacheEntitySupplier = CacheEntitySupplier()
    }

    @AfterTest
    fun onAfter() {
        stopKoin()
        cacheClient.close()
    }

    @Nested
    @DisplayName("Get identity")
    inner class GetIdentity {

        @SetSystemProperty(key = "cache.clientId.useUUID", value = "true")
        @SetSystemProperty(key = "cache.clientId.useName", value = "false")
        @Test
        fun `data is not into the cache with uuid key`() = runBlocking {
            dataNotInCache { cacheEntitySupplier.getIdentityByUUID(it.uuid) }
        }

        @SetSystemProperty(key = "cache.clientId.useUUID", value = "false")
        @SetSystemProperty(key = "cache.clientId.useName", value = "true")
        @Test
        fun `data is not into the cache with name key`() = runBlocking {
            dataNotInCache { cacheEntitySupplier.getIdentityByName(it.name) }
        }

        private suspend inline fun dataNotInCache(getter: (ClientIdentity) -> ClientIdentity?) {
            val id = createIdentity()
            cacheEntitySupplier.saveIdentity(id)
            assertNull(getter(createIdentity()))
        }

        @SetSystemProperty(key = "cache.clientId.useUUID", value = "true")
        @SetSystemProperty(key = "cache.clientId.useName", value = "false")
        @Test
        fun `data is retrieved from the cache with uuid key`() = runBlocking {
            dataPresentsInCache { cacheEntitySupplier.getIdentityByUUID(it.uuid)!! }
        }

        @SetSystemProperty(key = "cache.clientId.useUUID", value = "false")
        @SetSystemProperty(key = "cache.clientId.useName", value = "true")
        @Test
        fun `data is retrieved from the cache with name key`() = runBlocking {
            dataPresentsInCache { cacheEntitySupplier.getIdentityByName(it.name)!! }
        }

        private suspend inline fun dataPresentsInCache(getter: (ClientIdentity) -> ClientIdentity) {
            val id = createIdentity()
            cacheEntitySupplier.saveIdentity(id)
            assertEquals(id, getter(id))
        }

        @SetSystemProperty(key = "cache.clientId.useUUID", value = "false")
        @SetSystemProperty(key = "cache.clientId.useName", value = "true")
        @SetSystemProperty(key = "cache.clientId.prefixKey", value = "c:")
        @Test
        fun `data is retrieved from the cache with name key but serial value is not valid`() = runBlocking {
            val prefixKey = "c:"
            val id = createIdentity()
            val key = id.name
            cacheClient.connect {
                val keySerial = cacheClient.binaryFormat.encodeToByteArray(String.serializer(), prefixKey + key)
                it.set(keySerial, "test".encodeToByteArray())
            }
            assertNull(cacheEntitySupplier.getIdentityByName(key))
        }

    }

    @Nested
    @DisplayName("Save identity")
    inner class SaveIdentity {

        @SetSystemProperty(key = "cache.clientId.useUUID", value = "true")
        @SetSystemProperty(key = "cache.clientId.useName", value = "false")
        @Test
        fun `save identity with uuid not exists`() = runBlocking {
            saveWithKeyNotExists(
                { it.uuid },
                { cacheEntitySupplier.getIdentityByUUID(it) }
            )
        }

        @SetSystemProperty(key = "cache.clientId.useUUID", value = "false")
        @SetSystemProperty(key = "cache.clientId.useName", value = "true")
        @Test
        fun `save identity with name not exists`() = runBlocking {
            saveWithKeyNotExists(
                { it.name },
                { cacheEntitySupplier.getIdentityByName(it) }
            )
        }

        private suspend inline fun <T> saveWithKeyNotExists(
            getKey: (ClientIdentity) -> T,
            getId: (T) -> ClientIdentity?
        ) {
            val id = createIdentity()
            val key = getKey(id)
            assertNull(getId(key))
            cacheEntitySupplier.saveIdentity(id)
            assertEquals(id, getId(key))
        }

        @SetSystemProperty(key = "cache.clientId.useUUID", value = "true")
        @SetSystemProperty(key = "cache.clientId.useName", value = "false")
        @Test
        fun `save identity but uuid already exists`() = runBlocking {
            saveWithKeyAlreadyExists(
                { it.uuid },
                { createIdentity().apply { uuid = it } },
                { cacheEntitySupplier.getIdentityByUUID(it) }
            )
        }

        @SetSystemProperty(key = "cache.clientId.useUUID", value = "false")
        @SetSystemProperty(key = "cache.clientId.useName", value = "true")
        @Test
        fun `save identity but name already exists`() = runBlocking {
            saveWithKeyAlreadyExists(
                { it.name },
                { copy(name = it) },
                { cacheEntitySupplier.getIdentityByName(it) }
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
            cacheEntitySupplier.saveIdentity(id)
            assertEquals(id, getId(key))

            val id2 = id.createId(key)
            cacheEntitySupplier.saveIdentity(id2)
            assertEquals(id2, getId(key))
        }

        @SetSystemProperty(key = "cache.clientId.useUUID", value = "true")
        @SetSystemProperty(key = "cache.clientId.useName", value = "false")
        @SetSystemProperty(key = "cache.clientId.prefixKey", value = "c:")
        @Test
        fun `data is saved using the binary format from client`(): Unit = runBlocking {
            val prefixKey = "c:"
            val id = createIdentity()
            val key = id.uuid
            cacheEntitySupplier.saveIdentity(id)

            val keySerial = cacheClient.binaryFormat.encodeToByteArray(String.serializer(), prefixKey + key)

            val value = cacheClient.connect {
                it.get(keySerial)
            }!!

            val expectedName = id.name
            assertEquals(expectedName, cacheClient.binaryFormat.decodeFromByteArray(String.serializer(), value))
            assertNotEquals(expectedName, value.decodeToString())
        }

    }
}