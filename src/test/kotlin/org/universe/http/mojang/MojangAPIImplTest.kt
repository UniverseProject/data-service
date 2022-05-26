package org.universe.http.mojang

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import java.util.*
import kotlin.test.*

class MojangAPIImplTest : KoinTest {

    private lateinit var mojangAPIImpl: MojangAPIImpl

    @BeforeTest
    fun onBefore() {
        val jsonInstance = Json {
            ignoreUnknownKeys = true
        }
        startKoin {
            modules(
                module {
                    single { jsonInstance }
                    single {
                        HttpClient(CIO) {
                            expectSuccess = true
                            install(ContentNegotiation) {
                                json(jsonInstance)
                            }
                        }
                    }
                })
        }
        mojangAPIImpl = MojangAPIImpl()
    }

    @AfterTest
    fun onAfter() {
        stopKoin()
    }

    @Nested
    @DisplayName("Get player uuid")
    inner class GetUUID() {

        @Test
        fun `with an existing player name`() = runBlocking {
            val name = "lukethehacker23"
            val profileId = mojangAPIImpl.getId(name)
            assertNotNull(profileId)
            assertEquals(name, profileId.name)
            assertEquals("cdb5aee80f904fdda63ba16d38cd6b3b", profileId.id)
        }

        @Test
        fun `with an unknown player name`() = runBlocking {
            assertNull(mojangAPIImpl.getId("a"))
        }

        @Test
        fun `with a player name with invalid length`(): Unit = runBlocking {
            assertThrows<ClientRequestException> {
                mojangAPIImpl.getId("obviouslyanamethatistoolongforminecraft")
            }
        }

        @Test
        fun `with a player name with invalid character`(): Unit = runBlocking {
            assertThrows<ClientRequestException> {
                mojangAPIImpl.getId("&")
            }
        }
    }

    @Nested
    @DisplayName("Get player skin")
    inner class GetSkin() {

        @Test
        fun `with an existing player uuid`() = runBlocking {
            val id = "f1bfcbddc68b49bfaac9fb9d8ce5293d"
            val name = "123lmfao4"
            val skin = mojangAPIImpl.getSkin(id)
            assertNotNull(skin)
            assertEquals(id, skin.id)
            assertEquals(name, skin.name)

            val properties = skin.properties
            assertEquals(1, properties.size)

            val property = skin.getTexturesProperty()
            assertEquals("textures", property.name)
            assertNotNull(property.signature)

            val decoded = skin.getSkinDecoded()
            assertEquals(id, decoded.profileId)
            assertEquals(name, decoded.profileName)

            val textures = decoded.textures
            val skinTexture = textures.skin
            assertEquals("http://textures.minecraft.net/texture/e35f3a8df969b56b36f9aa60a736a2f9061de4ccf0fe9657d6c9bc02d77bfd7e", skinTexture.url)
            assertEquals("slim", skinTexture.metadata.model)
            assertNull(textures.cape)
        }

        @Test
        fun `with an unknown player uuid`() = runBlocking {
            assertNull(mojangAPIImpl.getSkin(UUID.randomUUID().toString()))
        }

        @Test
        fun `with a player uuid with invalid length`(): Unit = runBlocking {
            assertThrows<ClientRequestException> {
                mojangAPIImpl.getSkin(UUID.randomUUID().toString() + "a")
            }
        }

        @Test
        fun `with a player uuid with invalid character`(): Unit = runBlocking {
            assertThrows<ClientRequestException> {
                mojangAPIImpl.getSkin("invalid_uuid")
            }
        }
    }
}