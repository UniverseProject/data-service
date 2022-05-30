package org.universe.cache

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.support.BoundedPoolConfig
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.universe.container.createRedisContainer
import java.util.concurrent.CompletableFuture
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Testcontainers
class CacheClientTest {

    companion object {
        @JvmStatic
        @Container
        val redisContainer = createRedisContainer()
    }

    @Test
    fun `pool is used to get connection from client`() = runBlocking {
        val client = CacheClient(RedisURI.create(redisContainer.url))
        val pool = client.pool

        assertEquals(0, pool.objectCount)
        assertEquals(0, pool.idle)

        client.connect {
            assertEquals(1, pool.objectCount)
            assertEquals(0, pool.idle)

            client.connect {
                assertEquals(2, pool.objectCount)
                assertEquals(0, pool.idle)
            }

            assertEquals(2, pool.objectCount)
            assertEquals(1, pool.idle)
        }
        assertEquals(2, pool.objectCount)
        assertEquals(2, pool.idle)

        client.connect {
            assertEquals(2, pool.objectCount)
            assertEquals(1, pool.idle)
        }
    }

    @Test
    fun `close instance will stop the client`() {
        val redisClient = mockk<RedisClient>()
        justRun { redisClient.shutdown() }
        val client = CacheClient(RedisURI.create(redisContainer.url), client = redisClient)

        client.close()

        verify(exactly = 1) { redisClient.shutdown() }

        assertNull(client.pool.closeAsync().getNow(mockk()))
    }

    @Test
    fun `close async instance will stop the client`() = runBlocking {
        val redisClient = mockk<RedisClient>()
        every { redisClient.shutdownAsync() } returns CompletableFuture.completedFuture(mockk())
        val client = CacheClient(RedisURI.create(redisContainer.url), client = redisClient)

        client.closeAsync()

        verify(exactly = 1) { redisClient.shutdownAsync() }

        assertNull(client.pool.closeAsync().getNow(mockk()))
    }

    @Test
    fun `pool configuration is used to create pool`() {
        val poolConfig = BoundedPoolConfig.builder()
            .maxIdle(Random.nextInt(Int.MIN_VALUE, Int.MAX_VALUE))
            .minIdle(Random.nextInt(Int.MIN_VALUE, Int.MAX_VALUE))
            .maxTotal(Random.nextInt(Int.MIN_VALUE, Int.MAX_VALUE))
            .build()

        val client = CacheClient(RedisURI.create(redisContainer.url), poolConfiguration = poolConfig)

        val pool = client.pool
        assertEquals(poolConfig.maxIdle, pool.maxIdle)
        assertEquals(poolConfig.minIdle, pool.minIdle)
        assertEquals(poolConfig.maxTotal, pool.maxTotal)

        client.close()
    }
}