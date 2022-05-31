package org.universe.database.supplier

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.universe.cache.CacheClient
import org.universe.cache.service.ClientIdentityCacheService
import org.universe.database.dao.ClientIdentity
import org.universe.extension.getEnvOrProperty
import java.util.*

/**
 * [EntitySupplier] that uses a [CacheClient] to resolve entities.
 */
class CacheEntitySupplier : EntitySupplier, KoinComponent {

    private val client: CacheClient by inject()

    private val clientIdentityCache: ClientIdentityCacheService = ClientIdentityCacheService(
        client,
        prefixKey = getEnvOrProperty("cache.clientId.prefix") ?: "cliId:",
        cacheByUUID = (getEnvOrProperty("cache.clientId.useUUID") ?: true.toString()).toBooleanStrict(),
        cacheByName = (getEnvOrProperty("cache.clientId.useName") ?: true.toString()).toBooleanStrict()
    )

    override suspend fun getIdentityByUUID(uuid: UUID): ClientIdentity? = clientIdentityCache.getByUUID(uuid)

    override suspend fun getIdentityByName(name: String): ClientIdentity? = clientIdentityCache.getByName(name)

    override suspend fun saveIdentity(identity: ClientIdentity) = clientIdentityCache.save(identity)
}

