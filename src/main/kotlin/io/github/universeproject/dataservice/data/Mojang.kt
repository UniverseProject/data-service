package io.github.universeproject.dataservice.data

import io.github.universeproject.ProfileId
import io.github.universeproject.ProfileSkin
import org.universe.dataservice.supplier.http.EntitySupplier
import org.universe.dataservice.supplier.http.Strategizable

/**
 * Service to retrieve data about players.
 */
public interface MojangService : Strategizable {

    /**
     * Get the skin data using the name of a player.
     * @param name Player's name.
     * @return Information about player's skin.
     */
    public suspend fun getSkinByName(name: String): ProfileSkin?

    /**
     * Retrieve the id information about a player with his name.
     * @param name Player's name.
     * @return Information about the player's id.
     */
    public suspend fun getId(name: String): ProfileId?

    /**
     * Retrieve the skin data for a player.
     * A player is represented by his UUID.
     * @param uuid Player's UUID.
     * @return Information about player's skin.
     */
    public suspend fun getSkin(uuid: String): ProfileSkin?
}

/**
 * Service to retrieve data using Mojang api.
 * @property supplier Strategy to retrieve data.
 */
public class MojangServiceImpl(override val supplier: EntitySupplier) : MojangService {

    override suspend fun getSkinByName(name: String): ProfileSkin? {
        return supplier.getUUID(name)?.let { supplier.getSkin(it.id) }
    }

    override suspend fun getId(name: String): ProfileId? {
        return supplier.getUUID(name)
    }

    override suspend fun getSkin(uuid: String): ProfileSkin? {
        return supplier.getSkin(uuid)
    }

    override fun withStrategy(strategy: EntitySupplier): Strategizable {
        return MojangServiceImpl(strategy)
    }
}