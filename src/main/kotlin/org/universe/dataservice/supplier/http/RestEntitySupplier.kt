package org.universe.dataservice.supplier.http

import io.ktor.client.*
import io.ktor.client.plugins.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.universe.dataservice.data.MojangAPI
import org.universe.dataservice.data.ProfileId
import org.universe.dataservice.data.ProfileSkin

/**
 * [EntitySupplier] that uses a [HttpClient] to resolve entities.
 *
 * Error codes besides 429(Too Many Requests) will throw a [ClientRequestException],
 * 404(Not Found) will be caught by the `xOrNull` variant and return null instead.
 */
public class RestEntitySupplier : EntitySupplier, KoinComponent {

    private val mojangAPI: MojangAPI by inject()

    override suspend fun getId(name: String): ProfileId? {
        return mojangAPI.getId(name)
    }

    override suspend fun getSkin(uuid: String): ProfileSkin? {
        return mojangAPI.getSkin(uuid)
    }
}
