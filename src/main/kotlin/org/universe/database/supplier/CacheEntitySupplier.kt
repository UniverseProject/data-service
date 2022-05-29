package org.universe.database.supplier

import com.uchuhimo.konf.Config
import dev.kord.cache.api.DataCache
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.support.ConnectionPoolSupport.createGenericObjectPool
import kotlinx.serialization.protobuf.ProtoBuf
import org.apache.commons.pool2.impl.GenericObjectPool
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.universe.configuration.AppConfiguration
import org.universe.database.client.ClientIdentityService
import org.universe.database.dao.ClientIdentity
import org.universe.serializer.UUIDSerializer
import java.util.*

private val ENCODER = ProtoBuf {
    encodeDefaults = false
}

/**
 * [EntitySupplier] that uses a [DataCache] to resolve entities.
 */
@OptIn(ExperimentalLettuceCoroutinesApi::class)
class CacheEntitySupplier : EntitySupplier, KoinComponent {

    private val redis: RedisClient by inject()

    private val configuration: Config by inject()

    private var pool: GenericObjectPool<StatefulRedisConnection<ByteArray, ByteArray>> = createGenericObjectPool(
        { redis.connect(ByteArrayCodec.INSTANCE) },
        GenericObjectPoolConfig()
    )

    private inline fun <T> withConnection(body: (RedisCoroutinesCommands<ByteArray, ByteArray>) -> T): T {
        return pool.borrowObject().use {
            body(it.coroutines())
        }
    }

    private val clientIdentityCacheService = ClientIdentityCacheService()

    override suspend fun getIdentityByUUID(uuid: UUID): ClientIdentity? = clientIdentityCacheService.getByUUID(uuid)

    override suspend fun getIdentityByName(name: String): ClientIdentity? = clientIdentityCacheService.getByName(name)

    override suspend fun saveIdentity(identity: ClientIdentity) = clientIdentityCacheService.save(identity)

    internal inner class ClientIdentityCacheService : ClientIdentityService {

        private val prefixKey get() = configuration[AppConfiguration.CacheConfiguration.ClientIdentityConfiguration.prefixKey]

        private val cacheByUUID get() = configuration[AppConfiguration.CacheConfiguration.ClientIdentityConfiguration.cacheByUUID]

        private val cacheByName get() = configuration[AppConfiguration.CacheConfiguration.ClientIdentityConfiguration.cacheByName]

        override suspend fun getByUUID(uuid: UUID): ClientIdentity? {
            if (!cacheByUUID) {
                return null
            }

            return withConnection {
                val key = getKey(uuid)
                val nameSerial = it.get(key) ?: return null
                ClientIdentity(uuid, nameSerial.decodeToString())
            }
        }

        override suspend fun getByName(name: String): ClientIdentity? {
            if (!cacheByName) {
                return null
            }

            return withConnection {
                val key = getKey(name)
                val uuidSerial = it.get(key) ?: return null
                val uuid = decodeFromByteArrayToUUID(uuidSerial)
                ClientIdentity(uuid, name)
            }
        }

        override suspend fun save(identity: ClientIdentity) {
            if (cacheByUUID) {
                withConnection {
                    val key = getKey(identity.uuid)
                    val data = identity.name.encodeToByteArray()
                    it.set(key, data)
                }
            } else if (cacheByName) {
                withConnection {
                    val key = getKey(identity.name)
                    val data = encodeToByteArray(identity.uuid)
                    it.set(key, data)
                }
            }
        }

        private fun getKey(uuid: UUID): ByteArray = prefixKey.encodeToByteArray() + encodeToByteArray(uuid)

        private fun getKey(value: String): ByteArray = "$prefixKey$value".encodeToByteArray()

        private fun encodeToByteArray(uuid: UUID): ByteArray = ENCODER.encodeToByteArray(UUIDSerializer, uuid)

        private fun decodeFromByteArrayToUUID(uuidSerial: ByteArray): UUID =
            ENCODER.decodeFromByteArray(UUIDSerializer, uuidSerial)

    }
}