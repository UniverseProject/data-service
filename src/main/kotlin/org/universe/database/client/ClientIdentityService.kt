package org.universe.database.client

import org.universe.database.dao.ClientIdentity
import java.util.*

/**
 * Service to retrieve data about clients.
 */
interface ClientIdentityService {

    /**
     * Get the identity of a client from his [ClientIdentity.uuid].
     * @param uuid Id of the user.
     */
    suspend fun getByUUID(uuid: UUID): ClientIdentity?

    /**
     * Get the identity of a client from his [ClientIdentity.name].
     * @param name Name of the user.
     */
    suspend fun getByName(name: String): ClientIdentity?

    /**
     * Save a new identity.
     * @param identity Identity of a user.
     */
    suspend fun save(identity: ClientIdentity)
}