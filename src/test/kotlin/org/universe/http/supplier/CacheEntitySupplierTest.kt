package org.universe.http.supplier

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
import org.universe.http.mojang.model.ProfileId
import org.universe.http.mojang.model.ProfileSkin
import org.universe.http.utils.createProfileId
import org.universe.http.utils.createProfileSkin
import org.universe.http.utils.getRandomString
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
                        mapCache.register(description(ProfileId::name))
                        mapCache.register(description(ProfileSkin::id))
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
            cache.put(id)
            assertNull(cacheEntitySupplier.getId(getRandomString()))
        }

        @Test
        override fun `data is retrieved from the cache`() = runBlocking {
            val id = createProfileId()
            cache.put(id)
            assertEquals(id, cacheEntitySupplier.getId(id.name))
        }
    }

    @Nested
    @DisplayName("Get skin by id")
    inner class GetSkin: CacheTest {

        @Test
        override fun `data is not into the cache`() = runBlocking {
            val skin = createProfileSkin()
            cache.put(skin)
            assertNull(cacheEntitySupplier.getSkin(getRandomString()))
        }

        @Test
        override fun `data is retrieved from the cache`() = runBlocking {
            val skin = createProfileSkin()
            cache.put(skin)
            assertEquals(skin, cacheEntitySupplier.getSkin(skin.id))
        }
    }
}