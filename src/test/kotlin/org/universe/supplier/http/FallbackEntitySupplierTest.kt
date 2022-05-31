package org.universe.supplier.http

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.universe.utils.createProfileId
import org.universe.utils.createProfileSkin
import org.universe.utils.getRandomString
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

    interface FallbackTest {
        fun `data is not present into both supplier`()
        fun `data is present into one of both supplier`()
    }

    @Nested
    @DisplayName("Get player uuid")
    inner class GetId : FallbackTest {

        @Test
        override fun `data is not present into both supplier`() = runBlocking {
            val first = fallbackEntitySupplier.first
            val second = fallbackEntitySupplier.second
            val id = createProfileId()
            val name = id.name

            coEvery { first.getId(any()) } returns null
            coEvery { second.getId(any()) } returns null

            assertNull(fallbackEntitySupplier.getId(name))

            coVerify(exactly = 1) { first.getId(name) }
            coVerify(exactly = 1) { second.getId(name) }
        }

        @Test
        override fun `data is present into one of both supplier`() = runBlocking {
            val first = fallbackEntitySupplier.first
            val second = fallbackEntitySupplier.second
            val id = createProfileId()
            val name = id.name

            coEvery { first.getId(any()) } returns id
            coEvery { second.getId(any()) } returns null

            assertEquals(id, fallbackEntitySupplier.getId(name))
            coVerify(exactly = 1) { first.getId(name) }
            coVerify(exactly = 0) { second.getId(name) }

            coEvery { first.getId(any()) } returns null
            coEvery { second.getId(any()) } returns id

            assertEquals(id, fallbackEntitySupplier.getId(name))

            coVerify(exactly = 2) { first.getId(name) }
            coVerify(exactly = 1) { second.getId(name) }
        }

    }

    @Nested
    @DisplayName("Get skin by id")
    inner class GetSkin : FallbackTest {

        @Test
        override fun `data is not present into both supplier`() = runBlocking {
            val first = fallbackEntitySupplier.first
            val second = fallbackEntitySupplier.second
            val skin = createProfileSkin()
            val id = skin.id

            coEvery { first.getSkin(any()) } returns null
            coEvery { second.getSkin(any()) } returns null

            assertNull(fallbackEntitySupplier.getSkin(id))

            coVerify(exactly = 1) { first.getSkin(id) }
            coVerify(exactly = 1) { second.getSkin(id) }
        }

        @Test
        override fun `data is present into one of both supplier`() = runBlocking {
            val first = fallbackEntitySupplier.first
            val second = fallbackEntitySupplier.second
            val skin = createProfileSkin()
            val id = skin.id

            coEvery { first.getSkin(any()) } returns skin
            coEvery { second.getSkin(any()) } returns null

            assertEquals(skin, fallbackEntitySupplier.getSkin(id))
            coVerify(exactly = 1) { first.getSkin(id) }
            coVerify(exactly = 0) { second.getSkin(id) }

            coEvery { first.getSkin(any()) } returns null
            coEvery { second.getSkin(any()) } returns skin

            assertEquals(skin, fallbackEntitySupplier.getSkin(id))

            coVerify(exactly = 2) { first.getSkin(id) }
            coVerify(exactly = 1) { second.getSkin(id) }
        }

    }
}