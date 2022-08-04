package io.github.universeproject.dataservice.supplier.http

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import io.github.universeproject.dataservice.cache.CacheClient
import org.universe.dataservice.data.ProfileIdCacheService
import org.universe.dataservice.data.ProfileIdCacheServiceImpl
import org.universe.dataservice.data.ProfileSkinCacheService
import org.universe.dataservice.data.ProfileSkinCacheServiceImpl
import org.universe.dataservice.utils.createProfileId
import org.universe.dataservice.utils.createProfileSkin
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CacheEntitySupplierTest {

    private lateinit var cacheClient: io.github.universeproject.dataservice.cache.CacheClient

    @BeforeTest
    fun onBefore() {
        cacheClient = mockk()
    }

    @Nested
    inner class ProfileIdService {

        @Nested
        inner class DefaultParameter {

            @Test
            fun `default values`() {
                val service = ProfileIdCacheServiceImpl(cacheClient)
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

            assertEquals(profile, supplier.getUUID(name))
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

            @Test
            fun `default values`() {
                val service = ProfileSkinCacheServiceImpl(cacheClient)
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