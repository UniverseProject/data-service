package org.universe.dataservice.supplier.http

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.universe.dataservice.data.ClientIdentityCacheServiceImpl
import org.universe.dataservice.data.ProfileIdCacheService
import org.universe.dataservice.data.ProfileSkinCacheService
import org.universe.dataservice.utils.createProfileId
import org.universe.dataservice.utils.createProfileSkin
import org.universe.dataservice.utils.getRandomString
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CacheEntitySupplierTest {

    @Nested
    inner class DefaultParameter : KoinTest {

        @BeforeTest
        fun onBefore() {
            startKoin {
                modules(
                    module {
                        single {
                            mockk<org.universe.dataservice.cache.CacheClient>()
                        }
                    })
            }
        }

        @AfterTest
        fun onAfter() {
            stopKoin()
        }

        @Test
        fun `default implementation use environment variable`() {
            fun setPropertyAndCheckInstance(
                prefixKey: String,
                useUUID: Boolean,
                useName: Boolean
            ) {
                System.setProperty("cache.clientId.prefixKey", prefixKey)
                System.setProperty("cache.clientId.useUUID", "$useUUID")
                System.setProperty("cache.clientId.useName", "$useName")
                val supplier = org.universe.dataservice.supplier.database.CacheEntitySupplier()
                val service = supplier.clientIdentityCache as ClientIdentityCacheServiceImpl
                assertEquals(prefixKey, service.prefixKey)
                assertEquals(useUUID, service.cacheByUUID)
                assertEquals(useName, service.cacheByName)
            }

            setPropertyAndCheckInstance(getRandomString(), useUUID = true, useName = false)
            setPropertyAndCheckInstance(getRandomString(), useUUID = false, useName = true)
            setPropertyAndCheckInstance(getRandomString(), useUUID = true, useName = true)
            setPropertyAndCheckInstance(getRandomString(), useUUID = false, useName = false)
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
    fun `get id use the mock method`() = runBlocking {
        val cacheService = mockk<ProfileIdCacheService>()
        val supplier = CacheEntitySupplier(mockk(), cacheService)

        val profile = createProfileId()
        val name = profile.name
        coEvery { cacheService.getByName(name) } returns profile

        assertEquals(profile, supplier.getId(name))
        coVerify(exactly = 1) { cacheService.getByName(name) }
    }
}