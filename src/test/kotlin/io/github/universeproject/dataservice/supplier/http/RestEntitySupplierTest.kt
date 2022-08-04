package io.github.universeproject.dataservice.supplier.http

import io.github.universeproject.MojangAPI
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.universe.dataservice.utils.createProfileId
import org.universe.dataservice.utils.createProfileSkin
import org.universe.dataservice.utils.getRandomString
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RestEntitySupplierTest {

    private lateinit var mojangAPI: MojangAPI
    private lateinit var restEntitySupplier: RestEntitySupplier

    @BeforeTest
    fun onBefore() {
        mojangAPI = mockk(getRandomString())
        restEntitySupplier = RestEntitySupplier(mojangAPI)
    }

    interface RestTest {
        fun `data not found from rest`()
        fun `data is retrieved from rest`()
    }

    @Nested
    @DisplayName("Get player uuid")
    inner class GetId : RestTest {

        @Test
        override fun `data not found from rest`() = runBlocking {
            coEvery { mojangAPI.getUUID(any<String>()) } returns null
            val id = getRandomString()
            assertNull(restEntitySupplier.getUUID(id))
            coVerify(exactly = 1) { mojangAPI.getUUID(id) }
        }

        @Test
        override fun `data is retrieved from rest`() = runBlocking {
            val profileId = createProfileId()
            val name = profileId.name
            coEvery { mojangAPI.getUUID(name) } returns profileId
            assertEquals(profileId, restEntitySupplier.getUUID(name))
            coVerify(exactly = 1) { mojangAPI.getUUID(name) }
        }

    }

    @Nested
    @DisplayName("Get skin by id")
    inner class GetSkin : RestTest {

        @Test
        override fun `data not found from rest`() = runBlocking {
            coEvery { mojangAPI.getSkin(any()) } returns null
            val name = getRandomString()
            assertNull(restEntitySupplier.getSkin(name))
            coVerify(exactly = 1) { mojangAPI.getSkin(name) }
        }

        @Test
        override fun `data is retrieved from rest`() = runBlocking {
            val skin = createProfileSkin()
            val id = skin.id
            coEvery { mojangAPI.getSkin(id) } returns skin
            assertEquals(skin, restEntitySupplier.getSkin(id))
            coVerify(exactly = 1) { mojangAPI.getSkin(id) }
        }

    }
}