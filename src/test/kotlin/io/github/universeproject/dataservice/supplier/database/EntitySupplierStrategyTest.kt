package io.github.universeproject.dataservice.supplier.database

import io.github.universeproject.dataservice.cache.CacheClient
import io.github.universeproject.dataservice.container.createPSQLContainer
import io.github.universeproject.dataservice.container.createRedisContainer
import io.github.universeproject.dataservice.data.ClientIdentities
import io.github.universeproject.dataservice.data.ClientIdentityCacheServiceImpl
import io.github.universeproject.dataservice.supplier.SupplierConfiguration
import io.github.universeproject.dataservice.utils.createIdentity
import io.lettuce.core.RedisURI
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.postgresql.Driver
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.*

@Testcontainers
class EntitySupplierStrategyTest {

    companion object {
        @JvmStatic
        @Container
        private val psqlContainer = createPSQLContainer()

        @JvmStatic
        @Container
        private val redisContainer = createRedisContainer()
    }

    private lateinit var cacheClient: CacheClient

    private lateinit var configuration: SupplierConfiguration

    private lateinit var cacheEntitySupplier: CacheEntitySupplier
    private lateinit var databaseSupplier: EntitySupplier

    @BeforeTest
    fun onBefore() = runBlocking {
        cacheClient = CacheClient {
            uri = RedisURI.create(redisContainer.url)
        }
        Database.connect(
            url = psqlContainer.jdbcUrl,
            driver = Driver::class.java.name,
            user = psqlContainer.username,
            password = psqlContainer.password
        )
        transaction {
            SchemaUtils.create(ClientIdentities)
        }

        cacheEntitySupplier = CacheEntitySupplier(ClientIdentityCacheServiceImpl(cacheClient))
        databaseSupplier = DatabaseEntitySupplier()
        configuration = SupplierConfiguration(mockk(), cacheClient)
    }

    @AfterTest
    fun onAfter() {
        cacheClient.close()
    }

    @Test
    fun `database supplier corresponding to the class`() {
        assertEquals(DatabaseEntitySupplier::class, EntitySupplier.database()::class)
    }

    @Test
    fun `cache supplier corresponding to the class`() {
        assertEquals(CacheEntitySupplier::class, EntitySupplier.cache(configuration)::class)
    }

    @Nested
    @DisplayName("Caching Database")
    inner class CachingDatabase {

        private lateinit var supplier: EntitySupplier

        @BeforeTest
        fun onBefore() {
            supplier = EntitySupplier.cachingDatabase(configuration)
        }

        @Test
        fun `data found in database is saved into cache`(): Unit = runBlocking {
            val id = createIdentity()
            val uuid = id.uuid
            databaseSupplier.saveIdentity(id)

            assertNull(cacheEntitySupplier.getIdentityByUUID(uuid))
            assertEquals(id, supplier.getIdentityByUUID(uuid))
            assertEquals(id, cacheEntitySupplier.getIdentityByUUID(uuid))
        }

        @Test
        fun `data present in cache is not used to find value`(): Unit = runBlocking {
            val id = createIdentity()
            val uuid = id.uuid
            cacheEntitySupplier.saveIdentity(id)

            // The data is saved in the cache, but not in the database
            // So if the supplier returns null, it's because the value from cache is not used
            // And a database interaction is made
            assertNull(supplier.getIdentityByUUID(uuid))
        }
    }

    @Nested
    @DisplayName("Cache with Database fallback")
    inner class CacheWithDatabaseFallback {

        private lateinit var supplier: EntitySupplier

        @BeforeTest
        fun onBefore() {
            supplier = EntitySupplier.cacheWithDatabaseFallback(configuration)
        }

        @Test
        fun `data found in database is not saved into cache`(): Unit = runBlocking {
            val id = createIdentity()
            databaseSupplier.saveIdentity(id)

            val uuid = id.uuid
            assertEquals(id, supplier.getIdentityByUUID(uuid))
            assertNull(cacheEntitySupplier.getIdentityByUUID(uuid))
        }

        @Test
        fun `data present in cache is use to avoid database call`(): Unit = runBlocking {
            val id = createIdentity()
            cacheEntitySupplier.saveIdentity(id)

            val uuid = id.uuid
            assertEquals(id, supplier.getIdentityByUUID(uuid))
            assertNull(databaseSupplier.getIdentityByUUID(uuid))
        }
    }

    @Nested
    @DisplayName("Cache with caching result of Database fallback")
    inner class CacheWithCachingDatabaseFallback {

        private lateinit var supplier: EntitySupplier

        @BeforeTest
        fun onBefore() {
            supplier = EntitySupplier.cacheWithCachingDatabaseFallback(configuration)
        }

        @Test
        fun `data found in database is saved into cache`(): Unit = runBlocking {
            val id = createIdentity()
            databaseSupplier.saveIdentity(id)

            val uuid = id.uuid
            assertEquals(id, supplier.getIdentityByUUID(uuid))
            assertEquals(id, cacheEntitySupplier.getIdentityByUUID(uuid))
        }

        @Test
        fun `data present in cache is use to avoid database call`(): Unit = runBlocking {
            val id = createIdentity()
            cacheEntitySupplier.saveIdentity(id)

            val uuid = id.uuid
            assertEquals(id, supplier.getIdentityByUUID(uuid))
            assertNull(databaseSupplier.getIdentityByUUID(uuid))
        }
    }

}
