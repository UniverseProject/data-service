package io.github.universeproject.dataservice.supplier.database

import io.github.universeproject.dataservice.data.ClientIdentity
import io.github.universeproject.dataservice.data.ClientIdentityCacheService
import java.util.*

/**
 * [EntitySupplier] that uses [CacheService] to resolve entities.
 */
public class CacheEntitySupplier(public val clientIdentityCache: ClientIdentityCacheService) : EntitySupplier {

    override suspend fun getIdentityByUUID(uuid: UUID): ClientIdentity? = clientIdentityCache.getByUUID(uuid)

    override suspend fun getIdentityByName(name: String): ClientIdentity? = clientIdentityCache.getByName(name)

    override suspend fun saveIdentity(identity: ClientIdentity): Unit = clientIdentityCache.save(identity)
}

