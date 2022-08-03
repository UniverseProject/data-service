package org.universe.dataservice.supplier

import io.github.universeproject.MojangAPI
import org.universe.dataservice.cache.CacheClient
import org.universe.dataservice.data.*

public data class SupplierConfiguration(
    val mojangAPI: MojangAPI,
    val clientIdentityCache: ClientIdentityCacheService,
    val profileSkinCache: ProfileSkinCacheService,
    val profileIdCache: ProfileIdCacheService
) {
    public constructor(mojangAPI: MojangAPI, cacheClient: CacheClient) : this(
        mojangAPI,
        ClientIdentityCacheServiceImpl(cacheClient),
        ProfileSkinCacheServiceImpl(cacheClient),
        ProfileIdCacheServiceImpl(cacheClient)
    )
}