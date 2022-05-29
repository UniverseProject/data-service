package org.universe.database.client

import org.universe.database.dao.ClientIdentity
import org.universe.database.supplier.EntitySupplier
import org.universe.database.supplier.Strategizable
import java.util.*

/**
 * Service to retrieve data about client identity.
 * @property supplier Strategy to manage data.
 */
class ClientIdentityServiceImpl(override val supplier: EntitySupplier) : ClientIdentityService, Strategizable {

    override suspend fun getByUUID(uuid: UUID): ClientIdentity? = supplier.getIdentityByUUID(uuid)

    override suspend fun getByName(name: String): ClientIdentity? = supplier.getIdentityByName(name)

    override suspend fun save(identity: ClientIdentity) {
        supplier.saveIdentity(identity)
    }

    override fun withStrategy(strategy: EntitySupplier) = ClientIdentityServiceImpl(strategy)
}