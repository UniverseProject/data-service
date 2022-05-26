package org.universe.http.supplier

import org.universe.http.mojang.model.ProfileId
import org.universe.http.mojang.model.ProfileSkin

/**
 * An abstraction that allows for requesting Discord entities.
 *
 * @see RestEntitySupplier
 * @see CacheEntitySupplier
 */
interface EntitySupplier {

    companion object {

        /**
         * A supplier providing a strategy which exclusively uses REST calls to fetch entities.
         * See [RestEntitySupplier] for more details.
         */
        val rest get() = RestEntitySupplier()

        /**
         * A supplier providing a strategy which exclusively uses cache to fetch entities.
         * See [CacheEntitySupplier] for more details.
         */
        val cache get() = CacheEntitySupplier()

        /**
         * A supplier providing a strategy which exclusively uses REST calls to fetch entities.
         * fetched entities are stored in [cache].
         * See [StoreEntitySupplier] for more details.
         */
        val cachingRest get() = StoreEntitySupplier(rest)

        /**
         * A supplier providing a strategy which will first operate on the [cache] supplier. When an entity
         * is not present from cache it will be fetched from [rest] instead. Operations that return flows
         * will only fall back to rest when the returned flow contained no elements.
         */
        val cacheWithRestFallback get() = cache.withFallback(rest)

        /**
         * A supplier providing a strategy which will first operate on the [cache] supplier. When an entity
         * is not present from cache it will be fetched from [cachingRest] instead which will update [cache] with fetched elements.
         * Operations that return flows will only fall back to rest when the returned flow contained no elements.
         */
        val cacheWithCachingRestFallback get() = cache.withFallback(cachingRest)
    }

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
