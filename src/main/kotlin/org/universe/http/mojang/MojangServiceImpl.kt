package org.universe.http.mojang

import org.universe.http.mojang.model.ProfileId
import org.universe.http.mojang.model.ProfileSkin
import org.universe.http.supplier.EntitySupplier
import org.universe.http.supplier.Strategizable

/**
 * Service to retrieve data using Mojang api.
 * @property supplier Strategy to retrieve data.
 */
class MojangServiceImpl(override val supplier: EntitySupplier) : MojangService {

    override suspend fun getSkinByName(name: String): ProfileSkin? {
        return supplier.getId(name)?.let { supplier.getSkin(it.id) }
    }

    override suspend fun getId(name: String): ProfileId? {
        return supplier.getId(name)
    }

    override suspend fun getSkin(uuid: String): ProfileSkin? {
        return supplier.getSkin(uuid)
    }

    override fun withStrategy(strategy: EntitySupplier): Strategizable {
        return MojangServiceImpl(strategy)
    }
}