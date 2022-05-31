package org.universe.database.supplier

import org.koin.core.component.KoinComponent
import org.universe.database.dao.ClientIdentity
import java.util.*

/**
 * [EntitySupplier] that delegates to another [EntitySupplier] to resolve entities.
 *
 * Resolved entities will always be stored in [cache] if it wasn't null or empty for flows.
 */
class StoreEntitySupplier(private val supplier: EntitySupplier) : EntitySupplier, KoinComponent {

    private val cache get() = EntitySupplier.cache

    override suspend fun getIdentityByUUID(uuid: UUID): ClientIdentity? =
        saveIdentityInCache(supplier.getIdentityByUUID(uuid))

    override suspend fun getIdentityByName(name: String): ClientIdentity? =
        saveIdentityInCache(supplier.getIdentityByName(name))

    override suspend fun saveIdentity(identity: ClientIdentity) {
        supplier.saveIdentity(identity)
    }

    /**
     *  If the value is not null, store it into [cache].
     * @param identity Value retrieve by the [supplier].
     * @return Instance sent into parameter.
     */
    private suspend fun saveIdentityInCache(identity: ClientIdentity?): ClientIdentity? {
        return identity?.also { cache.saveIdentity(it) }
    }
}
