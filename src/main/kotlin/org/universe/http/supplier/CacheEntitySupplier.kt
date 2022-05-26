package org.universe.http.supplier

import dev.kord.cache.api.DataCache
import dev.kord.cache.api.query
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.universe.http.mojang.model.ProfileId
import org.universe.http.mojang.model.ProfileSkin

/**
 * [EntitySupplier] that uses a [DataCache] to resolve entities.
 */
class CacheEntitySupplier : EntitySupplier, KoinComponent {

    private val cache: DataCache by inject()

    override suspend fun getId(name: String): ProfileId? {
        return cache.query<ProfileId> { ProfileId::name eq name }.singleOrNull()
    }

    override suspend fun getSkin(uuid: String): ProfileSkin? {
        return cache.query<ProfileSkin> { ProfileSkin::id eq uuid }.singleOrNull()
    }
}