package org.universe.supplier.http

import org.universe.cache.CacheClient
import org.universe.data.*
import org.universe.extension.getPropertyOrEnv

/**
 * [EntitySupplier] that uses a [CacheClient] to resolve entities.
 */
class CacheEntitySupplier(
    private val profileSkinCache: ProfileSkinCacheService = ProfileSkinCacheServiceImpl(
        getPropertyOrEnv("cache.skin.prefix") ?: "skin:"
    ),
    private val profileIdCache: ProfileIdCacheService = ProfileIdCacheServiceImpl(
        getPropertyOrEnv("cache.profilId.prefix") ?: "skin:"
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