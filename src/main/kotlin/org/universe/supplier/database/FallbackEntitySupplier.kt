package org.universe.supplier.database

import org.universe.data.ClientIdentity
import java.util.*

/**
 * Creates supplier providing a strategy which will first operate on this supplier. When an entity
 * is not present from the first supplier it will be fetched from [other] instead. Operations that return flows
 * will only fall back to [other] when the returned flow contained no elements.
 */
infix fun EntitySupplier.withFallback(other: EntitySupplier): EntitySupplier =
    FallbackEntitySupplier(this, other)

/**
 * [EntitySupplier] that uses the first supplier to retrieve a data, if the value is null, get the data through the second supplier.
 */
class FallbackEntitySupplier(val first: EntitySupplier, val second: EntitySupplier) : EntitySupplier {

    override suspend fun getIdentityByUUID(uuid: UUID): ClientIdentity? = invoke { it.getIdentityByUUID(uuid) }

    override suspend fun getIdentityByName(name: String): ClientIdentity? = invoke { it.getIdentityByName(name) }

    override suspend fun saveIdentity(identity: ClientIdentity) {
        first.saveIdentity(identity)
    }

    /**
     * Invoke the body by [first] supplier, if the value returned is null, invoke the body by [second] supplier.
     * @param body Function executed by one or both supplier.
     * @return The instance returns by suppliers.
     */
    private inline fun <reified T> invoke(body: (EntitySupplier) -> T?): T? {
        return body(first) ?: body(second)
    }
}
