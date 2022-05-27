package org.universe.database.client.supplier

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.postgresql.Driver
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.universe.database.supplier.DatabaseEntitySupplier
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
        Database.connect(psqlContainer.jdbcUrl, Driver::class.java.name)
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
            TODO()
        }

        @Test
        override fun `data is retrieved from database`(): Unit = runBlocking {
            TODO()
        }

    }

    @Nested
    @DisplayName("Get identity by name")
    inner class GetIdentityByName : DatabaseTest {

        @Test
        override fun `data not found from database`(): Unit = runBlocking {
            TODO()
        }

        @Test
        override fun `data is retrieved from database`(): Unit = runBlocking {
            TODO()
        }

    }
}