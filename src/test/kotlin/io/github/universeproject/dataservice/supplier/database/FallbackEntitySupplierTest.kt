package io.github.universeproject.dataservice.supplier.database

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.universe.dataservice.data.ClientIdentity
import org.universe.dataservice.utils.createIdentity
import org.universe.dataservice.utils.getRandomString
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FallbackEntitySupplierTest {

    private lateinit var fallbackEntitySupplier: FallbackEntitySupplier

    @BeforeTest
    fun onBefore() {
        fallbackEntitySupplier = FallbackEntitySupplier(mockk(getRandomString()), mockk(getRandomString()))
    }

    abstract inner class FallbackTest {

        @Test
        fun `data is not present into both supplier`() = runBlocking {
            val first = fallbackEntitySupplier.first
            val second = fallbackEntitySupplier.second
            val id = createIdentity()

            coEvery { mockGetMethod(first) } returns null
            coEvery { mockGetMethod(second) } returns null

            assertNull(getIdentity(fallbackEntitySupplier, id))

            coVerify(exactly = 1) { getIdentity(first, id) }
            coVerify(exactly = 1) { getIdentity(second, id) }
        }

        @Test
        fun `data is present into one of both supplier`() = runBlocking {
            val first = fallbackEntitySupplier.first
            val second = fallbackEntitySupplier.second
            val id = createIdentity()

            coEvery { mockGetMethod(first) } returns id
            coEvery { mockGetMethod(second) } returns null

            assertEquals(id, getIdentity(fallbackEntitySupplier, id))
            coVerify(exactly = 1) { getIdentity(first, id) }
            coVerify(exactly = 0) { getIdentity(second, id) }

            coEvery { mockGetMethod(first) } returns null
            coEvery { mockGetMethod(second) } returns id

            assertEquals(id, getIdentity(fallbackEntitySupplier, id))

            coVerify(exactly = 2) { getIdentity(first, id) }
            coVerify(exactly = 1) { getIdentity(second, id) }
        }

        abstract suspend fun getIdentity(supplier: EntitySupplier, id: ClientIdentity): ClientIdentity?

        abstract suspend fun MockKMatcherScope.mockGetMethod(supplier: EntitySupplier): ClientIdentity?
    }

    @Nested
    @DisplayName("Get identity by uuid")
    inner class GetIdentityByUUID : FallbackTest() {

        override suspend fun getIdentity(supplier: EntitySupplier, id: ClientIdentity): ClientIdentity? {
            return supplier.getIdentityByUUID(id.uuid)
        }

        override suspend fun MockKMatcherScope.mockGetMethod(supplier: EntitySupplier): ClientIdentity? {
            return supplier.getIdentityByUUID(any())
        }

    }

    @Nested
    @DisplayName("Get identity by name")
    inner class GetIdentityByName : FallbackTest() {

        override suspend fun getIdentity(supplier: EntitySupplier, id: ClientIdentity): ClientIdentity? {
            return supplier.getIdentityByName(id.name)
        }

        override suspend fun MockKMatcherScope.mockGetMethod(supplier: EntitySupplier): ClientIdentity? {
            return supplier.getIdentityByName(any())
        }

    }

    @Nested
    @DisplayName("Save identity")
    inner class SaveIdentity {

        @Test
        fun `save identity on supplier will not save call the second supplier`() = runBlocking {
            val first = fallbackEntitySupplier.first
            val second = fallbackEntitySupplier.second

            val id = createIdentity()
            coJustRun { first.saveIdentity(id) }
            fallbackEntitySupplier.saveIdentity(id)

            coVerify(exactly = 1) { first.saveIdentity(id) }
            coVerify(exactly = 0) { second.saveIdentity(id) }
        }

    }
}