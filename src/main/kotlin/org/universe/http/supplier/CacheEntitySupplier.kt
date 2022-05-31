package org.universe.http.supplier

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.universe.cache.CacheClient
import org.universe.cache.service.ProfileIdCacheService
import org.universe.cache.service.ProfileSkinCacheService
import org.universe.model.ProfileId
import org.universe.model.ProfileSkin

/**
 * [EntitySupplier] that uses a [CacheClient] to resolve entities.
 */
class CacheEntitySupplier : EntitySupplier, KoinComponent {

    private val client: CacheClient by inject()

    private val profileSkinCache = ProfileSkinCacheService(client, "skin:")

    private val profileIdCache = ProfileIdCacheService(client, "profId:")

    override suspend fun getId(name: String): ProfileId? = profileIdCache.getByName(name)

    override suspend fun getSkin(uuid: String): ProfileSkin? = profileSkinCache.getByUUID(uuid)

    suspend fun save(profile: ProfileId) {
        profileIdCache.save(profile)
    }

    suspend fun save(profile: ProfileSkin) {
        profileSkinCache.save(profile)
    }
}