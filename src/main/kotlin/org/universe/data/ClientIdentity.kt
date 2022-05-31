package org.universe.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.jetbrains.exposed.sql.Table
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.universe.cache.CacheClient
import org.universe.cache.CacheService
import org.universe.serializer.UUIDSerializer
import org.universe.supplier.database.EntitySupplier
import org.universe.supplier.database.Strategizable
import java.util.*

/**
 * Max length of a minecraft player.
 */
const val MAX_NAME_LENGTH = 16

/**
 * SQL table to interact with [ClientIdentity] data in database.
 */
object ClientIdentities : Table(ClientIdentity::class.simpleName!!) {
    val uuid = uuid("uuid").uniqueIndex()
    val name = varchar("name", MAX_NAME_LENGTH)
    override val primaryKey = PrimaryKey(uuid)
}

/**
 * Identity to link the [uuid] of a player with his name.
 * @property uuid Player's UUID.
 * @property name Player's name.
 */
@Serializable
data class ClientIdentity(
    @Serializable(with = UUIDSerializer::class)
    var uuid: UUID,
    var name: String
)

/**
 * Service to manage [ClientIdentity] data in cache.
 */
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
            val key = encodeKey(binaryFormat, uuid.toString())
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
            val key = encodeKey(binaryFormat, name)
            val uuidSerial = it.get(key) ?: return null
            val uuid = decodeFromByteArrayOrNull(binaryFormat, UUIDSerializer, uuidSerial) ?: return null
            ClientIdentity(uuid, name)
        }
    }

    override suspend fun save(identity: ClientIdentity) {
        if (cacheByUUID) {
            client.connect {
                val binaryFormat = client.binaryFormat
                val key = encodeKey(binaryFormat, identity.uuid.toString())
                val data = encodeToByteArray(binaryFormat, String.serializer(), identity.name)
                it.set(key, data)
            }
        } else if (cacheByName) {
            client.connect {
                val binaryFormat = client.binaryFormat
                val key = encodeKey(binaryFormat, identity.name)
                val data = encodeToByteArray(binaryFormat, UUIDSerializer, identity.uuid)
                it.set(key, data)
            }
        }
    }
}

/**
 * Service to retrieve data about clients.
 */
interface ClientIdentityService : Strategizable {

    /**
     * Get the identity of a client from his [ClientIdentity.uuid].
     * @param uuid Id of the user.
     */
    suspend fun getByUUID(uuid: UUID): ClientIdentity?

    /**
     * Get the identity of a client from his [ClientIdentity.name].
     * @param name Name of the user.
     */
    suspend fun getByName(name: String): ClientIdentity?

    /**
     * Save a new identity.
     * @param identity Identity of a user.
     */
    suspend fun save(identity: ClientIdentity)
}

/**
 * Service to retrieve data about client identity.
 * @property supplier Strategy to manage data.
 */
class ClientIdentityServiceImpl(override val supplier: EntitySupplier) : ClientIdentityService {

    override suspend fun getByUUID(uuid: UUID): ClientIdentity? = supplier.getIdentityByUUID(uuid)

    override suspend fun getByName(name: String): ClientIdentity? = supplier.getIdentityByName(name)

    override suspend fun save(identity: ClientIdentity) {
        supplier.saveIdentity(identity)
    }

    override fun withStrategy(strategy: EntitySupplier) = ClientIdentityServiceImpl(strategy)
}