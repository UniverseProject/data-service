package org.universe.dataservice.supplier.database

import io.lettuce.core.RedisURI
import io.mockk.MockKMatcherScope
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.universe.dataservice.cache.CacheClient
import org.universe.dataservice.container.createRedisContainer
import org.universe.dataservice.data.ClientIdentity
import org.universe.dataservice.data.ClientIdentityCacheServiceImpl
import org.universe.dataservice.utils.createIdentity
import kotlin.test.*

@Testcontainers
class StoreEntitySupplierTest {

    companion object {
        @JvmStatic
        @Container
        private val redisContainer = createRedisContainer()
    }

    private lateinit var cacheClient: CacheClient
    private lateinit var mockSupplier: EntitySupplier

    @BeforeTest
    fun onBefore(): Unit = runBlocking {
        cacheClient = CacheClient {
            uri = RedisURI.create(redisContainer.url)
        }
        mockSupplier = mockk()
    }

    @AfterTest
    fun onAfter() {
        cacheClient.close()
    }

    abstract inner class StoreTest {

        private lateinit var cacheEntitySupplier: CacheEntitySupplier

        abstract fun useName(): Boolean

        abstract fun useUUID(): Boolean

        private lateinit var storeEntitySupplier: StoreEntitySupplier

        @BeforeTest
        fun onBefore() {
            cacheEntitySupplier = CacheEntitySupplier(
                ClientIdentityCacheServiceImpl(
                    cacheClient,
                    cacheByUUID = useUUID(),
                    cacheByName = useName()
                )
            )

            storeEntitySupplier = StoreEntitySupplier(cacheEntitySupplier, mockSupplier)
        }

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
    inner class GetIdentityByUUID : StoreTest() {

        override fun useName(): Boolean = false

        override fun useUUID(): Boolean = true

        override suspend fun getIdentity(supplier: EntitySupplier, id: ClientIdentity): ClientIdentity? {
            return supplier.getIdentityByUUID(id.uuid)
        }

        override suspend fun MockKMatcherScope.mockGetMethod(supplier: EntitySupplier): ClientIdentity? {
            return supplier.getIdentityByUUID(any())
        }

    }

    @Nested
    @DisplayName("Get identity by name")
    inner class GetIdentityByName : StoreTest() {

        override fun useName(): Boolean = true

        override fun useUUID(): Boolean = false

        override suspend fun getIdentity(supplier: EntitySupplier, id: ClientIdentity): ClientIdentity? {
            return supplier.getIdentityByName(id.name)
        }

        override suspend fun MockKMatcherScope.mockGetMethod(supplier: EntitySupplier): ClientIdentity? {
            return supplier.getIdentityByName(any())
        }

    }

    @Nested
    @DisplayName("Save identity")
    inner class SaveIdentity {

        @Test
        fun `save identity on supplier will not save into the store supplier`() = runBlocking {
            val id = createIdentity()
            val uuid = id.uuid
            coJustRun { mockSupplier.saveIdentity(id) }

            val cacheIdentityCacheService =
                ClientIdentityCacheServiceImpl(cacheClient, cacheByUUID = true, cacheByName = false)
            val storeEntitySupplier = StoreEntitySupplier(
                CacheEntitySupplier(cacheIdentityCacheService),
                mockSupplier
            )
            // Use to verify if data is inserted
            val cacheEntitySupplier = CacheEntitySupplier(cacheIdentityCacheService)
            storeEntitySupplier.saveIdentity(id)

            assertNull(cacheEntitySupplier.getIdentityByUUID(uuid))
        }

    }
}