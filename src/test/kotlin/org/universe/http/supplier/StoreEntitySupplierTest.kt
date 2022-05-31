package org.universe.http.supplier

import io.lettuce.core.RedisURI
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.universe.cache.CacheClient
import org.universe.container.createRedisContainer
import org.universe.database.supplier.CacheEntitySupplierTest
import org.universe.utils.createProfileId
import org.universe.utils.createProfileSkin
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
    fun onBefore() = runBlocking {
        cacheClient = CacheClient {
            RedisURI.create(CacheEntitySupplierTest.redisContainer.url)
        }

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
    }

    interface StoreTest {
        fun `data not stored into cache if data not exists`()
        fun `data stored if found`()
    }

    @Nested
    @DisplayName("Get player uuid")
    inner class GetId : StoreTest {

        @Test
        override fun `data not stored into cache if data not exists`() = runBlocking {
            val id = createProfileId()
            val name = id.name
            coEvery { mockSupplier.getId(name) } returns null
            assertNull(storeEntitySupplier.getId(name))
            assertNull(cacheEntitySupplier.getId(name))
        }

        @Test
        override fun `data stored if found`() = runBlocking {
            val id = createProfileId()
            val name = id.name
            coEvery { mockSupplier.getId(name) } returns id
            assertEquals(id, storeEntitySupplier.getId(name))
            assertEquals(id, cacheEntitySupplier.getId(name))
        }

    }

    @Nested
    @DisplayName("Get skin by id")
    inner class GetSkin : StoreTest {

        @Test
        override fun `data not stored into cache if data not exists`() = runBlocking {
            val skin = createProfileSkin()
            val uuid = skin.id
            coEvery { mockSupplier.getSkin(uuid) } returns null
            assertNull(storeEntitySupplier.getSkin(uuid))
            assertNull(cacheEntitySupplier.getSkin(uuid))
        }

        @Test
        override fun `data stored if found`() = runBlocking {
            val skin = createProfileSkin()
            val uuid = skin.id
            coEvery { mockSupplier.getSkin(uuid) } returns skin
            assertEquals(skin, storeEntitySupplier.getSkin(uuid))
            assertEquals(skin, cacheEntitySupplier.getSkin(uuid))
        }

    }
}