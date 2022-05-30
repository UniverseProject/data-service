package org.universe.database.supplier

import io.lettuce.core.RedisURI
import io.mockk.MockKMatcherScope
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.mockk
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
import kotlin.test.*

@Testcontainers
class StoreEntitySupplierTest : KoinTest {

    companion object {
        @JvmStatic
        @Container
        val redisContainer = createRedisContainer()
    }

    private lateinit var cacheClient: CacheClient

    private lateinit var storeEntitySupplier: StoreEntitySupplier
    private lateinit var mockSupplier: EntitySupplier
    private lateinit var cacheEntitySupplier: CacheEntitySupplier

    @BeforeTest
    fun onBefore() {
        ServiceConfiguration.reloadConfigurations()
        cacheClient = CacheClient(RedisURI.create(redisContainer.url))

        startKoin {
            modules(
                module {
                    single { cacheClient }
                })
        }
        mockSupplier = mockk()
        storeEntitySupplier = StoreEntitySupplier(mockSupplier)
        // Use to verify if data is inserted
        cacheEntitySupplier = CacheEntitySupplier()
    }

    @AfterTest
    fun onAfter() {
        stopKoin()
        cacheClient.close()
    }

    abstract inner class StoreTest {

        @Test
        fun `data not stored into cache if data not exists`() = runBlocking {
            val id = createIdentity()
            coEvery { mockGetMethod(mockSupplier) } returns null
            assertNull(getIdentity(storeEntitySupplier, id))
            assertNull(getIdentity(cacheEntitySupplier, id))
        }

        @Test
        fun `data stored if found`() = runBlocking {
            val id = createIdentity()
            coEvery { mockGetMethod(mockSupplier) } returns id
            assertEquals(id, getIdentity(storeEntitySupplier, id))
            assertEquals(id, getIdentity(cacheEntitySupplier, id))
        }

        abstract suspend fun getIdentity(supplier: EntitySupplier, id: ClientIdentity): ClientIdentity?

        abstract suspend fun MockKMatcherScope.mockGetMethod(supplier: EntitySupplier): ClientIdentity?
    }

    @Nested
    @DisplayName("Get identity by uuid")
    @SetSystemProperty(key = "cache.clientId.useUUID", value = "true")
    @SetSystemProperty(key = "cache.clientId.useName", value = "false")
    inner class GetIdentityByUUID : StoreTest() {

        override suspend fun getIdentity(supplier: EntitySupplier, id: ClientIdentity): ClientIdentity? {
            return supplier.getIdentityByUUID(id.uuid)
        }

        override suspend fun MockKMatcherScope.mockGetMethod(supplier: EntitySupplier): ClientIdentity? {
            return supplier.getIdentityByUUID(any())
        }

    }

    @Nested
    @DisplayName("Get identity by name")
    @SetSystemProperty(key = "cache.clientId.useUUID", value = "false")
    @SetSystemProperty(key = "cache.clientId.useName", value = "true")
    inner class GetIdentityByName : StoreTest() {

        override suspend fun getIdentity(supplier: EntitySupplier, id: ClientIdentity): ClientIdentity? {
            return supplier.getIdentityByName(id.name)
        }

        override suspend fun MockKMatcherScope.mockGetMethod(supplier: EntitySupplier): ClientIdentity? {
            return supplier.getIdentityByName(any())
        }

    }

    @Nested
    @DisplayName("Save identity")
    @SetSystemProperty(key = "cache.clientId.useUUID", value = "true")
    @SetSystemProperty(key = "cache.clientId.useName", value = "false")
    inner class SaveIdentity {

        @Test
        fun `save identity on supplier will not save into the store supplier`() = runBlocking {
            val id = createIdentity()
            val uuid = id.uuid
            coJustRun { mockSupplier.saveIdentity(id) }
            storeEntitySupplier.saveIdentity(id)

            assertNull(cacheEntitySupplier.getIdentityByUUID(uuid))
        }

    }
}