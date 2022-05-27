package org.universe.model

import kotlinx.serialization.Serializable

/**
 * Expected response of the Mojang api to retrieve id from a name.
 * @property name Player's name.
 * @property id Player's uuid.
 */
@Serializable
data class ProfileId(val name: String, val id: String)