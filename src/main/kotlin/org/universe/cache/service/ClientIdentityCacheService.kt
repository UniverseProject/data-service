package org.universe.cache.service

import kotlinx.serialization.builtins.serializer
import org.universe.cache.CacheClient
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
internal class ClientIdentityCacheService(
    client: CacheClient,
    prefixKey: String,
    private val cacheByUUID: Boolean,
    private val cacheByName: Boolean
) : CacheService(client, prefixKey) {

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
            val name = decodeFromByteArrayOrNull(String.serializer(), nameSerial) ?: return null
            ClientIdentity(uuid, name)
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
            val uuid = decodeFromByteArrayOrNull(UUIDSerializer, uuidSerial) ?: return null
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
}