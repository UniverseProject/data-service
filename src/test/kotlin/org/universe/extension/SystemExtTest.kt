package org.universe.extension

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junitpioneer.jupiter.SetEnvironmentVariable
import org.junitpioneer.jupiter.SetSystemProperty
import org.universe.utils.getRandomString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SystemExtTest {

    @Nested
    @DisplayName("Get env or property variable")
    inner class GetEnvOrProperty {

        @Test
        fun `key not found`() {
            assertNull(getEnvOrProperty(getRandomString()))
        }

        @Test
        @SetEnvironmentVariable(key = "testEnv", value = "testValueEnv")
        fun `key found from environnement`() {
            assertEquals("testValueEnv", getEnvOrProperty("testEnv"))
        }

        @Test
        @SetSystemProperty(key = "testProperty", value = "testValueProperty")
        fun `key found from properties`() {
            assertEquals("testValueProperty", getEnvOrProperty("testProperty"))
        }

        @Test
        @SetEnvironmentVariable(key = "test", value = "testValueEnv")
        @SetSystemProperty(key = "test", value = "testValueProperty")
        fun `key found from properties before env`() {
            assertEquals("testValueProperty", getEnvOrProperty("test"))
        }
    }
}