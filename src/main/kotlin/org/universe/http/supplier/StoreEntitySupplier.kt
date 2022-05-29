package org.universe.http.supplier

import org.koin.core.component.KoinComponent
import org.universe.model.ProfileId
import org.universe.model.ProfileSkin

/**
 * [EntitySupplier] that delegates to another [EntitySupplier] to resolve entities.
 *
 * Resolved entities will always be stored in [cache] if it wasn't null or empty for flows.
 */
class StoreEntitySupplier(private val supplier: EntitySupplier) : EntitySupplier, KoinComponent {

    private val cache get() = EntitySupplier.cache

    override suspend fun getId(name: String): ProfileId? {
        return supplier.getId(name)?.also { cache.save(it) }
    }

    override suspend fun getSkin(uuid: String): ProfileSkin? {
        return supplier.getSkin(uuid)?.also { cache.save(it) }
    }
}
