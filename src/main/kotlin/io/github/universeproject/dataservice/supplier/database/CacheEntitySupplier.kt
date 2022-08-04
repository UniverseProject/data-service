package io.github.universeproject.dataservice.supplier.database

import org.universe.dataservice.cache.CacheService
import org.universe.dataservice.data.ClientIdentity
import org.universe.dataservice.data.ClientIdentityCacheService
import java.util.*

/**
 * [EntitySupplier] that uses [CacheService] to resolve entities.
 */
public class CacheEntitySupplier(public val clientIdentityCache: ClientIdentityCacheService) : EntitySupplier {

    override suspend fun getIdentityByUUID(uuid: UUID): ClientIdentity? = clientIdentityCache.getByUUID(uuid)

    override suspend fun getIdentityByName(name: String): ClientIdentity? = clientIdentityCache.getByName(name)

    override suspend fun saveIdentity(identity: ClientIdentity): Unit = clientIdentityCache.save(identity)
}

