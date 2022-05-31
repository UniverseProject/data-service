package org.universe.cache.service

import kotlinx.serialization.builtins.serializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.universe.cache.CacheClient
import org.universe.model.ProfileId

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
internal class ProfileIdCacheServiceImpl(
    prefixKey: String
) : CacheService(prefixKey), KoinComponent, ProfileIdCacheService {

    val client: CacheClient by inject()

    override suspend fun getByName(name: String): ProfileId? {
        return client.connect {
            val binaryFormat = client.binaryFormat
            val key = getKey(binaryFormat, name)
            val dataSerial = it.get(key) ?: return null
            val uuid = decodeFromByteArrayOrNull(binaryFormat, String.serializer(), dataSerial) ?: return null
            ProfileId(id = uuid, name = name)
        }
    }

    override suspend fun save(profile: ProfileId) {
        client.connect {
            val binaryFormat = client.binaryFormat
            val key = getKey(binaryFormat, profile.name)
            it.set(key, encodeToByteArray(binaryFormat, String.serializer(), profile.id))
        }
    }
}