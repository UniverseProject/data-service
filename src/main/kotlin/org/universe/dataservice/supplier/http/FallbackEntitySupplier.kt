package org.universe.dataservice.supplier.http

import io.github.universeproject.ProfileId
import io.github.universeproject.ProfileSkin

/**
 * Creates supplier providing a strategy which will first operate on this supplier. When an entity
 * is not present from the first supplier it will be fetched from [other] instead. Operations that return flows
 * will only fall back to [other] when the returned flow contained no elements.
 */
public infix fun EntitySupplier.withFallback(other: EntitySupplier): EntitySupplier =
    FallbackEntitySupplier(this, other)

/**
 * [EntitySupplier] that uses the first supplier to retrieve a data, if the value is null, get the data through the second supplier.
 */
public class FallbackEntitySupplier(public val first: EntitySupplier, public val second: EntitySupplier) :
    EntitySupplier {

    override suspend fun getUUID(name: String): ProfileId? {
        return invoke { it.getUUID(name) }
    }

    override suspend fun getSkin(uuid: String): ProfileSkin? {
        return invoke { it.getSkin(uuid) }
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
