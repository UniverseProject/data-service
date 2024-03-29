package io.github.universeproject.dataservice.supplier.http

import io.github.universeproject.dataservice.cache.CacheClient
import io.github.universeproject.dataservice.container.createRedisContainer
import io.github.universeproject.dataservice.data.ProfileIdCacheServiceImpl
import io.github.universeproject.dataservice.data.ProfileSkinCacheServiceImpl
import io.github.universeproject.dataservice.utils.createProfileId
import io.github.universeproject.dataservice.utils.createProfileSkin
import io.lettuce.core.RedisURI
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Testcontainers
class StoreEntitySupplierTest {

    companion object {
        @JvmStatic
        @Container
        private val redisContainer = createRedisContainer()
    }

    private lateinit var cacheClient: CacheClient

    private lateinit var storeEntitySupplier: StoreEntitySupplier
    private lateinit var mockSupplier: EntitySupplier
    private lateinit var cacheEntitySupplier: CacheEntitySupplier

    @BeforeTest
    fun onBefore() = runBlocking {
        cacheClient = CacheClient {
            uri = RedisURI.create(redisContainer.url)
        }

        mockSupplier = mockk()
        // Use to verify if data is inserted
        cacheEntitySupplier = CacheEntitySupplier(
            ProfileSkinCacheServiceImpl(cacheClient),
            ProfileIdCacheServiceImpl(cacheClient)
        )

        storeEntitySupplier = StoreEntitySupplier(cacheEntitySupplier, mockSupplier)
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
            coEvery { mockSupplier.getUUID(name) } returns null
            assertNull(storeEntitySupplier.getUUID(name))
            assertNull(cacheEntitySupplier.getUUID(name))
        }

        @Test
        override fun `data stored if found`() = runBlocking {
            val id = createProfileId()
            val name = id.name
            coEvery { mockSupplier.getUUID(name) } returns id
            assertEquals(id, storeEntitySupplier.getUUID(name))
            assertEquals(id, cacheEntitySupplier.getUUID(name))
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