package org.universe.cache.service

import org.universe.cache.CacheClient
import org.universe.model.ProfileSkin

/**
 * Cache service for [ProfileSkin].
 * @property client Cache client.
 * @property prefixKey Prefix key to identify the data in cache.
 */
internal class ProfileSkinCacheService(
    client: CacheClient,
    prefixKey: String
): CacheService(client, prefixKey) {

    /**
     * Get the instance of [ProfileSkin] linked to the [uuid] data.
     * @param uuid UUID of the user.
     * @return The instance stored if found, or null if not found.
     */
    suspend fun getByUUID(uuid: String): ProfileSkin? {
        return client.connect {
            val key = getKey(uuid)
            val dataSerial = it.get(key) ?: return null
            decodeFromByteArray(ProfileSkin.serializer(), dataSerial)
        }
    }

    /**
     * Save the instance into cache using the key defined by the configuration.
     * @param profile Data that will be stored.
     */
    suspend fun save(profile: ProfileSkin) {
        client.connect {
            val key = getKey(profile.id)
            val data = encodeToByteArray(ProfileSkin.serializer(), profile)
            it.set(key, data)
        }
    }
}