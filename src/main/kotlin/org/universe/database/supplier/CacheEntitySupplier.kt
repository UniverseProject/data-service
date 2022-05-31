package org.universe.database.supplier

import org.universe.cache.CacheClient
import org.universe.cache.service.ClientIdentityCacheService
import org.universe.cache.service.ClientIdentityCacheServiceImpl
import org.universe.database.dao.ClientIdentity
import org.universe.extension.getPropertyOrEnv
import java.util.*

/**
 * [EntitySupplier] that uses a [CacheClient] to resolve entities.
 */
class CacheEntitySupplier(
    val clientIdentityCache: ClientIdentityCacheService = ClientIdentityCacheServiceImpl(
        prefixKey = getPropertyOrEnv("cache.clientId.prefixKey") ?: "cliId:",
        cacheByUUID = (getPropertyOrEnv("cache.clientId.useUUID") ?: true.toString()).toBooleanStrict(),
        cacheByName = (getPropertyOrEnv("cache.clientId.useName") ?: false.toString()).toBooleanStrict()
    )
) : EntitySupplier {

    override suspend fun getIdentityByUUID(uuid: UUID): ClientIdentity? = clientIdentityCache.getByUUID(uuid)

    override suspend fun getIdentityByName(name: String): ClientIdentity? = clientIdentityCache.getByName(name)

    override suspend fun saveIdentity(identity: ClientIdentity) = clientIdentityCache.save(identity)
}

