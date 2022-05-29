package org.universe.cache

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
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
internal class ClientIdentityCache : KoinComponent {

    private val client: CacheClient by inject()

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
            val key = getKey(uuid)
            val nameSerial = it.get(key) ?: return null
            ClientIdentity(uuid, nameSerial.decodeToString())
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
            val uuid = decodeFromByteArrayToUUID(uuidSerial)
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
                val key = getKey(identity.uuid)
                val data = identity.name.encodeToByteArray()
                it.set(key, data)
            }
        } else if (cacheByName) {
            client.connect {
                val key = getKey(identity.name)
                val data = encodeToByteArray(identity.uuid)
                it.set(key, data)
            }
        }
    }

    /**
     * Create the key from the [uuid][ClientIdentity.uuid] to identify data in cache.
     * @param uuid Id of a client.
     * @return [ByteArray] corresponding to the key using the [prefixKey] and [uuid].
     */
    private fun getKey(uuid: UUID): ByteArray = prefixKey.encodeToByteArray() + encodeToByteArray(uuid)

    /**
     * Create the key from a [String] value to identify data in cache.
     * @param value Value using to create key.
     * @return [ByteArray] corresponding to the key using the [prefixKey] and [value].
     */
    private fun getKey(value: String): ByteArray = "$prefixKey$value".encodeToByteArray()

    /**
     * Transform a [UUID] to a [ByteArray] by encoding data using [binaryFormat][CacheClient.binaryFormat].
     * @param uuid UUID.
     * @return Result of the serialization of [uuid].
     */
    private fun encodeToByteArray(uuid: UUID): ByteArray = client.binaryFormat.encodeToByteArray(UUIDSerializer, uuid)

    /***
     * Transform a [ByteArray] to a [UUID] by decoding data using [binaryFormat][CacheClient.binaryFormat].
     * @param uuidSerial Serialization of a [UUID] value.
     * @return The [UUID] value from the [uuidSerial] decoded.
     */
    private fun decodeFromByteArrayToUUID(uuidSerial: ByteArray): UUID =
        client.binaryFormat.decodeFromByteArray(UUIDSerializer, uuidSerial)
}