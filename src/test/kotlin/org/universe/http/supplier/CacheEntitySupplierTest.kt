package org.universe.http.supplier

import io.lettuce.core.RedisURI
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
import org.universe.utils.getRandomString
import kotlin.test.*

@Testcontainers
class CacheEntitySupplierTest : KoinTest {

    companion object {
        @JvmStatic
        @Container
        val redisContainer = createRedisContainer()
    }

    private lateinit var cacheClient: CacheClient
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
        cacheEntitySupplier = CacheEntitySupplier()
    }

    @AfterTest
    fun onAfter() {
        stopKoin()
        cacheClient.close()
    }

    interface CacheTest {
        fun `data is not into the cache`()
        fun `data is retrieved from the cache`()
    }

    @Nested
    @DisplayName("Get player uuid")
    inner class GetId : CacheTest {

        @Test
        override fun `data is not into the cache`() = runBlocking {
            val id = createProfileId()
            cacheEntitySupplier.save(id)
            assertNull(cacheEntitySupplier.getId(getRandomString()))
        }

        @Test
        override fun `data is retrieved from the cache`() = runBlocking {
            val id = createProfileId()
            cacheEntitySupplier.save(id)
            assertEquals(id, cacheEntitySupplier.getId(id.name))
        }
    }

    @Nested
    @DisplayName("Get skin by id")
    inner class GetSkin : CacheTest {

        @Test
        override fun `data is not into the cache`() = runBlocking {
            val skin = createProfileSkin()
            cacheEntitySupplier.save(skin)
            assertNull(cacheEntitySupplier.getSkin(getRandomString()))
        }

        @Test
        override fun `data is retrieved from the cache`() = runBlocking {
            val skin = createProfileSkin()
            cacheEntitySupplier.save(skin)
            assertEquals(skin, cacheEntitySupplier.getSkin(skin.id))
        }
    }
}