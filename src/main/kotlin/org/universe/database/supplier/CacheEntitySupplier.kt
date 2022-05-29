package org.universe.database.supplier

import org.koin.core.component.KoinComponent
import org.universe.cache.CacheClient
import org.universe.cache.ClientIdentityCache
import org.universe.database.dao.ClientIdentity
import java.util.*

/**
 * [EntitySupplier] that uses a [CacheClient] to resolve entities.
 */
class CacheEntitySupplier : EntitySupplier, KoinComponent {

    private val clientIdentityCache = ClientIdentityCache()

    override suspend fun getIdentityByUUID(uuid: UUID): ClientIdentity? = clientIdentityCache.getByUUID(uuid)

    override suspend fun getIdentityByName(name: String): ClientIdentity? = clientIdentityCache.getByName(name)

    override suspend fun saveIdentity(identity: ClientIdentity) = clientIdentityCache.save(identity)
}

