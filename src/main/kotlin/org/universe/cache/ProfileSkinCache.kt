package org.universe.cache

import kotlinx.serialization.encodeToByteArray
import org.universe.configuration.CacheConfiguration
import org.universe.configuration.ServiceConfiguration
import org.universe.model.ProfileSkin

/**
 * Cache service for [ProfileSkin].
 * @property client Cache client.
 * @property prefixKey Prefix key to identify the data in cache.
 */
internal class ProfileSkinCache(val client: CacheClient) {

    private val prefixKey get() = ServiceConfiguration.cacheConfiguration[CacheConfiguration.ProfileSkinConfiguration.prefixKey]

    /**
     * Get the instance of [ProfileSkin] linked to the [uuid] data.
     * @param uuid UUID of the user.
     * @return The instance stored if found, or null if not found.
     */
    suspend fun getByUUID(uuid: String): ProfileSkin? {
        return client.connect {
            val key = getKey(uuid)
            val dataSerial = it.get(key) ?: return null
            client.binaryFormat.decodeFromByteArray(ProfileSkin.serializer(), dataSerial)
        }
    }

    /**
     * Save the instance into cache using the key defined by the configuration.
     * @param profile Data that will be stored.
     */
    suspend fun save(profile: ProfileSkin) {
        client.connect {
            val key = getKey(profile.id)
            val data = client.binaryFormat.encodeToByteArray(profile)
            it.set(key, data)
        }
    }

    /**
     * Create the key from a [String] value to identify data in cache.
     * @param value Value using to create key.
     * @return [ByteArray] corresponding to the key using the [prefixKey] and [value].
     */
    private fun getKey(value: String): ByteArray = "$prefixKey$value".encodeToByteArray()
}