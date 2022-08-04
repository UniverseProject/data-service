package io.github.universeproject.dataservice.supplier.database

import io.github.universeproject.dataservice.data.ClientIdentity
import io.github.universeproject.dataservice.supplier.SupplierConfiguration
import java.util.*

/**
 * An abstraction that allows for requesting entities.
 *
 * @see DatabaseEntitySupplier
 * @see CacheEntitySupplier
 */
public interface EntitySupplier {

    public companion object {

        /**
         * A supplier providing a strategy which exclusively uses database calls to fetch entities.
         * See [DatabaseEntitySupplier] for more details.
         */
        public fun database(): DatabaseEntitySupplier = DatabaseEntitySupplier()

        /**
         * A supplier providing a strategy which exclusively uses cache to fetch entities.
         * See [CacheEntitySupplier] for more details.
         */
        public fun cache(configuration: SupplierConfiguration): CacheEntitySupplier =
            CacheEntitySupplier(configuration.clientIdentityCache)

        /**
         * A supplier providing a strategy which exclusively uses database calls to fetch entities.
         * fetched entities are stored in [cache].
         * See [StoreEntitySupplier] for more details.
         */
        public fun cachingDatabase(configuration: SupplierConfiguration): StoreEntitySupplier =
            StoreEntitySupplier(cache(configuration), database())

        /**
         * A supplier providing a strategy which will first operate on the [cache] supplier. When an entity
         * is not present from cache it will be fetched from [database] instead. Operations that return flows
         * will only fall back to rest when the returned flow contained no elements.
         */
        public fun cacheWithDatabaseFallback(configuration: SupplierConfiguration): EntitySupplier =
            cache(configuration) withFallback database()

        /**
         * A supplier providing a strategy which will first operate on the [cache] supplier. When an entity
         * is not present from cache it will be fetched from [cachingDatabase] instead which will update [cache] with fetched elements.
         * Operations that return flows will only fall back to rest when the returned flow contained no elements.
         */
        public fun cacheWithCachingDatabaseFallback(configuration: SupplierConfiguration): EntitySupplier =
            cache(configuration) withFallback cachingDatabase(configuration)

    }

    /**
     * Get the identity of a client from his [ClientIdentity.uuid].
     * @param uuid Id of the user.
     */
    public suspend fun getIdentityByUUID(uuid: UUID): ClientIdentity?

    /**
     * Get the identity of a client from his [ClientIdentity.name].
     * @param name Name of the user.
     */
    public suspend fun getIdentityByName(name: String): ClientIdentity?

    /**
     * Save a new identity.
     * @param identity Identity of a user.
     */
    public suspend fun saveIdentity(identity: ClientIdentity)

}
