package org.universe.http.mojang

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.universe.http.mojang.model.ProfileId
import org.universe.http.mojang.model.ProfileSkin

/**
 * Implementation of the [MojangAPI] interface to interact with the Mojang API.
 * @property webClient web client to interact with api.
 */
class MojangAPIImpl : KoinComponent, MojangAPI {

    val webClient: HttpClient by inject()

    override suspend fun getId(name: String): ProfileId? {
        val response = webClient.get("https://api.mojang.com/user/profile/agent/minecraft/name/$name")
        return if (response.status == HttpStatusCode.NoContent) null else response.body()
    }

    override suspend fun getSkin(uuid: String): ProfileSkin? {
        val response = webClient.get("https://sessionserver.mojang.com/session/minecraft/profile/$uuid?unsigned=false")
        return if (response.status == HttpStatusCode.NoContent) null else response.body()
    }

}