package org.universe.database.supplier

import dev.kord.cache.api.DataCache
import dev.kord.cache.api.data.description
import dev.kord.cache.map.MapDataCache
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.universe.database.client.createIdentity
import org.universe.database.dao.ClientIdentity
import org.universe.model.ProfileId
import org.universe.model.ProfileSkin
import kotlin.test.*

class StoreEntitySupplierTest : KoinTest {

    private lateinit var storeEntitySupplier: StoreEntitySupplier
    private lateinit var mockSupplier: EntitySupplier
    private lateinit var cacheEntitySupplier: CacheEntitySupplier

    @BeforeTest
    fun onBefore() {
        startKoin {
            modules(
                module {
                    val mapCache: DataCache = MapDataCache {
                        forType<ProfileId> { concurrentHashMap() }
                        forType<ProfileSkin> { concurrentHashMap() }
                    }

                    runBlocking {
                        mapCache.register(description(ClientIdentity::name))
                        mapCache.register(description(ClientIdentity::uuid))
                    }

                    single { mapCache }
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
    @DisplayName("Get identity by uuid")
    inner class GetIdentityByUUID : StoreTest {

        @Test
        override fun `data not stored into cache if data not exists`() = runBlocking {
            val id = createIdentity()
            val uuid = id.uuid
            coEvery { mockSupplier.getIdentityByUUID(uuid) } returns null
            assertNull(storeEntitySupplier.getIdentityByUUID(uuid))
            assertNull(cacheEntitySupplier.getIdentityByUUID(uuid))
        }

        @Test
        override fun `data stored if found`() = runBlocking{
            val id = createIdentity()
            val uuid = id.uuid
            coEvery { mockSupplier.getIdentityByUUID(uuid) } returns id
            assertEquals(id, storeEntitySupplier.getIdentityByUUID(uuid))
            assertEquals(id, cacheEntitySupplier.getIdentityByUUID(uuid))
        }

    }

    @Nested
    @DisplayName("Get identity by name")
    inner class GetIdentityByName : StoreTest {

        @Test
        override fun `data not stored into cache if data not exists`() = runBlocking {
            val id = createIdentity()
            val name = id.name
            coEvery { mockSupplier.getIdentityByName(name) } returns null
            assertNull(storeEntitySupplier.getIdentityByName(name))
            assertNull(cacheEntitySupplier.getIdentityByName(name))
        }

        @Test
        override fun `data stored if found`() = runBlocking{
            val id = createIdentity()
            val name = id.name
            coEvery { mockSupplier.getIdentityByName(name) } returns id
            assertEquals(id, storeEntitySupplier.getIdentityByName(name))
            assertEquals(id, cacheEntitySupplier.getIdentityByName(name))
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
            storeEntitySupplier.saveIdentity(id)

            assertNull(cacheEntitySupplier.getIdentityByUUID(uuid))
        }

    }
}