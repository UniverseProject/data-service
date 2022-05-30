package org.universe.cache

import io.lettuce.core.api.StatefulRedisConnection
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.pool2.impl.GenericObjectPool
import kotlin.test.Test

class CacheClientTest {

//    @Test
//    fun `pool is used to get connection from client`() {
//        val client = mockk<CacheClient>()
//        val mockPool = mockk<GenericObjectPool<StatefulRedisConnection<ByteArray, ByteArray>>>()
//
//        coEvery { client.connect<Unit>(any()) } answers { callOriginal() }
//        every { client.pool } returns mockPool
//
//        val connection = mockk<StatefulRedisConnection<ByteArray, ByteArray>>()
//        every { mockPool.borrowObject() } returns connection
//        val commandCoroutine = mockk<RedisCoroutinesCommands<ByteArray, ByteArray>>()
//        every { connection.coroutines() } returns commandCoroutine
//
//        client.connect {
//            assertEquals(commandCoroutine, it)
//        }
//    }

    @Test
    fun `close client will close the pool`() {
        val client = mockk<CacheClient>()
        val mockPool = mockk<GenericObjectPool<StatefulRedisConnection<ByteArray, ByteArray>>>()

        every { client.close() } answers { callOriginal() }
        every { client.pool } returns mockPool
        justRun { mockPool.close() }

        client.close()

        verify(exactly = 1) { mockPool.close() }

    }
}