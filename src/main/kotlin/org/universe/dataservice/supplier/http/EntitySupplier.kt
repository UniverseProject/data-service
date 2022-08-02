package org.universe.dataservice.supplier.http

import io.github.universeproject.ProfileId
import io.github.universeproject.ProfileSkin

/**
 * An abstraction that allows for requesting entities.
 *
 * @see RestEntitySupplier
 * @see CacheEntitySupplier
 */
public interface EntitySupplier {

    /**
     * Retrieve the id information about a player with his name.
     * @param name Player's name.
     * @return Information about the player's id.
     */
    public suspend fun getUUID(name: String): ProfileId?

    /**
     * Retrieve the skin data for a player.
     * A player is represented by his UUID.
     * @param uuid Player's UUID.
     * @return Information about player's skin.
     */
    public suspend fun getSkin(uuid: String): ProfileSkin?

}
