package org.universe.http.mojang.model

import io.ktor.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.universe.http.utils.getRandomString
import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileSkinTest {

    @Nested
    @DisplayName("Textures property")
    inner class TexturesProperty {

        @Test
        fun `if properties is empty, throws exception`() {
            val skin = ProfileSkin(id = getRandomString(), name = getRandomString(), properties = createRandomProperties())
            assertThrows<NoSuchElementException> {
                skin.getTexturesProperty()
            }
        }

        @Test
        fun `if properties is not empty, search the property of textures`() {
            val texturesProperty = ProfileSkin.Property(name = "textures", getRandomString(), getRandomString())
            val properties = createRandomProperties()
            properties.add(texturesProperty)

            val skin = ProfileSkin(id = getRandomString(), name = getRandomString(), properties = properties)
            assertEquals(texturesProperty, skin.getTexturesProperty())
        }

        @Test
        fun `skin property throw exception if textures property is not found`() {
            val skin = ProfileSkin(id = getRandomString(), name = getRandomString(), properties = createRandomProperties())
            assertThrows<NoSuchElementException> {
                skin.skin
            }
        }

        @Test
        fun `skin property refers the textures property`() {
            val texturesProperty = ProfileSkin.Property(name = "textures", getRandomString(), getRandomString())
            val properties = createRandomProperties()
            properties.add(texturesProperty)

            val skin = ProfileSkin(id = getRandomString(), name = getRandomString(), properties = properties)
            assertEquals(texturesProperty.value, skin.skin)
        }

        @Test
        fun `signature texture property throw exception if textures property is not found`() {
            val skin = ProfileSkin(id = getRandomString(), name = getRandomString(), properties = createRandomProperties())
            assertThrows<NoSuchElementException> {
                skin.signature
            }
        }

        @Test
        fun `signature texture property refers the textures property`() {
            val texturesProperty = ProfileSkin.Property(name = "textures", getRandomString(), getRandomString())
            val properties = createRandomProperties()
            properties.add(texturesProperty)

            val skin = ProfileSkin(id = getRandomString(), name = getRandomString(), properties = properties)
            assertEquals(texturesProperty.signature, skin.signature)
        }
    }

    @Nested
    @DisplayName("Skin decoded")
    inner class SkinDecoded {

        @Test
        fun `decode the skin property to build object`() {
            val skinDecoded = ProfileSkinDecoded(
                timestamp = System.currentTimeMillis(),
                profileId = getRandomString(),
                profileName = getRandomString(),
                textures = ProfileSkinDecoded.Textures(
                    skin = ProfileSkinDecoded.Textures.Skin(
                        url = getRandomString(),
                        metadata = ProfileSkinDecoded.Textures.Skin.Metadata(
                            model = getRandomString()
                        )
                    ),
                    cape = ProfileSkinDecoded.Textures.Cape(
                        url = getRandomString()
                    )
                )
            )

            val encoded = Json.encodeToString(skinDecoded).encodeBase64()

            val profileSkin = ProfileSkin(id = getRandomString(), name = getRandomString(), properties = listOf(
                ProfileSkin.Property(name = "textures", value = encoded, signature = getRandomString())
            ))

            val skinDecodedLoad = profileSkin.getSkinDecoded()
            assertEquals(skinDecoded, skinDecodedLoad)
        }

    }

    private fun createRandomProperties() =
        List(5) { ProfileSkin.Property(name = getRandomString(), getRandomString()) }.toMutableList()
}