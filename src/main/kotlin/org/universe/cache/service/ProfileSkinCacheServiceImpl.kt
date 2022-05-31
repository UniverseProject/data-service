package org.universe.cache.service

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.universe.cache.CacheClient
import org.universe.model.ProfileSkin

interface ProfileSkinCacheService {
    /**
     * Get the instance of [ProfileSkin] linked to the [uuid] data.
     * @param uuid UUID of the user.
     * @return The instance stored if found, or null if not found.
     */
    suspend fun getByUUID(uuid: String): ProfileSkin?

    /**
     * Save the instance into cache using the key defined by the configuration.
     * @param profile Data that will be stored.
     */
    suspend fun save(profile: ProfileSkin)
}

/**
 * Cache service for [ProfileSkin].
 * @property client Cache client.
 * @property prefixKey Prefix key to identify the data in cache.
 */
class ProfileSkinCacheServiceImpl(
    prefixKey: String
) : CacheService(prefixKey), KoinComponent, ProfileSkinCacheService {

    val client: CacheClient by inject()

    override suspend fun getByUUID(uuid: String): ProfileSkin? {
        return client.connect {
            val binaryFormat = client.binaryFormat
            val key = getKey(binaryFormat, uuid)
            val dataSerial = it.get(key) ?: return null
            decodeFromByteArrayOrNull(binaryFormat, ProfileSkin.serializer(), dataSerial)
        }
    }

    override suspend fun save(profile: ProfileSkin) {
        client.connect {
            val binaryFormat = client.binaryFormat
            val key = getKey(binaryFormat, profile.id)
            val data = encodeToByteArray(binaryFormat, ProfileSkin.serializer(), profile)
            it.set(key, data)
        }
    }
}