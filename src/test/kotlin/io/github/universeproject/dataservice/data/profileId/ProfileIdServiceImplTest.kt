package io.github.universeproject.dataservice.data.profileId

import io.github.universeproject.dataservice.data.ProfileIdServiceImpl
import io.github.universeproject.dataservice.supplier.http.EntitySupplier
import io.github.universeproject.dataservice.utils.createProfileId
import io.github.universeproject.dataservice.utils.getRandomString
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProfileIdServiceImplTest {

    private lateinit var serviceImpl: ProfileIdServiceImpl
    private val supplier get() = serviceImpl.supplier

    @BeforeTest
    fun onBefore() {
        serviceImpl = ProfileIdServiceImpl(mockk(getRandomString()))
    }

    interface ProfileIdServiceServiceTest {
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
    @DisplayName("Get by name")
    inner class GetByName : ProfileIdServiceServiceTest {

        @Test
        override fun `data is not found into supplier`() = runBlocking {
            coEvery { supplier.getUUID(any()) } returns null
            assertNull(serviceImpl.getByName(getRandomString()))
        }

        @Test
        override fun `data is retrieved from supplier`() = runBlocking {
            val profile = createProfileId()
            val name = profile.name
            coEvery { supplier.getUUID(name) } returns profile
            assertEquals(profile, serviceImpl.getByName(name))
            coVerify(exactly = 1) { supplier.getUUID(name) }
        }
    }
}