package io.github.universeproject.dataservice.supplier.http

import io.github.universeproject.ProfileId
import io.github.universeproject.ProfileSkin
import io.github.universeproject.dataservice.data.ProfileIdCacheService
import io.github.universeproject.dataservice.data.ProfileSkinCacheService

/**
 * [EntitySupplier] that uses [CacheService] to resolve entities.
 */
public class CacheEntitySupplier(
    public val profileSkinCache: ProfileSkinCacheService,
    public val profileIdCache: ProfileIdCacheService
) : EntitySupplier {

    override suspend fun getUUID(name: String): ProfileId? = profileIdCache.getByName(name)

    override suspend fun getSkin(uuid: String): ProfileSkin? = profileSkinCache.getByUUID(uuid)

    public suspend fun save(profile: ProfileId) {
        profileIdCache.save(profile)
    }

    public suspend fun save(profile: ProfileSkin) {
        profileSkinCache.save(profile)
    }
}