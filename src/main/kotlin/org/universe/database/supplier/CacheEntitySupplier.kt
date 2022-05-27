package org.universe.database.supplier

import dev.kord.cache.api.DataCache
import dev.kord.cache.api.put
import dev.kord.cache.api.query
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.universe.database.dao.ClientIdentity
import java.util.*

/**
 * [EntitySupplier] that uses a [DataCache] to resolve entities.
 */
class CacheEntitySupplier : EntitySupplier, KoinComponent {

    private val cache: DataCache by inject()

    override suspend fun getIdentityByUUID(uuid: UUID): ClientIdentity? =
        cache.query<ClientIdentity> { ClientIdentity::uuid eq uuid }.singleOrNull()

    override suspend fun getIdentityByName(name: String): ClientIdentity? =
        cache.query<ClientIdentity> { ClientIdentity::name eq name }.singleOrNull()

    override suspend fun saveIdentity(identity: ClientIdentity) {
        cache.put(identity)
    }

}