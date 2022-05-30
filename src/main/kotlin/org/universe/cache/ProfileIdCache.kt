package org.universe.cache

import org.universe.configuration.CacheConfiguration
import org.universe.configuration.ServiceConfiguration
import org.universe.model.ProfileId

/**
 * Cache service for [ProfileId].
 * @property client Cache client.
 * @property prefixKey Prefix key to identify the data in cache.
 */
internal class ProfileIdCache(val client: CacheClient) {

    private val prefixKey get() = ServiceConfiguration.cacheConfiguration[CacheConfiguration.ProfileIdConfiguration.prefixKey]

    /**
     * Get the instance of [ProfileId] linked to the [name] data.
     * @param name Name of the user.
     * @return The instance stored if found, or null if not found.
     */
    suspend fun getByName(name: String): ProfileId? {
        return client.connect {
            val key = getKey(name)
            val dataSerial = it.get(key) ?: return null
            val uuid = dataSerial.decodeToString()
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
            it.set(key, profile.id.encodeToByteArray())
        }
    }

    /**
     * Create the key from a [String] value to identify data in cache.
     * @param value Value using to create key.
     * @return [ByteArray] corresponding to the key using the [prefixKey] and [value].
     */
    private fun getKey(value: String): ByteArray = "$prefixKey$value".encodeToByteArray()
}