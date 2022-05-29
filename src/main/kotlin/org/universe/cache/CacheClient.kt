package org.universe.cache

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.support.ConnectionPoolSupport
import org.apache.commons.pool2.impl.GenericObjectPool
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.universe.configuration.CacheConfiguration
import org.universe.configuration.ServiceConfiguration

/**
 * Wrapper of [RedisClient] using pool to manage connection.
 * @property client Redis client.
 * @property pool Pool of connection from [client].
 */
class CacheClient(private val client: RedisClient) {

    val pool: GenericObjectPool<StatefulRedisConnection<ByteArray, ByteArray>> =
        ConnectionPoolSupport.createGenericObjectPool(
            { client.connect(ByteArrayCodec.INSTANCE) },
            GenericObjectPoolConfig<StatefulRedisConnection<ByteArray, ByteArray>>().apply {
                minIdle = ServiceConfiguration.cacheConfiguration[CacheConfiguration.PoolConfiguration.minIdle]
                maxIdle = ServiceConfiguration.cacheConfiguration[CacheConfiguration.PoolConfiguration.maxIdle]
                maxTotal = ServiceConfiguration.cacheConfiguration[CacheConfiguration.PoolConfiguration.maxTotal]
            }
        )

    /**
     * Use a connection from the [pool] to interact with the cache.
     * At the end of the method, the connection is returned to the pool.
     * @param body Function using the connection.
     * @return An instance from [body].
     */
    inline fun <T> connect(body: (RedisCoroutinesCommands<ByteArray, ByteArray>) -> T): T {
        return pool.borrowObject().use {
            body(it.coroutines())
        }
    }
}