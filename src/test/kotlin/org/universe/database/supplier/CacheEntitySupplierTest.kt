package org.universe.database.supplier

import dev.kord.cache.api.DataCache
import dev.kord.cache.api.data.description
import dev.kord.cache.api.put
import dev.kord.cache.map.MapDataCache
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.universe.database.client.createIdentity
import org.universe.database.dao.ClientIdentity
import org.universe.model.ProfileId
import org.universe.model.ProfileSkin
import kotlin.test.*

class CacheEntitySupplierTest : KoinTest {

    private val cache: DataCache by inject()
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
        cacheEntitySupplier = CacheEntitySupplier()
    }

    @AfterTest
    fun onAfter() {
        stopKoin()
    }

    abstract inner class CacheTest {

        @Test
        fun `data is not into the cache`() = runBlocking {
            val id = createIdentity()
            cache.put(id)
            assertNull(getIdentity(cacheEntitySupplier, createIdentity()))
        }

        @Test
        fun `data is retrieved from the cache`() = runBlocking {
            val id = createIdentity()
            cache.put(id)
            assertEquals(id, getIdentity(cacheEntitySupplier, id))
        }

        abstract suspend fun getIdentity(supplier: EntitySupplier, id: ClientIdentity): ClientIdentity?

    }

    @Nested
    @DisplayName("Get identity by uuid")
    inner class GetIdentityByUUID : CacheTest() {

        override suspend fun getIdentity(supplier: EntitySupplier, id: ClientIdentity): ClientIdentity? {
            return supplier.getIdentityByUUID(id.uuid)
        }

    }

    @Nested
    @DisplayName("Get identity by name")
    inner class GetIdentityByName : CacheTest() {

        override suspend fun getIdentity(supplier: EntitySupplier, id: ClientIdentity): ClientIdentity? {
            return supplier.getIdentityByName(id.name)
        }

    }
}