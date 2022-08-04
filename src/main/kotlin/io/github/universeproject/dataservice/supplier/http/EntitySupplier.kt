package io.github.universeproject.dataservice.supplier.http

import io.github.universeproject.ProfileId
import io.github.universeproject.ProfileSkin
import io.github.universeproject.dataservice.supplier.SupplierConfiguration

/**
 * An abstraction that allows for requesting entities.
 *
 * @see RestEntitySupplier
 * @see CacheEntitySupplier
 */
public interface EntitySupplier {

    public companion object {

        /**
         * A supplier providing a strategy which exclusively uses database calls to fetch entities.
         * See [RestEntitySupplier] for more details.
         */
        public fun rest(configuration: SupplierConfiguration): RestEntitySupplier =
            RestEntitySupplier(configuration.mojangAPI)

        /**
         * A supplier providing a strategy which exclusively uses cache to fetch entities.
         * See [CacheEntitySupplier] for more details.
         */
        public fun cache(configuration: SupplierConfiguration): CacheEntitySupplier =
            CacheEntitySupplier(configuration.profileSkinCache, configuration.profileIdCache)

        /**
         * A supplier providing a strategy which exclusively uses database calls to fetch entities.
         * fetched entities are stored in [cache].
         * See [StoreEntitySupplier] for more details.
         */
        public fun cachingRest(configuration: SupplierConfiguration): StoreEntitySupplier =
            StoreEntitySupplier(cache(configuration), rest(configuration))

        /**
         * A supplier providing a strategy which will first operate on the [cache] supplier. When an entity
         * is not present from cache it will be fetched from [rest] instead. Operations that return flows
         * will only fall back to rest when the returned flow contained no elements.
         */
        public fun cacheWithRestFallback(configuration: SupplierConfiguration): EntitySupplier =
            cache(configuration) withFallback rest(configuration)

        /**
         * A supplier providing a strategy which will first operate on the [cache] supplier. When an entity
         * is not present from cache it will be fetched from [cachingRest] instead which will update [cache] with fetched elements.
         * Operations that return flows will only fall back to rest when the returned flow contained no elements.
         */
        public fun cacheWithCachingRestFallback(configuration: SupplierConfiguration): EntitySupplier =
            cache(configuration) withFallback cachingRest(configuration)
    }

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
