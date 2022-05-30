package org.universe.cache

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.serializer
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

    /**
     * Create the key from a [String] value to identify data in cache.
     * @param value Value using to create key.
     * @return [ByteArray] corresponding to the key using the [prefixKey] and [value].
     */
    private fun getKey(value: String): ByteArray = encodeToByteArray(String.serializer(), "$prefixKey$value")

    /**
     * Transform an instance to a [ByteArray] by encoding data using [binaryFormat][CacheClient.binaryFormat].
     * @param value Value that will be serialized.
     * @return Result of the serialization of [value].
     */
    private fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray =
        client.binaryFormat.encodeToByteArray(serializer, value)

    /***
     * Transform a [ByteArray] to a value by decoding data using [binaryFormat][CacheClient.binaryFormat].
     * @param valueSerial Serialization of the value.
     * @return The value from the [valueSerial] decoded.
     */
    private fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, valueSerial: ByteArray): T =
        client.binaryFormat.decodeFromByteArray(deserializer, valueSerial)
}