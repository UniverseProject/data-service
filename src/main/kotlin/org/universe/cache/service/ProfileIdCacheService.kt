package org.universe.cache.service

import kotlinx.serialization.builtins.serializer
import org.universe.cache.CacheClient
import org.universe.model.ProfileId

/**
 * Cache service for [ProfileId].
 * @property client Cache client.
 * @property prefixKey Prefix key to identify the data in cache.
 */
internal class ProfileIdCacheService(
    client: CacheClient,
    prefixKey: String
): CacheService(client, prefixKey) {

    /**
     * Get the instance of [ProfileId] linked to the [name] data.
     * @param name Name of the user.
     * @return The instance stored if found, or null if not found.
     */
    suspend fun getByName(name: String): ProfileId? {
        return client.connect {
            val key = getKey(name)
            val dataSerial = it.get(key) ?: return null
            val uuid = decodeFromByteArray(String.serializer(), dataSerial)
            ProfileId(id = uuid, name = name)
        }
    }

    /**
     * Save the instance into cache using the key defined by the configuration.
     * @param profile Data that will be stored.
     */
    suspend fun save(profile: ProfileId) {
        client.connect {
            val key = getKey(profile.name)
            it.set(key, encodeToByteArray(String.serializer(), profile.id))
        }
    }
}