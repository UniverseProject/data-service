package org.universe.dataservice.supplier.http

import org.universe.dataservice.supplier.SupplierConfiguration

public interface EntitySupplyStrategy<T : EntitySupplier> {

    public companion object {

        /**
         * A supplier providing a strategy which exclusively uses database calls to fetch entities.
         * See [RestEntitySupplier] for more details.
         */
        public val rest: EntitySupplyStrategy<RestEntitySupplier> =
            object : EntitySupplyStrategy<RestEntitySupplier> {

                override fun supply(configuration: SupplierConfiguration): RestEntitySupplier =
                    RestEntitySupplier(configuration.mojangAPI)

            }

        /**
         * A supplier providing a strategy which exclusively uses cache to fetch entities.
         * See [CacheEntitySupplier] for more details.
         */
        public val cache: EntitySupplyStrategy<CacheEntitySupplier> =
            object : EntitySupplyStrategy<CacheEntitySupplier> {

                override fun supply(configuration: SupplierConfiguration): CacheEntitySupplier =
                    CacheEntitySupplier(configuration.profileSkinCache, configuration.profileIdCache)

            }

        /**
         * A supplier providing a strategy which exclusively uses database calls to fetch entities.
         * fetched entities are stored in [cache].
         * See [StoreEntitySupplier] for more details.
         */
        public val cachingRest: EntitySupplyStrategy<StoreEntitySupplier> =
            object : EntitySupplyStrategy<StoreEntitySupplier> {

                override fun supply(configuration: SupplierConfiguration): StoreEntitySupplier =
                    StoreEntitySupplier(cache.supply(configuration), rest.supply(configuration))

            }

        /**
         * A supplier providing a strategy which will first operate on the [cache] supplier. When an entity
         * is not present from cache it will be fetched from [rest] instead. Operations that return flows
         * will only fall back to rest when the returned flow contained no elements.
         */
        public val cacheWithRestFallback: EntitySupplyStrategy<EntitySupplier> =
            object : EntitySupplyStrategy<EntitySupplier> {

                override fun supply(configuration: SupplierConfiguration): EntitySupplier =
                    cache.supply(configuration) withFallback rest.supply(configuration)

            }

        /**
         * A supplier providing a strategy which will first operate on the [cache] supplier. When an entity
         * is not present from cache it will be fetched from [cachingRest] instead which will update [cache] with fetched elements.
         * Operations that return flows will only fall back to rest when the returned flow contained no elements.
         */
        public val cacheWithCachingRestFallback: EntitySupplyStrategy<EntitySupplier> =
            object : EntitySupplyStrategy<EntitySupplier> {

                override fun supply(configuration: SupplierConfiguration): EntitySupplier =
                    cache.supply(configuration) withFallback cachingRest.supply(configuration)

            }
    }

    /**
     * Returns an [EntitySupplier] of type [T].
     */
    public fun supply(configuration: SupplierConfiguration): T

}