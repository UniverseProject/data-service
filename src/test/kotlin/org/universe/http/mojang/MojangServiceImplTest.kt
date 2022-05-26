package org.universe.http.mojang

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.universe.http.supplier.EntitySupplier
import org.universe.http.utils.createProfileId
import org.universe.http.utils.createProfileSkin
import org.universe.http.utils.getRandomString
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MojangServiceImplTest {

    private lateinit var serviceImpl: MojangServiceImpl
    private val supplier get() = serviceImpl.supplier

    @BeforeTest
    fun onBefore() {
        serviceImpl = MojangServiceImpl(mockk(getRandomString()))
    }

    interface MojangServiceTest {
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
    @DisplayName("Get skin by name")
    inner class GetSkinByName: MojangServiceTest {

        @Test
        override fun `data is not found into supplier`() = runBlocking {
            val profileId = createProfileId()
            val profileSkin = createProfileSkin(profileId)
            val id = profileId.id
            val name = profileId.name
            coEvery { supplier.getId(any()) } returns null
            coEvery { supplier.getSkin(id) } returns profileSkin
            assertNull(serviceImpl.getSkinByName(name))

            coEvery { supplier.getId(name) } returns profileId
            coEvery { supplier.getSkin(any()) } returns null
            assertNull(serviceImpl.getSkinByName(name))
        }

        @Test
        override fun `data is retrieved from supplier`() = runBlocking {
            val profileId = createProfileId()
            val profileSkin = createProfileSkin(profileId)
            val id = profileId.id
            val name = profileId.name
            coEvery { supplier.getId(name) } returns profileId
            coEvery { supplier.getSkin(id) } returns profileSkin
            assertEquals(profileSkin, serviceImpl.getSkinByName(name))
        }
    }

    @Nested
    @DisplayName("Get player uuid")
    inner class GetId : MojangServiceTest {

        @Test
        override fun `data is not found into supplier`() = runBlocking {
            coEvery { supplier.getId(any()) } returns null
            assertNull(serviceImpl.getId(getRandomString()))
        }

        @Test
        override fun `data is retrieved from supplier`() = runBlocking {
            val id = createProfileId()
            val name = id.name
            coEvery { supplier.getId(name) } returns id
            assertEquals(id, serviceImpl.getId(name))
        }
    }

    @Nested
    @DisplayName("Get skin by id")
    inner class GetSkin: MojangServiceTest {

        @Test
        override fun `data is not found into supplier`() = runBlocking {
            coEvery { supplier.getSkin(any()) } returns null
            assertNull(serviceImpl.getSkin(getRandomString()))
        }

        @Test
        override fun `data is retrieved from supplier`() = runBlocking {
            val skin = createProfileSkin()
            val id = skin.id
            coEvery { supplier.getSkin(id) } returns skin
            assertEquals(skin, serviceImpl.getSkin(id))
        }
    }
}