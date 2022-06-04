package org.universe.dataservice.supplier.database

import org.universe.dataservice.data.ClientIdentity
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
        public val database: DatabaseEntitySupplier get() = DatabaseEntitySupplier()

        /**
         * A supplier providing a strategy which exclusively uses cache to fetch entities.
         * See [CacheEntitySupplier] for more details.
         */
        public val cache: CacheEntitySupplier get() = CacheEntitySupplier()

        /**
         * A supplier providing a strategy which exclusively uses database calls to fetch entities.
         * fetched entities are stored in [cache].
         * See [StoreEntitySupplier] for more details.
         */
        public val cachingDatabase: StoreEntitySupplier get() = StoreEntitySupplier(database)

        /**
         * A supplier providing a strategy which will first operate on the [cache] supplier. When an entity
         * is not present from cache it will be fetched from [database] instead. Operations that return flows
         * will only fall back to rest when the returned flow contained no elements.
         */
        public val cacheWithDatabaseFallback: EntitySupplier get() = cache.withFallback(database)

        /**
         * A supplier providing a strategy which will first operate on the [cache] supplier. When an entity
         * is not present from cache it will be fetched from [cachingDatabase] instead which will update [cache] with fetched elements.
         * Operations that return flows will only fall back to rest when the returned flow contained no elements.
         */
        public val cacheWithCachingDatabaseFallback: EntitySupplier get() = cache.withFallback(cachingDatabase)
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
