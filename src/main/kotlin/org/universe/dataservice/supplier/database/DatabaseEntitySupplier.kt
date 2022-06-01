package org.universe.dataservice.supplier.database

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.universe.dataservice.data.ClientIdentities
import org.universe.dataservice.data.ClientIdentity
import java.util.*

/**
 * [EntitySupplier] that uses a database client to resolve entities.
 */
class DatabaseEntitySupplier : EntitySupplier {

    override suspend fun getIdentityByUUID(uuid: UUID): ClientIdentity? = newSuspendedTransaction {
        ClientIdentities.select { ClientIdentities.uuid eq uuid }.singleOrNull()?.let {
            ClientIdentity(it[ClientIdentities.uuid], it[ClientIdentities.name])
        }
    }

    override suspend fun getIdentityByName(name: String): ClientIdentity? = newSuspendedTransaction {
        ClientIdentities.select { ClientIdentities.name eq name }.singleOrNull()?.let {
            ClientIdentity(it[ClientIdentities.uuid], it[ClientIdentities.name])
        }
    }

    override suspend fun saveIdentity(identity: ClientIdentity) {
        newSuspendedTransaction {
            ClientIdentities.insert {
                it[uuid] = identity.uuid
                it[name] = identity.name
            }
        }
    }
}
