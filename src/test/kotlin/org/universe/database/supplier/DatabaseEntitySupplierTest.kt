package org.universe.database.supplier

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.postgresql.Driver
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.universe.container.createPSQLContainer
import org.universe.database.client.createIdentity
import org.universe.database.dao.ClientIdentities
import org.universe.database.dao.ClientIdentity
import kotlin.test.*

@Testcontainers
class DatabaseEntitySupplierTest : KoinTest {

    companion object {
        @JvmStatic
        @Container
        private val psqlContainer = createPSQLContainer()
    }

    private lateinit var databaseEntitySupplier: DatabaseEntitySupplier

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

        databaseEntitySupplier = DatabaseEntitySupplier()
    }

    @AfterTest
    fun onAfter() {
        stopKoin()
    }

    @Nested
    @DisplayName("Get identity")
    inner class GetIdentity {

        @Test
        fun `data is not into the cache with uuid key`() = runBlocking {
            dataNotInCache { databaseEntitySupplier.getIdentityByUUID(it.uuid) }
        }

        @Test
        fun `data is not into the cache with name key`() = runBlocking {
            dataNotInCache { databaseEntitySupplier.getIdentityByName(it.name) }
        }

        private suspend inline fun dataNotInCache(getter: (ClientIdentity) -> ClientIdentity?) {
            val id = createIdentity()
            databaseEntitySupplier.saveIdentity(id)
            assertNull(getter(createIdentity()))
        }

        @Test
        fun `data is retrieved from the cache with uuid key`() = runBlocking {
            dataPresentsInCache { databaseEntitySupplier.getIdentityByUUID(it.uuid)!! }
        }

        @Test
        fun `data is retrieved from the cache with name key`() = runBlocking {
            dataPresentsInCache { databaseEntitySupplier.getIdentityByName(it.name)!! }
        }

        private suspend inline fun dataPresentsInCache(getter: (ClientIdentity) -> ClientIdentity) {
            val id = createIdentity()
            databaseEntitySupplier.saveIdentity(id)
            assertEquals(id, getter(id))
        }

    }

    @Nested
    @DisplayName("Save identity")
    inner class SaveIdentity {

        @Test
        fun `save identity with uuid not exists`() = runBlocking {
            saveWithKeyNotExists(
                { it.uuid },
                { databaseEntitySupplier.getIdentityByUUID(it) }
            )
        }

        @Test
        fun `save identity with name not exists`() = runBlocking {
            saveWithKeyNotExists(
                { it.name },
                { databaseEntitySupplier.getIdentityByName(it) }
            )
        }

        private suspend inline fun <T> saveWithKeyNotExists(
            getKey: (ClientIdentity) -> T,
            getId: (T) -> ClientIdentity?
        ) {
            val id = createIdentity()
            val key = getKey(id)
            assertNull(getId(key))
            databaseEntitySupplier.saveIdentity(id)
            assertEquals(id, getId(key))
        }

        @Test
        fun `save identity but uuid already exists`() {
            val id = createIdentity()
            val uuid = id.uuid

            runBlocking {
            assertNull(databaseEntitySupplier.getIdentityByUUID(uuid))
            databaseEntitySupplier.saveIdentity(id)
            assertEquals(id, databaseEntitySupplier.getIdentityByUUID(uuid))
            }

            val id2 = createIdentity().apply { this.uuid = uuid }
            assertThrows<Exception> {
                runBlocking {
                    databaseEntitySupplier.saveIdentity(id2)
                }
            }
        }

        @Test
        fun `save identity but name already exists`() = runBlocking {
            val id = createIdentity()
            val name = id.name

            assertNull(databaseEntitySupplier.getIdentityByName(name))
            databaseEntitySupplier.saveIdentity(id)
            assertEquals(id, databaseEntitySupplier.getIdentityByName(name))

            val id2 = createIdentity().apply { this.name = name }
            databaseEntitySupplier.saveIdentity(id2)
            /**
             * Multiple instance with the same name,
             */
            assertNull(databaseEntitySupplier.getIdentityByName(name))
        }

    }
}