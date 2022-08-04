@file:OptIn(ExperimentalLettuceCoroutinesApi::class)

package io.github.universeproject.dataservice.data

import io.github.universeproject.dataservice.cache.CacheClient
import io.github.universeproject.dataservice.cache.CacheService
import io.github.universeproject.dataservice.serializer.UUIDSerializer
import io.github.universeproject.dataservice.supplier.database.EntitySupplier
import io.github.universeproject.dataservice.supplier.database.Strategizable
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import java.util.*

/**
 * Max length of a minecraft player.
 */
public const val MAX_NAME_LENGTH: Int = 16

/**
 * SQL table to interact with [ClientIdentity] data in database.
 */
public object ClientIdentities : Table(ClientIdentity::class.simpleName!!) {
    public val uuid: Column<UUID> = uuid("uuid").uniqueIndex()
    public val name: Column<String> = varchar("name", MAX_NAME_LENGTH)
    override val primaryKey: PrimaryKey = PrimaryKey(uuid)
}

/**
 * Identity to link the [uuid] of a player with his name.
 * @property uuid Player's UUID.
 * @property name Player's name.
 */
@kotlinx.serialization.Serializable
public data class ClientIdentity(
    @Serializable(with = UUIDSerializer::class)
    var uuid: UUID,
    var name: String
)

/**
 * Service to manage [ClientIdentity] data in cache.
 */
public interface ClientIdentityCacheService {
    /**
     * Get the identity of a client from his [ClientIdentity.uuid].
     * @param uuid Id of the user.
     * @return The instance stored if found, or null if not found.
     */
    public suspend fun getByUUID(uuid: UUID): ClientIdentity?

    /**
     * Get the identity of a client from his [ClientIdentity.name].
     * @param name Name of the user.
     * @return The instance stored if found, or null if not found.
     */
    public suspend fun getByName(name: String): ClientIdentity?

    /**
     * Save the instance into cache using the key defined by the configuration.
     * @param identity Data that will be stored.
     */
    public suspend fun save(identity: ClientIdentity)
}

/**
 * Cache service for [ClientIdentity].
 * Only one of both [cacheByUUID] and [cacheByName] must be equals to `true`. In the other case, some performance issue should be provoked.
 * @property client Cache client.
 * @property cacheByUUID `true` if the data should be stored by the [uuid][ClientIdentity.uuid].
 * @property cacheByName `true` if the data should be stored by the [name][ClientIdentity.name].
 */
public class ClientIdentityCacheServiceImpl(
    public val client: CacheClient,
    prefixKey: String = "cliId:",
    public val cacheByUUID: Boolean = true,
    public val cacheByName: Boolean = false
) : CacheService(prefixKey), ClientIdentityCacheService {

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
public interface ClientIdentityService : Strategizable {

    /**
     * Get the identity of a client from his [ClientIdentity.uuid].
     * @param uuid Id of the user.
     */
    public suspend fun getByUUID(uuid: UUID): ClientIdentity?

    /**
     * Get the identity of a client from his [ClientIdentity.name].
     * @param name Name of the user.
     */
    public suspend fun getByName(name: String): ClientIdentity?

    /**
     * Save a new identity.
     * @param identity Identity of a user.
     */
    public suspend fun save(identity: ClientIdentity)
}

/**
 * Service to retrieve data about client identity.
 * @property supplier Strategy to manage data.
 */
public class ClientIdentityServiceImpl(override val supplier: EntitySupplier) : ClientIdentityService {

    override suspend fun getByUUID(uuid: UUID): ClientIdentity? = supplier.getIdentityByUUID(uuid)

    override suspend fun getByName(name: String): ClientIdentity? = supplier.getIdentityByName(name)

    override suspend fun save(identity: ClientIdentity) {
        supplier.saveIdentity(identity)
    }

    override fun withStrategy(strategy: EntitySupplier): ClientIdentityService = ClientIdentityServiceImpl(strategy)
}