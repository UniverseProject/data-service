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
