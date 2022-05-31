package org.universe.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.universe.cache.CacheClient
import org.universe.cache.CacheService
import org.universe.supplier.http.EntitySupplier
import org.universe.supplier.http.Strategizable

/**
 * Expected response of the Mojang api to retrieve id from a name.
 * @property name Player's name.
 * @property id Player's uuid.
 */
@Serializable
data class ProfileId(val name: String, val id: String)

/**
 * Service to manage [ProfileId] data in cache.
 */
interface ProfileIdCacheService {
    /**
     * Get the instance of [ProfileId] linked to the [name] data.
     * @param name Name of the user.
     * @return The instance stored if found, or null if not found.
     */
    suspend fun getByName(name: String): ProfileId?

    /**
     * Save the instance into cache using the key defined by the configuration.
     * @param profile Data that will be stored.
     */
    suspend fun save(profile: ProfileId)
}

/**
 * Cache service for [ProfileId].
 * @property client Cache client.
 * @property prefixKey Prefix key to identify the data in cache.
 */
class ProfileIdCacheServiceImpl(
    prefixKey: String
) : CacheService(prefixKey), KoinComponent, ProfileIdCacheService {

    val client: CacheClient by inject()

    override suspend fun getByName(name: String): ProfileId? {
        return client.connect {
            val binaryFormat = client.binaryFormat
            val key = encodeKey(binaryFormat, name)
            val dataSerial = it.get(key) ?: return null
            val uuid = decodeFromByteArrayOrNull(binaryFormat, String.serializer(), dataSerial) ?: return null
            ProfileId(id = uuid, name = name)
        }
    }

    override suspend fun save(profile: ProfileId) {
        client.connect {
            val binaryFormat = client.binaryFormat
            val key = encodeKey(binaryFormat, profile.name)
            it.set(key, encodeToByteArray(binaryFormat, String.serializer(), profile.id))
        }
    }
}

/**
 * Service to retrieve data about profile.
 */
interface ProfileIdService : Strategizable {

    /**
     * Get the profile of a client from his [ProfileId.name].
     * @param name Profile's name.
     */
    suspend fun getByName(name: String): ProfileId?
}

/**
 * Service to retrieve data about client identity.
 * @property supplier Strategy to manage data.
 */
class ProfileIdServiceImpl(override val supplier: EntitySupplier) : ProfileIdService, Strategizable {

    override suspend fun getByName(name: String): ProfileId? = supplier.getId(name)

    override fun withStrategy(strategy: EntitySupplier) = ProfileIdServiceImpl(strategy)
}