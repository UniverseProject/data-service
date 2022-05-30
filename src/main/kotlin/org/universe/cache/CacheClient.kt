package org.universe.cache

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.support.AsyncConnectionPoolSupport
import io.lettuce.core.support.BoundedAsyncPool
import io.lettuce.core.support.BoundedPoolConfig
import kotlinx.coroutines.future.await
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * Wrapper of [RedisClient] using pool to manage connection.
 * @property uri URI to connect the client.
 * @property client Redis client.
 * @property binaryFormat Object to encode and decode information.
 * @property pool Pool of connection from [client].
 */
class CacheClient(
    val uri: RedisURI,
    val client: RedisClient = RedisClient.create(),
    val binaryFormat: BinaryFormat = DEFAULT_BINARY_FORMAT,
    poolConfiguration: BoundedPoolConfig = BoundedPoolConfig.builder()
        .maxTotal(-1)
        .build()
) : AutoCloseable {

    companion object {
        /**
         * [ProtoBuf] is used to transform data to [ByteArray] and vice-versa.
         * [ByteArray] type is used to maximize the compatibility with all data type.
         */
        val DEFAULT_BINARY_FORMAT = ProtoBuf {
            encodeDefaults = false
        }
    }

    val pool: BoundedAsyncPool<StatefulRedisConnection<ByteArray, ByteArray>> =
        AsyncConnectionPoolSupport.createBoundedObjectPool(
            { client.connectAsync(ByteArrayCodec.INSTANCE, uri) },
            poolConfiguration
        )

    /**
     * Use a connection from the [pool] to interact with the cache.
     * At the end of the method, the connection is returned to the pool.
     * @param body Function using the connection.
     * @return An instance from [body].
     */
    suspend inline fun <T> connect(body: (RedisCoroutinesCommands<ByteArray, ByteArray>) -> T): T {
        val connection = pool.acquire().await()
        return try {
            body(connection.coroutines())
        } finally {
            pool.release(connection).await()
        }
    }

    override fun close() {
        try {
            pool.close()
        } finally {
            client.shutdown()
        }
    }

    /**
     * Requests to close this object and releases any system resources associated with it. If the object is already closed then invoking this method has no effect.
     * All connections from the [pool] will be closed.
     */
    suspend fun closeAsync() {
        try {
            pool.closeAsync().await()
        } finally {
            client.shutdownAsync().await()
        }
    }

}