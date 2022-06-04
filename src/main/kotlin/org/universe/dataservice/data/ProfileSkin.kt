package org.universe.dataservice.data

import io.ktor.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.universe.dataservice.cache.CacheClient
import org.universe.dataservice.cache.CacheService
import org.universe.dataservice.supplier.http.EntitySupplier
import org.universe.dataservice.supplier.http.Strategizable

/**
 * Name of the property to set and get textures.
 */
private const val PROPERTY_TEXTURES = "textures"

/**
 * Expected response of the Mojang api to retrieve player skin from an uuid.
 * https://mojang-api-docs.netlify.app/no-auth/uuid-to-profile.html?highlight=get%20skin#uuid-to-user-profile-skins-capes-etc
 * @property id Player's uuid.
 * @property name Player's name.
 * @property properties List of the properties, in the documentation, the list contains only once element who is the texture property.
 * @property legacy `true` if the account is not migrated (2010-2012), `false` otherwise.
 * @property skin Base64 of the skin texture information.
 * @property signature Signature of the base64-encoded texture information.
 */
@Serializable
public data class ProfileSkin(
    val id: String,
    val name: String,
    val properties: List<Property> = emptyList(),
    val legacy: Boolean = false
) {

    @Serializable
    public data class Property(val name: String, val value: String, val signature: String? = null)

    val skin: String
        get() = getTexturesProperty().value

    val signature: String?
        get() = getTexturesProperty().signature

    /**
     * Get the property for the texture.
     * In the documentation, only one property is sent.
     * So the property defined as texture property, is the only element.
     * @return The texture property.
     */
    public fun getTexturesProperty(): Property = properties.first { it.name == PROPERTY_TEXTURES }

    /**
     * Decode the [skin] value.
     * @return All information present into the encoded value.
     */
    public fun getSkinDecoded(): ProfileSkinDecoded = ProfileSkinDecoded.fromEncoded(skin)

}

/**
 * Structure of the skin information when the texture property is decoded.
 * @property timestamp Timestamp when the request is sent.
 * @property profileId UUID of account.
 * @property profileName Account username.
 * @property textures Textures information.
 */
@Serializable
public data class ProfileSkinDecoded(
    val timestamp: Long,
    val profileId: String,
    val profileName: String,
    val textures: Textures
) {
    public companion object {

        private val json: Json = Json {
            ignoreUnknownKeys = true
        }

        /**
         * Read a Base64 encoded value and load a new instance of [ProfileSkinDecoded] with the json retrieved.
         * @param value String encoded with Base64.
         * @return A new instance of [ProfileSkinDecoded].
         */
        public fun fromEncoded(value: String): ProfileSkinDecoded {
            return json.decodeFromString(value.decodeBase64String())
        }
    }

    /**
     * Contains all textures information about a player.
     * @property skin Information about the skin.
     * @property cape Information about the cape.
     */
    @Serializable
    public data class Textures(
        @SerialName("SKIN") val skin: Skin,
        @SerialName("CAPE") val cape: Cape? = null
    ) {

        /**
         * Contains all information about the skin of a player.
         * @property url URL to get the skin.
         * @property metadata Metadata of the skin.
         */
        @Serializable
        public data class Skin(val url: String, val metadata: Metadata = Metadata()) {

            /**
             * Metadata of the skin texture.
             * @property model alex (slim) or steve (classic).
             */
            @Serializable
            public data class Metadata(val model: String = "classic")

        }

        /**
         * Contains all information about the cape of a player.
         * @property url URL to get the skin.
         */
        @Serializable
        public data class Cape(val url: String)

    }
}

public interface ProfileSkinCacheService {
    /**
     * Get the instance of [ProfileSkin] linked to the [uuid] data.
     * @param uuid UUID of the user.
     * @return The instance stored if found, or null if not found.
     */
    public suspend fun getByUUID(uuid: String): ProfileSkin?

    /**
     * Save the instance into cache using the key defined by the configuration.
     * @param profile Data that will be stored.
     */
    public suspend fun save(profile: ProfileSkin)
}

/**
 * Cache service for [ProfileSkin].
 * @property client Cache client.
 * @property prefixKey Prefix key to identify the data in cache.
 */
public class ProfileSkinCacheServiceImpl(
    prefixKey: String
) : CacheService(prefixKey), KoinComponent, ProfileSkinCacheService {

    public val client: CacheClient by inject()

    override suspend fun getByUUID(uuid: String): ProfileSkin? {
        return client.connect {
            val binaryFormat = client.binaryFormat
            val key = encodeKey(binaryFormat, uuid)
            val dataSerial = it.get(key) ?: return null
            decodeFromByteArrayOrNull(binaryFormat, ProfileSkin.serializer(), dataSerial)
        }
    }

    override suspend fun save(profile: ProfileSkin) {
        client.connect {
            val binaryFormat = client.binaryFormat
            val key = encodeKey(binaryFormat, profile.id)
            val data = encodeToByteArray(binaryFormat, ProfileSkin.serializer(), profile)
            it.set(key, data)
        }
    }
}

/**
 * Service to retrieve data about profile.
 */
public interface ProfileSkinService : Strategizable {

    /**
     * Get the skin information of a player from his [ProfileSkin.id].
     * @param uuid Profile's id.
     */
    public suspend fun getByUUID(uuid: String): ProfileSkin?
}

/**
 * Service to retrieve data about client identity.
 * @property supplier Strategy to manage data.
 */
public class ProfileSkinServiceImpl(override val supplier: EntitySupplier) : ProfileSkinService {

    override suspend fun getByUUID(uuid: String): ProfileSkin? = supplier.getSkin(uuid)

    override fun withStrategy(strategy: EntitySupplier): ProfileSkinService = ProfileSkinServiceImpl(strategy)
}