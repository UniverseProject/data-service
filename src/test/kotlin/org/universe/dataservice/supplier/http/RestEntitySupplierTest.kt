package org.universe.dataservice.supplier.http

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.universe.dataservice.data.MojangAPI
import org.universe.dataservice.utils.createProfileId
import org.universe.dataservice.utils.createProfileSkin
import org.universe.dataservice.utils.getRandomString
import kotlin.test.*

class RestEntitySupplierTest : KoinTest {

    private val mojangAPI: MojangAPI by inject()
    private lateinit var restEntitySupplier: RestEntitySupplier

    @BeforeTest
    fun onBefore() {
        startKoin {
            modules(
                module {
                    single { mockk<MojangAPI>(getRandomString()) } bind MojangAPI::class
                })
        }
        restEntitySupplier = RestEntitySupplier()
    }

    @AfterTest
    fun onAfter() {
        stopKoin()
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
            coEvery { mojangAPI.getId(any()) } returns null
            val id = getRandomString()
            assertNull(restEntitySupplier.getId(id))
            coVerify(exactly = 1) { mojangAPI.getId(id) }
        }

        @Test
        override fun `data is retrieved from rest`() = runBlocking {
            val profileId = createProfileId()
            val name = profileId.name
            coEvery { mojangAPI.getId(name) } returns profileId
            assertEquals(profileId, restEntitySupplier.getId(name))
            coVerify(exactly = 1) { mojangAPI.getId(name) }
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