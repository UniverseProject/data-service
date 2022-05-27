package org.universe.http.supplier

import dev.kord.cache.api.DataCache
import dev.kord.cache.api.put
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.universe.model.ProfileId
import org.universe.model.ProfileSkin

/**
 * [EntitySupplier] that delegates to another [EntitySupplier] to resolve entities.
 *
 * Resolved entities will always be stored in [cache] if it wasn't null or empty for flows.
 */
class StoreEntitySupplier(private val supplier: EntitySupplier) : EntitySupplier, KoinComponent {

    private val cache: DataCache by inject()

    override suspend fun getId(name: String): ProfileId? {
        return storeAndReturn(supplier.getId(name))
    }

    override suspend fun getSkin(uuid: String): ProfileSkin? {
        return storeAndReturn(supplier.getSkin(uuid))
    }

    /**
     * If the value is not null, store it into [cache].
     * @param value Value retrieve by the [supplier].
     * @return Instance sent into parameter.
     */
    private suspend inline fun <reified T> storeAndReturn(value: T?): T? {
        if (value == null) return null
        cache.put(value)
        return value
    }
}
