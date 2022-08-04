package io.github.universeproject.dataservice.data.clientIdentity

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.universe.dataservice.data.ClientIdentityServiceImpl
import org.universe.dataservice.supplier.database.EntitySupplier
import org.universe.dataservice.utils.createIdentity
import org.universe.dataservice.utils.getRandomString
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ClientIdentityServiceImplTest {

    private lateinit var serviceImpl: ClientIdentityServiceImpl
    private val supplier get() = serviceImpl.supplier

    @BeforeTest
    fun onBefore() {
        serviceImpl = ClientIdentityServiceImpl(mockk(getRandomString()))
    }

    interface ClientIdentityServiceTest {
        fun `data is not found into supplier`()
        fun `data is retrieved from supplier`()
    }

    @Test
    fun `change supplier strategy`() {
        val initStrategy = serviceImpl.supplier
        val newStrategy = mockk<EntitySupplier>(getRandomString())

        val newService = serviceImpl.withStrategy(newStrategy)
        assertEquals(newStrategy, newService.supplier)
        assertEquals(initStrategy, serviceImpl.supplier)
    }

    @Nested
    @DisplayName("Get identity by UUID")
    inner class GetByUUID : ClientIdentityServiceTest {

        @Test
        override fun `data is not found into supplier`() = runBlocking {
            coEvery { supplier.getIdentityByUUID(any()) } returns null
            assertNull(serviceImpl.getByUUID(UUID.randomUUID()))
        }

        @Test
        override fun `data is retrieved from supplier`() = runBlocking {
            val identity = createIdentity()
            val uuid = identity.uuid
            coEvery { supplier.getIdentityByUUID(uuid) } returns identity
            assertEquals(identity, serviceImpl.getByUUID(uuid))
            coVerify(exactly = 1) { supplier.getIdentityByUUID(uuid) }
        }
    }

    @Nested
    @DisplayName("Get identity by name")
    inner class GetByName : ClientIdentityServiceTest {

        @Test
        override fun `data is not found into supplier`() = runBlocking {
            coEvery { supplier.getIdentityByName(any()) } returns null
            assertNull(serviceImpl.getByName(getRandomString()))
        }

        @Test
        override fun `data is retrieved from supplier`() = runBlocking {
            val identity = createIdentity()
            val name = identity.name
            coEvery { supplier.getIdentityByName(name) } returns identity
            assertEquals(identity, serviceImpl.getByName(name))
            coVerify(exactly = 1) { supplier.getIdentityByName(name) }
        }
    }

    @Nested
    @DisplayName("Get skin by id")
    inner class SaveIdentity {

        @Test
        fun `trigger save from supplier`() = runBlocking {
            val identity = createIdentity()
            coJustRun { supplier.saveIdentity(identity) }

            serviceImpl.save(identity)
            coVerify(exactly = 1) { supplier.saveIdentity(identity) }
        }
    }
}