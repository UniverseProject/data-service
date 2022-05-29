package org.universe.database.supplier

import dev.kord.cache.api.DataCache
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.universe.cache.CacheClient
import org.universe.configuration.CacheConfiguration
import org.universe.configuration.ServiceConfiguration.cacheConfiguration
import org.universe.database.client.ClientIdentityService
import org.universe.database.dao.ClientIdentity
import org.universe.serializer.UUIDSerializer
import java.util.*

/**
 * [ProtoBuf] is used to transform data to [ByteArray] and vice-versa.
 * [ByteArray] type is used to maximize the compatibility with all data type.
 */
private val PROTOBUF = ProtoBuf {
    encodeDefaults = false
}

/**
 * [EntitySupplier] that uses a [DataCache] to resolve entities.
 */
class CacheEntitySupplier : EntitySupplier, KoinComponent {

    private val clientIdentityCacheService = ClientIdentityCacheService()

    override suspend fun getIdentityByUUID(uuid: UUID): ClientIdentity? = clientIdentityCacheService.getByUUID(uuid)

    override suspend fun getIdentityByName(name: String): ClientIdentity? = clientIdentityCacheService.getByName(name)

    override suspend fun saveIdentity(identity: ClientIdentity) = clientIdentityCacheService.save(identity)
}

/**
 * Cache service for [ClientIdentity].
 * @property client Cache client.
 * @property prefixKey Prefix key to identify the data in cache.
 * @property cacheByUUID `true` if the data should be stored by the [uuid][ClientIdentity.uuid].
 * @property cacheByName `true` if the data should be stored by the [name][ClientIdentity.name].
 */
class ClientIdentityCacheService : ClientIdentityService, KoinComponent {

    private val client: CacheClient by inject()

    private val prefixKey get() = cacheConfiguration[CacheConfiguration.ClientIdentityConfiguration.prefixKey]

    private val cacheByUUID get() = cacheConfiguration[CacheConfiguration.ClientIdentityConfiguration.useUUID]

    private val cacheByName get() = cacheConfiguration[CacheConfiguration.ClientIdentityConfiguration.useName]

    override suspend fun getByUUID(uuid: UUID): ClientIdentity? {
        if (!cacheByUUID) {
            return null
        }

        return client.connect {
            val key = getKey(uuid)
            val nameSerial = it.get(key) ?: return null
            ClientIdentity(uuid, nameSerial.decodeToString())
        }
    }

    override suspend fun getByName(name: String): ClientIdentity? {
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

    override suspend fun save(identity: ClientIdentity) {
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
     * Transform a [UUID] to a [ByteArray] by encoding data using [PROTOBUF].
     * @param uuid UUID.
     * @return Result of the serialization of [uuid].
     */
    private fun encodeToByteArray(uuid: UUID): ByteArray = PROTOBUF.encodeToByteArray(UUIDSerializer, uuid)

    /***
     * Transform a [ByteArray] to a [UUID] by decoding data using [PROTOBUF].
     * @param uuidSerial Serialization of a [UUID] value.
     * @return The [UUID] value from the [uuidSerial] decoded.
     */
    private fun decodeFromByteArrayToUUID(uuidSerial: ByteArray): UUID =
        PROTOBUF.decodeFromByteArray(UUIDSerializer, uuidSerial)
}