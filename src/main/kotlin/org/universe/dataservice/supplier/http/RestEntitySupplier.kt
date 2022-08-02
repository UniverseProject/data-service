package org.universe.dataservice.supplier.http

import io.github.universeproject.MojangAPI
import io.github.universeproject.ProfileId
import io.github.universeproject.ProfileSkin
import io.ktor.client.*

/**
 * [EntitySupplier] that uses a [HttpClient] to resolve entities.
 */
public class RestEntitySupplier(private val mojangAPI: MojangAPI) : EntitySupplier {

    override suspend fun getUUID(name: String): ProfileId? {
        return mojangAPI.getUUID(name)
    }

    override suspend fun getSkin(uuid: String): ProfileSkin? {
        return mojangAPI.getSkin(uuid)
    }
}
