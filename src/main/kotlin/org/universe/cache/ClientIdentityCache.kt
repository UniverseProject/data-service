package org.universe.cache

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.serializer
import org.universe.configuration.CacheConfiguration
import org.universe.configuration.ServiceConfiguration
import org.universe.database.dao.ClientIdentity
import org.universe.serializer.UUIDSerializer
import java.util.*

/**
 * Cache service for [ClientIdentity].
 * Only one of both [cacheByUUID] and [cacheByName] must be equals to `true`. In the other case, some performance issue should be provoked.
 * @property client Cache client.
 * @property prefixKey Prefix key to identify the data in cache.
 * @property cacheByUUID `true` if the data should be stored by the [uuid][ClientIdentity.uuid].
 * @property cacheByName `true` if the data should be stored by the [name][ClientIdentity.name].
 */
internal class ClientIdentityCache(val client: CacheClient) {

    private val prefixKey get() = ServiceConfiguration.cacheConfiguration[CacheConfiguration.ClientIdentityConfiguration.prefixKey]

    private val cacheByUUID get() = ServiceConfiguration.cacheConfiguration[CacheConfiguration.ClientIdentityConfiguration.useUUID]

    private val cacheByName get() = ServiceConfiguration.cacheConfiguration[CacheConfiguration.ClientIdentityConfiguration.useName]

    /**
     * Get the identity of a client from his [ClientIdentity.uuid].
     * If [cacheByUUID] is equals to `true`, the instance will be found by the [uuid][ClientIdentity.uuid].
     * Otherwise, the value returned is null.
     * @param uuid Id of the user.
     * @return The instance stored if found, or null if not found.
     */
    suspend fun getByUUID(uuid: UUID): ClientIdentity? {
        if (!cacheByUUID) {
            return null
        }

        return client.connect {
            val key = getKey(uuid.toString())
            val nameSerial = it.get(key) ?: return null
            ClientIdentity(uuid, decodeFromByteArray(String.serializer(), nameSerial))
        }
    }

    /**
     * Get the identity of a client from his [ClientIdentity.name].
     * If [cacheByName] is equals to `true`, the instance will be found by the [name][ClientIdentity.name].
     * Otherwise, the value returned is null.
     * @param name Name of the user.
     * @return The instance stored if found, or null if not found.
     */
    suspend fun getByName(name: String): ClientIdentity? {
        if (!cacheByName) {
            return null
        }

        return client.connect {
            val key = getKey(name)
            val uuidSerial = it.get(key) ?: return null
            val uuid = decodeFromByteArray(UUIDSerializer, uuidSerial)
            ClientIdentity(uuid, name)
        }
    }

    /**
     * Save the instance into cache using the key defined by the configuration.
     * If [cacheByUUID] is equals to `true`, the key to store the data will be built from the [uuid][ClientIdentity.uuid]
     * and will be findable only using the [uuid][ClientIdentity.uuid].
     * Otherwise, if [cacheByName] is equals to `true`, the key to store the data will be built from the [name][ClientIdentity.name]
     * and will be findable only using the [name][ClientIdentity.name].
     * @param identity Data that will be stored.
     */
    suspend fun save(identity: ClientIdentity) {
        if (cacheByUUID) {
            client.connect {
                val key = getKey(identity.uuid.toString())
                val data = encodeToByteArray(String.serializer(), identity.name)
                it.set(key, data)
            }
        } else if (cacheByName) {
            client.connect {
                val key = getKey(identity.name)
                val data = encodeToByteArray(UUIDSerializer, identity.uuid)
                it.set(key, data)
            }
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