package org.universe.database.supplier

import dev.kord.cache.api.DataCache
import dev.kord.cache.api.data.description
import dev.kord.cache.api.put
import dev.kord.cache.map.MapDataCache
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.postgresql.Driver
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.universe.database.client.createIdentity
import org.universe.container.createPSQLContainer
import org.universe.database.dao.ClientIdentities
import org.universe.database.dao.ClientIdentity
import org.universe.model.ProfileId
import org.universe.model.ProfileSkin
import kotlin.test.*

@Testcontainers
class EntitySupplierCompanionTest : KoinTest {

    @Container
    private val psqlContainer = createPSQLContainer()

    private val cache: DataCache by inject()
    private lateinit var cacheEntitySupplier: CacheEntitySupplier
    private lateinit var databaseSupplier: EntitySupplier

    @BeforeTest
    fun onBefore() {
        Database.connect(
            url = psqlContainer.jdbcUrl,
            driver = Driver::class.java.name,
            user = psqlContainer.username,
            password = psqlContainer.password
        )
        transaction {
            SchemaUtils.create(ClientIdentities)
        }
        
        startKoin {
            modules(
                module {
                    val mapCache: DataCache = MapDataCache {
                        forType<ProfileId> { concurrentHashMap() }
                        forType<ProfileSkin> { concurrentHashMap() }
                    }

                    runBlocking {
                        mapCache.register(description(ClientIdentity::uuid))
                    }

                    single { mapCache }
                })
        }
        cacheEntitySupplier = CacheEntitySupplier()
        databaseSupplier = DatabaseEntitySupplier()
    }

    @AfterTest
    fun onAfter() {
        stopKoin()
    }

    @Test
    fun `database supplier corresponding to the class`() {
        assertEquals(DatabaseEntitySupplier::class, EntitySupplier.database::class)
    }

    @Test
    fun `cache supplier corresponding to the class`() {
        assertEquals(CacheEntitySupplier::class, EntitySupplier.cache::class)
    }

    @Nested
    @DisplayName("Caching Database")
    inner class CachingDatabase {

        private lateinit var supplier: EntitySupplier

        @BeforeTest
        fun onBefore() {
            supplier = EntitySupplier.cachingDatabase
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
            cache.put(id)

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
            supplier = EntitySupplier.cacheWithDatabaseFallback
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
            cache.put(id)

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
            supplier = EntitySupplier.cacheWithCachingDatabaseFallback
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
            cache.put(id)

            val uuid = id.uuid
            assertEquals(id, supplier.getIdentityByUUID(uuid))
            assertNull(databaseSupplier.getIdentityByUUID(uuid))
        }
    }

}
