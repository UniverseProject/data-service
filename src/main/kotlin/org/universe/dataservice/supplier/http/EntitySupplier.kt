package org.universe.dataservice.supplier.http

import org.universe.dataservice.data.ProfileId
import org.universe.dataservice.data.ProfileSkin

/**
 * An abstraction that allows for requesting entities.
 *
 * @see RestEntitySupplier
 * @see CacheEntitySupplier
 */
public interface EntitySupplier {

    public companion object {

        /**
         * A supplier providing a strategy which exclusively uses REST calls to fetch entities.
         * See [RestEntitySupplier] for more details.
         */
        public val rest: RestEntitySupplier get() = RestEntitySupplier()

        /**
         * A supplier providing a strategy which exclusively uses cache to fetch entities.
         * See [CacheEntitySupplier] for more details.
         */
        public val cache: CacheEntitySupplier get() = CacheEntitySupplier()

        /**
         * A supplier providing a strategy which exclusively uses REST calls to fetch entities.
         * fetched entities are stored in [cache].
         * See [StoreEntitySupplier] for more details.
         */
        public val cachingRest: StoreEntitySupplier get() = StoreEntitySupplier(rest)

        /**
         * A supplier providing a strategy which will first operate on the [cache] supplier. When an entity
         * is not present from cache it will be fetched from [rest] instead. Operations that return flows
         * will only fall back to rest when the returned flow contained no elements.
         */
        public val cacheWithRestFallback: EntitySupplier get() = cache.withFallback(rest)

        /**
         * A supplier providing a strategy which will first operate on the [cache] supplier. When an entity
         * is not present from cache it will be fetched from [cachingRest] instead which will update [cache] with fetched elements.
         * Operations that return flows will only fall back to rest when the returned flow contained no elements.
         */
        public val cacheWithCachingRestFallback: EntitySupplier get() = cache.withFallback(cachingRest)
    }

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
