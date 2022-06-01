package org.universe.dataservice.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.universe.dataservice.supplier.http.EntitySupplier
import org.universe.dataservice.supplier.http.Strategizable

/**
 * Use to interact with Mojang api.
 */
interface MojangAPI {

    /**
     * Retrieve the id information about a player with his name.
     * https://mojang-api-docs.netlify.app/no-auth/username-to-uuid-get.html
     * @param name Player's name.
     * @return Id information.
     */
    suspend fun getId(name: String): ProfileId?

    /**
     * Retrieve the skin data for a player.
     * A player is represented by his UUID.
     * https://mojang-api-docs.netlify.app/no-auth/uuid-to-profile.html
     * @param uuid Player's UUID.
     * @return Information about player's skin.
     */
    suspend fun getSkin(uuid: String): ProfileSkin?
}

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

/**
 * Service to retrieve data about players.
 */
interface MojangService : Strategizable {

    /**
     * Get the skin data using the name of a player.
     * @param name Player's name.
     * @return Information about player's skin.
     */
    suspend fun getSkinByName(name: String): ProfileSkin?

    /**
     * Retrieve the id information about a player with his name.
     * @param name Player's name.
     * @return Information about the player's id.
     */
    suspend fun getId(name: String): ProfileId?

    /**
     * Retrieve the skin data for a player.
     * A player is represented by his UUID.
     * @param uuid Player's UUID.
     * @return Information about player's skin.
     */
    suspend fun getSkin(uuid: String): ProfileSkin?
}

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