package org.universe.dataservice.supplier.database

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
import org.universe.dataservice.data.ClientIdentityCacheService
import org.universe.dataservice.data.ClientIdentityCacheServiceImpl
import org.universe.dataservice.utils.createIdentity
import kotlin.test.*

class CacheEntitySupplierTest : KoinTest {

    private lateinit var cacheEntitySupplier: CacheEntitySupplier

    private lateinit var cacheService: ClientIdentityCacheService

    @BeforeTest
    fun onBefore() = runBlocking {
        cacheService = mockk()
        cacheEntitySupplier = CacheEntitySupplier(cacheService)
    }

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

        @SetSystemProperty(key = "cache.clientId.prefixKey", value = "test:")
        @SetSystemProperty(key = "cache.clientId.useUUID", value = "false")
        @SetSystemProperty(key = "cache.clientId.useName", value = "true")
        @Test
        fun `default implementation use environment variable`() {
            val supplier = CacheEntitySupplier()
            val service = supplier.clientIdentityCache as ClientIdentityCacheServiceImpl
            assertEquals("test:", service.prefixKey)
            assertFalse { service.cacheByUUID }
            assertTrue { service.cacheByName }
        }

        @Test
        fun `default values`() {
            val supplier = CacheEntitySupplier()
            val service = supplier.clientIdentityCache as ClientIdentityCacheServiceImpl
            assertTrue { service.cacheByUUID }
            assertFalse { service.cacheByName }
        }

    }

    @Test
    fun `get identity by uuid use the mock method`() = runBlocking {
        val id = createIdentity()
        val uuid = id.uuid
        coEvery { cacheService.getByUUID(uuid) } returns id

        assertEquals(id, cacheEntitySupplier.getIdentityByUUID(uuid))
        coVerify(exactly = 1) { cacheService.getByUUID(uuid) }
    }

    @Test
    fun `get identity by name use the mock method`() = runBlocking {
        val id = createIdentity()
        val name = id.name
        coEvery { cacheService.getByName(name) } returns id

        assertEquals(id, cacheEntitySupplier.getIdentityByName(name))
        coVerify(exactly = 1) { cacheService.getByName(name) }
    }

    @Test
    fun `save identity by name use the mock method`() = runBlocking {
        val id = createIdentity()
        coJustRun { cacheService.save(id) }

        cacheEntitySupplier.saveIdentity(id)
        coVerify(exactly = 1) { cacheService.save(id) }
    }

}