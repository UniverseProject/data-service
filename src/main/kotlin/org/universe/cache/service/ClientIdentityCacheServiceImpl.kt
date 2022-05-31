package org.universe.cache.service

import kotlinx.serialization.builtins.serializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.universe.cache.CacheClient
import org.universe.database.dao.ClientIdentity
import org.universe.serializer.UUIDSerializer
import java.util.*

interface ClientIdentityCacheService {
    /**
     * Get the identity of a client from his [ClientIdentity.uuid].
     * @param uuid Id of the user.
     * @return The instance stored if found, or null if not found.
     */
    suspend fun getByUUID(uuid: UUID): ClientIdentity?

    /**
     * Get the identity of a client from his [ClientIdentity.name].
     * @param name Name of the user.
     * @return The instance stored if found, or null if not found.
     */
    suspend fun getByName(name: String): ClientIdentity?

    /**
     * Save the instance into cache using the key defined by the configuration.
     * @param identity Data that will be stored.
     */
    suspend fun save(identity: ClientIdentity)
}

/**
 * Cache service for [ClientIdentity].
 * Only one of both [cacheByUUID] and [cacheByName] must be equals to `true`. In the other case, some performance issue should be provoked.
 * @property client Cache client.
 * @property prefixKey Prefix key to identify the data in cache.
 * @property cacheByUUID `true` if the data should be stored by the [uuid][ClientIdentity.uuid].
 * @property cacheByName `true` if the data should be stored by the [name][ClientIdentity.name].
 */
class ClientIdentityCacheServiceImpl(
    prefixKey: String,
    val cacheByUUID: Boolean,
    val cacheByName: Boolean
) : CacheService(prefixKey), KoinComponent, ClientIdentityCacheService {

    val client: CacheClient by inject()

    override suspend fun getByUUID(uuid: UUID): ClientIdentity? {
        if (!cacheByUUID) {
            return null
        }

        return client.connect {
            val binaryFormat = client.binaryFormat
            val key = getKey(binaryFormat, uuid.toString())
            val nameSerial = it.get(key) ?: return null
            val name = decodeFromByteArrayOrNull(binaryFormat, String.serializer(), nameSerial) ?: return null
            ClientIdentity(uuid, name)
        }
    }

    override suspend fun getByName(name: String): ClientIdentity? {
        if (!cacheByName) {
            return null
        }

        return client.connect {
            val binaryFormat = client.binaryFormat
            val key = getKey(binaryFormat, name)
            val uuidSerial = it.get(key) ?: return null
            val uuid = decodeFromByteArrayOrNull(binaryFormat, UUIDSerializer, uuidSerial) ?: return null
            ClientIdentity(uuid, name)
        }
    }

    override suspend fun save(identity: ClientIdentity) {
        if (cacheByUUID) {
            client.connect {
                val binaryFormat = client.binaryFormat
                val key = getKey(binaryFormat, identity.uuid.toString())
                val data = encodeToByteArray(binaryFormat, String.serializer(), identity.name)
                it.set(key, data)
            }
        } else if (cacheByName) {
            client.connect {
                val binaryFormat = client.binaryFormat
                val key = getKey(binaryFormat, identity.name)
                val data = encodeToByteArray(binaryFormat, UUIDSerializer, identity.uuid)
                it.set(key, data)
            }
        }
    }
}