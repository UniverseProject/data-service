package org.universe.configuration

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.universe.utils.getRandomString
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfigurationTest {

    @Nested
    inner class Cache {

        @Test
        fun `load client identity configuration`() {
            val prefix = getRandomString()
            System.setProperty("cache.clientId.useUUID", "true")
            System.setProperty("cache.clientId.useName", "true")
            System.setProperty("cache.clientId.prefixKey", prefix)
            ServiceConfiguration.reloadConfigurations()

            assertTrue { ServiceConfiguration.cacheConfiguration[CacheConfiguration.ClientIdentityConfiguration.useUUID] }
            assertTrue { ServiceConfiguration.cacheConfiguration[CacheConfiguration.ClientIdentityConfiguration.useName] }
            assertEquals(prefix, ServiceConfiguration.cacheConfiguration[CacheConfiguration.ClientIdentityConfiguration.prefixKey])
        }

        @Test
        fun `load profile skin configuration`() {
            val prefix = getRandomString()
            System.setProperty("cache.skin.prefixKey", prefix)
            ServiceConfiguration.reloadConfigurations()

            assertEquals(prefix, ServiceConfiguration.cacheConfiguration[CacheConfiguration.ProfileSkinConfiguration.prefixKey])
        }

        @Test
        fun `load profile id configuration`() {
            val prefix = getRandomString()
            System.setProperty("cache.profilId.prefixKey", prefix)
            ServiceConfiguration.reloadConfigurations()

            assertEquals(prefix, ServiceConfiguration.cacheConfiguration[CacheConfiguration.ProfileIdConfiguration.prefixKey])
        }
    }

    @Test
    fun `reload configuration`() {
        var prefix = getRandomString()
        System.setProperty("cache.skin.prefixKey", prefix)
        ServiceConfiguration.reloadConfigurations()

        assertEquals(prefix, ServiceConfiguration.cacheConfiguration[CacheConfiguration.ProfileSkinConfiguration.prefixKey])

        prefix = getRandomString()
        System.setProperty("cache.skin.prefixKey", prefix)
        ServiceConfiguration.reloadConfigurations()
        assertEquals(prefix, ServiceConfiguration.cacheConfiguration[CacheConfiguration.ProfileSkinConfiguration.prefixKey])
    }
}