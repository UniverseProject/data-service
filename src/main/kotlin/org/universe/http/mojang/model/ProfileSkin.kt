package org.universe.http.mojang.model

import kotlinx.serialization.Serializable


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
data class ProfileSkin(val id: String, val name: String, val properties: List<Property> = emptyList(), val legacy: Boolean = false) {

    @Serializable
    data class Property(val name: String, val value: String, val signature: String? = null)

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
    fun getTexturesProperty() = properties.first { it.name == PROPERTY_TEXTURES }

    /**
     * Decode the [skin] value.
     * @return All information present into the encoded value.
     */
    fun getSkinDecoded() = ProfileSkinDecoded.fromEncoded(skin)

}