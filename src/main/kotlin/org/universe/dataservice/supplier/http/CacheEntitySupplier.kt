package org.universe.dataservice.supplier.http

import org.universe.dataservice.data.*
import org.universe.dataservice.extension.getPropertyOrEnv
import org.universe.dataservice.cache.CacheService

/**
 * [EntitySupplier] that uses [CacheService] to resolve entities.
 */
class CacheEntitySupplier(
    val profileSkinCache: ProfileSkinCacheService = ProfileSkinCacheServiceImpl(
        getPropertyOrEnv("cache.skin.prefixKey") ?: "skin:"
    ),
    val profileIdCache: ProfileIdCacheService = ProfileIdCacheServiceImpl(
        getPropertyOrEnv("cache.profilId.prefixKey") ?: "profId:"
    )
) : EntitySupplier {

    override suspend fun getId(name: String): ProfileId? = profileIdCache.getByName(name)

    override suspend fun getSkin(uuid: String): ProfileSkin? = profileSkinCache.getByUUID(uuid)

    suspend fun save(profile: ProfileId) {
        profileIdCache.save(profile)
    }

    suspend fun save(profile: ProfileSkin) {
        profileSkinCache.save(profile)
    }
}