package org.universe.dataservice.supplier.database

import org.universe.dataservice.data.ClientIdentity
import java.util.*

/**
 * [EntitySupplier] that delegates to another [EntitySupplier] to resolve entities.
 *
 * Resolved entities will always be stored in [cache] if it wasn't null or empty for flows.
 */
public class StoreEntitySupplier(private val cache: CacheEntitySupplier, private val supplier: EntitySupplier) :
    EntitySupplier {

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
