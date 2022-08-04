package io.github.universeproject.dataservice.supplier.database

import io.github.universeproject.dataservice.cache.CacheClient
import io.github.universeproject.dataservice.data.ClientIdentityCacheService
import io.github.universeproject.dataservice.data.ClientIdentityCacheServiceImpl
import io.github.universeproject.dataservice.utils.createIdentity
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import kotlin.test.*

class CacheEntitySupplierTest {

    private lateinit var cacheEntitySupplier: CacheEntitySupplier

    private lateinit var cacheService: ClientIdentityCacheService

    @BeforeTest
    fun onBefore() = runBlocking {
        cacheService = mockk()
        cacheEntitySupplier = CacheEntitySupplier(cacheService)
    }

    @Nested
    inner class DefaultParameter {

        private lateinit var cacheClient: CacheClient

        @BeforeTest
        fun onBefore() {
            cacheClient = mockk()
        }

        @Test
        fun `default values`() {
            val service = ClientIdentityCacheServiceImpl(cacheClient)
            assertEquals("cliId:", service.prefixKey)
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