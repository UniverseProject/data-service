package org.universe.database

import org.testcontainers.containers.PostgreSQLContainer

fun createPSQLContainer(): PostgreSQLContainer<*> = PostgreSQLContainer("postgres:alpine")
    .withDatabaseName("db")
    .withUsername("test")
    .withPassword("test")