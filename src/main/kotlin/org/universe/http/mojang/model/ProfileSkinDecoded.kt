package org.universe.http.mojang.model

import io.ktor.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Structure of the skin information when the texture property is decoded.
 * @property timestamp Timestamp when the request is sent.
 * @property profileId UUID of account.
 * @property profileName Account username.
 * @property textures Textures information.
 */
@Serializable
data class ProfileSkinDecoded(
    val timestamp: Long,
    val profileId: String,
    val profileName: String,
    val textures: Textures
) {
    companion object {

        private val json: Json = Json {
            ignoreUnknownKeys = true
        }

        /**
         * Read a Base64 encoded value and load a new instance of [ProfileSkinDecoded] with the json retrieved.
         * @param value String encoded with Base64.
         * @return A new instance of [ProfileSkinDecoded].
         */
        fun fromEncoded(value: String): ProfileSkinDecoded {
            return json.decodeFromString(value.decodeBase64String())
        }
    }

    /**
     * Contains all textures information about a player.
     * @property skin Information about the skin.
     * @property cape Information about the cape.
     */
    @Serializable
    data class Textures(
        @SerialName("SKIN") val skin: Skin,
        @SerialName("CAPE") val cape: Cape? = null
    ) {

        /**
         * Contains all information about the skin of a player.
         * @property url URL to get the skin.
         * @property metadata Metadata of the skin.
         */
        @Serializable
        data class Skin(val url: String, val metadata: Metadata = Metadata()) {

            /**
             * Metadata of the skin texture.
             * @property model alex (slim) or steve (classic).
             */
            @Serializable
            data class Metadata(val model: String = "classic")

        }

        /**
         * Contains all information about the cape of a player.
         * @property url URL to get the skin.
         */
        @Serializable
        data class Cape(val url: String)

    }
}