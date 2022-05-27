package org.universe.http.mojang

import org.universe.model.ProfileId
import org.universe.model.ProfileSkin

/**
 * Use to interact with Mojang api.
 */
interface MojangAPI {

    /**
     * Retrieve the id information about a player with his name.
     * https://mojang-api-docs.netlify.app/no-auth/username-to-uuid-get.html
     * @param name Player's name.
     * @return Id information.
     */
    suspend fun getId(name: String): ProfileId?

    /**
     * Retrieve the skin data for a player.
     * A player is represented by his UUID.
     * https://mojang-api-docs.netlify.app/no-auth/uuid-to-profile.html
     * @param uuid Player's UUID.
     * @return Information about player's skin.
     */
    suspend fun getSkin(uuid: String): ProfileSkin?
}