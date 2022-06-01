package org.universe.dataservice.supplier.http

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junitpioneer.jupiter.SetSystemProperty
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.universe.dataservice.cache.CacheClient
import org.universe.dataservice.data.ProfileIdCacheService
import org.universe.dataservice.data.ProfileIdCacheServiceImpl
import org.universe.dataservice.data.ProfileSkinCacheService
import org.universe.dataservice.data.ProfileSkinCacheServiceImpl
import org.universe.dataservice.utils.createProfileId
import org.universe.dataservice.utils.createProfileSkin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CacheEntitySupplierTest : KoinTest {

    @BeforeTest
    fun onBefore() {
        startKoin {
            modules(
                module {
                    single {
                        mockk<CacheClient>()
                    }
                })
        }
    }

    @AfterTest
    fun onAfter() {
        stopKoin()
    }

    @Nested
    inner class ProfileIdService {

        @Nested
        inner class DefaultParameter {

            @SetSystemProperty(key = "cache.profilId.prefixKey", value = "test:")
            @Test
            fun `default implementation use environment variable`() {
                val supplier = CacheEntitySupplier()
                val service = supplier.profileIdCache as ProfileIdCacheServiceImpl
                assertEquals("test:", service.prefixKey)
            }

            @Test
            fun `default values`() {
                val supplier = CacheEntitySupplier()
                val service = supplier.profileIdCache as ProfileIdCacheServiceImpl
                assertEquals("profId:", service.prefixKey)
            }

        }

        @Test
        fun `get id use the mock method`() = runBlocking {
            val cacheService = mockk<ProfileIdCacheService>()
            val supplier = CacheEntitySupplier(mockk(), cacheService)

            val profile = createProfileId()
            val name = profile.name
            coEvery { cacheService.getByName(name) } returns profile

            assertEquals(profile, supplier.getId(name))
            coVerify(exactly = 1) { cacheService.getByName(name) }
        }

        @Test
        fun `save id use the mock method`() = runBlocking {
            val cacheService = mockk<ProfileIdCacheService>()
            val supplier = CacheEntitySupplier(mockk(), cacheService)

            val profile = createProfileId()
            coJustRun { cacheService.save(profile) }

            supplier.save(profile)
            coVerify(exactly = 1) { cacheService.save(profile) }
        }

    }

    @Nested
    inner class ProfileSkinService {

        @Nested
        inner class DefaultParameter {

            @SetSystemProperty(key = "cache.skin.prefixKey", value = "test:")
            @Test
            fun `default implementation use environment variable`() {
                val supplier = CacheEntitySupplier()
                val service = supplier.profileSkinCache as ProfileSkinCacheServiceImpl
                assertEquals("test:", service.prefixKey)
            }

            @Test
            fun `default values`() {
                val supplier = CacheEntitySupplier()
                val service = supplier.profileSkinCache as ProfileSkinCacheServiceImpl
                assertEquals("skin:", service.prefixKey)
            }

        }

        @Test
        fun `get skin use the mock method`() = runBlocking {
            val cacheService = mockk<ProfileSkinCacheService>()
            val supplier = CacheEntitySupplier(cacheService, mockk())

            val profile = createProfileSkin()
            val uuid = profile.id
            coEvery { cacheService.getByUUID(uuid) } returns profile

            assertEquals(profile, supplier.getSkin(uuid))
            coVerify(exactly = 1) { cacheService.getByUUID(uuid) }
        }

        @Test
        fun `save id use the mock method`() = runBlocking {
            val cacheService = mockk<ProfileSkinCacheService>()
            val supplier = CacheEntitySupplier(cacheService, mockk())

            val profile = createProfileSkin()
            coJustRun { cacheService.save(profile) }

            supplier.save(profile)
            coVerify(exactly = 1) { cacheService.save(profile) }
        }

    }

}