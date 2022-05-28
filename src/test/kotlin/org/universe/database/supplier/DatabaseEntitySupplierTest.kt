package org.universe.database.supplier

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.postgresql.Driver
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.universe.database.client.createIdentity
import org.universe.database.dao.ClientIdentities
import kotlin.test.*

@Testcontainers
class DatabaseEntitySupplierTest : KoinTest {

    @Container
    private val psqlContainer = PostgreSQLContainer("postgres:alpine")
        .withDatabaseName("db")
        .withUsername("test")
        .withPassword("test")

    private lateinit var databaseEntitySupplier: DatabaseEntitySupplier

    @BeforeTest
    fun onBefore() {
        Database.connect(
            url = psqlContainer.jdbcUrl,
            driver = Driver::class.java.name,
            user = "test",
            password = "test"
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

    interface DatabaseTest {
        fun `data not found from database`()
        fun `data is retrieved from database`()
    }

    @Nested
    @DisplayName("Get identity by uuid")
    inner class GetIdentityByUUID : DatabaseTest {

        @Test
        override fun `data not found from database`(): Unit = runBlocking {
            val id = createIdentity()
            databaseEntitySupplier.saveIdentity(createIdentity())
            assertNull(databaseEntitySupplier.getIdentityByUUID(id.uuid))
        }

        @Test
        override fun `data is retrieved from database`(): Unit = runBlocking {
            val id = createIdentity()
            databaseEntitySupplier.saveIdentity(id)
            assertEquals(id, databaseEntitySupplier.getIdentityByUUID(id.uuid))
        }

    }

    @Nested
    @DisplayName("Get identity by name")
    inner class GetIdentityByName : DatabaseTest {

        @Test
        override fun `data not found from database`(): Unit = runBlocking {
            val id = createIdentity()
            databaseEntitySupplier.saveIdentity(createIdentity())
            assertNull(databaseEntitySupplier.getIdentityByName(id.name))
        }

        @Test
        override fun `data is retrieved from database`(): Unit = runBlocking {
            val id = createIdentity()
            databaseEntitySupplier.saveIdentity(id)
            assertEquals(id, databaseEntitySupplier.getIdentityByName(id.name))
        }

    }

    @Nested
    @DisplayName("Save identity")
    inner class SaveIdentity {

        @Test
        fun `save identity with uuid not exists`(): Unit = runBlocking {
            val id = createIdentity()
            databaseEntitySupplier.saveIdentity(createIdentity())
            databaseEntitySupplier.saveIdentity(id)
        }

        @Test
        fun `save identity but uuid already exists`() {
            val id = createIdentity()
            val idSaved = createIdentity().apply { uuid = id.uuid }
            runBlocking {
                databaseEntitySupplier.saveIdentity(idSaved)
            }

            assertThrows<ExposedSQLException> {
                // We need to divide the context of the coroutine because the exception extends to the whole coroutine.
                runBlocking {
                    databaseEntitySupplier.saveIdentity(id)
                }
            }
        }

    }
}