package org.universe.supplier.database

import org.universe.cache.CacheClient
import org.universe.data.ClientIdentity
import org.universe.data.ClientIdentityCacheService
import org.universe.data.ClientIdentityCacheServiceImpl
import org.universe.extension.getPropertyOrEnv
import java.util.*

/**
 * [EntitySupplier] that uses a [CacheClient] to resolve entities.
 */
class CacheEntitySupplier(
    val clientIdentityCache: ClientIdentityCacheService = ClientIdentityCacheServiceImpl(
        prefixKey = getPropertyOrEnv("cache.clientId.prefixKey") ?: "cliId:",
        cacheByUUID = getPropertyOrEnv("cache.clientId.useUUID")?.toBooleanStrict() ?: true,
        cacheByName = getPropertyOrEnv("cache.clientId.useName")?.toBooleanStrict() ?: false
    )
) : EntitySupplier {

    override suspend fun getIdentityByUUID(uuid: UUID): ClientIdentity? = clientIdentityCache.getByUUID(uuid)

    override suspend fun getIdentityByName(name: String): ClientIdentity? = clientIdentityCache.getByName(name)

    override suspend fun saveIdentity(identity: ClientIdentity) = clientIdentityCache.save(identity)
}

