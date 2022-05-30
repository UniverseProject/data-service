package org.universe.cache

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.serializer
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