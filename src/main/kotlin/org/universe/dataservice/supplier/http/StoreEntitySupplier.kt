package org.universe.dataservice.supplier.http

import io.github.universeproject.ProfileId
import io.github.universeproject.ProfileSkin

/**
 * [EntitySupplier] that delegates to another [EntitySupplier] to resolve entities.
 *
 * Resolved entities will always be stored in [cache] if it wasn't null or empty for flows.
 */
public class StoreEntitySupplier(private val cache: CacheEntitySupplier, private val supplier: EntitySupplier) :
    EntitySupplier {

    override suspend fun getUUID(name: String): ProfileId? {
        return supplier.getUUID(name)?.also { cache.save(it) }
    }

    override suspend fun getSkin(uuid: String): ProfileSkin? {
        return supplier.getSkin(uuid)?.also { cache.save(it) }
    }
}
