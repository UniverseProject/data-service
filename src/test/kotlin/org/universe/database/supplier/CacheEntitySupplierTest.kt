package org.universe.database.supplier

import io.lettuce.core.RedisClient
import kotlinx.coroutines.runBlocking
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
import org.universe.configuration.ServiceConfiguration
import org.universe.container.createRedisContainer
import org.universe.database.client.createIdentity
import org.universe.database.dao.ClientIdentity
import java.util.*
import kotlin.test.*

@Testcontainers
class CacheEntitySupplierTest : KoinTest {

    @Container
    private val redisContainer = createRedisContainer()

    private lateinit var cacheEntitySupplier: CacheEntitySupplier

    private lateinit var cacheClient: CacheClient

    @BeforeTest
    fun onBefore() {
        cacheClient = CacheClient(RedisClient.create(redisContainer.url))
        startKoin {
            modules(
                module {
                    single {
                        cacheClient
                    }
                })
        }
        cacheEntitySupplier = CacheEntitySupplier()
        ServiceConfiguration.reloadConfigurations()
    }

    @AfterTest
    fun onAfter() {
        stopKoin()
        cacheClient.pool.close()
    }

    abstract inner class CacheTest {

        @Test
        fun `data is not into the cache`() = runBlocking {
            val id = createIdentity()
            cacheEntitySupplier.saveIdentity(id)
            assertNull(getIdentity(cacheEntitySupplier, createIdentity()))
        }

        @Test
        fun `data is retrieved from the cache`() = runBlocking {
            val id = createIdentity()
            cacheEntitySupplier.saveIdentity(id)
            assertEquals(id, getIdentity(cacheEntitySupplier, id))
        }

        abstract suspend fun getIdentity(supplier: EntitySupplier, id: ClientIdentity): ClientIdentity?

    }

    @Nested
    @DisplayName("Get identity by uuid")
    @SetSystemProperty(key = "cache.clientId.useUUID", value = "true")
    @SetSystemProperty(key = "cache.clientId.useName", value = "false")
    inner class GetIdentityByUUID : CacheTest() {

        override suspend fun getIdentity(supplier: EntitySupplier, id: ClientIdentity): ClientIdentity? {
            return supplier.getIdentityByUUID(id.uuid)
        }

    }

    @Nested
    @DisplayName("Get identity by name")
    @SetSystemProperty(key = "cache.clientId.useUUID", value = "false")
    @SetSystemProperty(key = "cache.clientId.useName", value = "true")
    inner class GetIdentityByName : CacheTest() {

        override suspend fun getIdentity(supplier: EntitySupplier, id: ClientIdentity): ClientIdentity? {
            return supplier.getIdentityByName(id.name)
        }

    }

    @Nested
    @DisplayName("Save identity")
    inner class SaveIdentity {

        @Test
        @SetSystemProperty(key = "cache.clientId.useUUID", value = "true")
        @SetSystemProperty(key = "cache.clientId.useName", value = "false")
        fun `save identity with uuid not exists`() = runBlocking {
            val id = createIdentity()
            val uuid = id.uuid
            assertNull(cacheEntitySupplier.getIdentityByUUID(uuid))
            cacheEntitySupplier.saveIdentity(id)
            assertEquals(id, cacheEntitySupplier.getIdentityByUUID(uuid))
        }

        @Test
        @SetSystemProperty(key = "cache.clientId.useUUID", value = "true")
        @SetSystemProperty(key = "cache.clientId.useName", value = "false")
        fun `save identity but uuid already exists`() = runBlocking {
            val id = createIdentity()
            val idKey = id.uuid

            assertNull(cacheEntitySupplier.getIdentityByUUID(idKey))
            cacheEntitySupplier.saveIdentity(id)
            assertEquals(id, cacheEntitySupplier.getIdentityByUUID(idKey))

            val id2 = createIdentity().apply { this.uuid = idKey }
            cacheEntitySupplier.saveIdentity(id2)
            assertEquals(id2, cacheEntitySupplier.getIdentityByUUID(idKey))
        }

        @Test
        @SetSystemProperty(key = "cache.clientId.useUUID", value = "false")
        @SetSystemProperty(key = "cache.clientId.useName", value = "true")
        fun `save identity but name already exists`() = runBlocking {
            val id = createIdentity()
            val idKey = id.name

            assertNull(cacheEntitySupplier.getIdentityByName(idKey))
            cacheEntitySupplier.saveIdentity(id)
            assertEquals(id, cacheEntitySupplier.getIdentityByName(id.name))

            val id2 = id.copy(uuid = UUID.randomUUID())
            cacheEntitySupplier.saveIdentity(id2)

            assertEquals(id2, cacheEntitySupplier.getIdentityByName(id2.name))
        }

    }
}