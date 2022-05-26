package org.universe.http.mojang

import org.universe.http.mojang.model.ProfileId
import org.universe.http.mojang.model.ProfileSkin
import org.universe.http.supplier.Strategizable

/**
 * Service to retrieve data about players.
 */
interface MojangService : Strategizable {

    /**
     * Get the skin data using the name of a player.
     * @param name Player's name.
     * @return Information about player's skin.
     */
    suspend fun getSkinByName(name: String): ProfileSkin?

    /**
     * Retrieve the id information about a player with his name.
     * @param name Player's name.
     * @return Information about the player's id.
     */
    suspend fun getId(name: String): ProfileId?

    /**
     * Retrieve the skin data for a player.
     * A player is represented by his UUID.
     * @param uuid Player's UUID.
     * @return Information about player's skin.
     */
    suspend fun getSkin(uuid: String): ProfileSkin?
}