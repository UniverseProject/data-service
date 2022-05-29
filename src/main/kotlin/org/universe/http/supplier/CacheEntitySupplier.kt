package org.universe.http.supplier

import org.koin.core.component.KoinComponent
import org.universe.cache.CacheClient
import org.universe.cache.ProfileIdCache
import org.universe.cache.ProfileSkinCache
import org.universe.model.ProfileId
import org.universe.model.ProfileSkin

/**
 * [EntitySupplier] that uses a [CacheClient] to resolve entities.
 */
class CacheEntitySupplier : EntitySupplier, KoinComponent {

    private val profileSkinCache = ProfileSkinCache()

    private val profileIdCache = ProfileIdCache()

    override suspend fun getId(name: String): ProfileId? = profileIdCache.getByName(name)

    override suspend fun getSkin(uuid: String): ProfileSkin? = profileSkinCache.getByUUID(uuid)

    suspend fun save(profile: ProfileId) {
        profileIdCache.save(profile)
    }

    suspend fun save(profile: ProfileSkin) {
        profileSkinCache.save(profile)
    }
}