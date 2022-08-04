package io.github.universeproject.dataservice.supplier.database

import io.github.universeproject.dataservice.data.ClientIdentities
import io.github.universeproject.dataservice.data.ClientIdentity
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

/**
 * [EntitySupplier] that uses a database client to resolve entities.
 */
public class DatabaseEntitySupplier : EntitySupplier {

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
