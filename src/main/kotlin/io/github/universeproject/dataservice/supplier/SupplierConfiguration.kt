package io.github.universeproject.dataservice.supplier

import io.github.universeproject.MojangAPI
import io.github.universeproject.dataservice.cache.CacheClient
import org.universe.dataservice.data.*

public data class SupplierConfiguration(
    val mojangAPI: MojangAPI,
    val clientIdentityCache: ClientIdentityCacheService,
    val profileSkinCache: ProfileSkinCacheService,
    val profileIdCache: ProfileIdCacheService
) {
    public constructor(mojangAPI: MojangAPI, cacheClient: io.github.universeproject.dataservice.cache.CacheClient) : this(
        mojangAPI,
        ClientIdentityCacheServiceImpl(cacheClient),
        ProfileSkinCacheServiceImpl(cacheClient),
        ProfileIdCacheServiceImpl(cacheClient)
    )
}